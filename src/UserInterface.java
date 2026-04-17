import java.time.*;
import java.util.concurrent.*;

import ExtraMath.Quaternion;
import ExtraMath.Vector3D;

import java.util.*;
import java.io.IOException;
import ODM.*;

public class UserInterface implements Runnable {
	private static final double FEET_PER_KILOMETER = 3280.84;
	/** Standard gravitational parameter of earth in km^3/s^2 */
	public static final double EARTH_GRAV_PARAM = 3.986004418e5;

	/** The number of cycles to wait before manually checking for interrupt status */
	private static final int INTERRUPT_CHECK_PERIOD = 10;

	private long milliDelay;

	private final DataGetter webRequests;

	private final BlockingQueue<ApiResponse> recv;

	private boolean quit;

	private final Thread thisThread;

	private final GUI gui;

	public UserInterface(String objectURL, long millisecondsPerUpdate, boolean makeLogFiles, boolean useGUI, long webRequestTimer, TimeUnit unit) {
		thisThread = Thread.currentThread();
		milliDelay = millisecondsPerUpdate;
		recv = new ArrayBlockingQueue<>(1);
		webRequests = new NetRequester(objectURL, recv, thisThread, makeLogFiles, webRequestTimer, unit);
		quit = false;
		if (useGUI) {
			gui = new GUI(this);
		}
		else {
			gui = null;
		}
	}

	public void run() {
		Scanner in = new Scanner(System.in);
		int cycles = 0;
		Thread requests = new Thread(webRequests);
		requests.setName("NASA Google Storage API Requests");
		requests.setDaemon(true);
		requests.start();
		ApiResponse next = null;
		ApiResponse lastData = null;
		StateVector lastVectors = null;
		KeplerElements lastElements = null;
		Quaternion lastAttitude = null;
		//EulerAngles lastAngles = null;
		Duration elapsedTime = null;
		try {
			while (true) {
				try {
					if (lastData == null) {
						System.out.println("Waiting for web...");
						Thread.sleep(Math.max(500,milliDelay*5));
					}
					else if (next != null) {
						next = null;
						if (gui == null) {
							System.out.println();System.out.println();System.out.println();
							System.out.println("Latest data:");
							printDuration(elapsedTime);
							printVectors(lastVectors);
							System.out.println();
							printElements(lastElements);
							System.out.println();
							printAngles(EulerAngles.fromQuaternion(lastAttitude));
						}
						else {
							gui.updateCurrentTelemetry(elapsedTime,lastVectors, lastElements, lastAttitude);
						}
					}
					else {
						System.out.println("\nNo new data");
					}

					cycles++;
					if (cycles >= INTERRUPT_CHECK_PERIOD) {
						wasInterrupted();
						cycles = 0;
					}
					Thread.sleep(milliDelay);
				}
				catch (InterruptedException e) {
					if (quit) {
						break;
					}
					cycles = 0;
					next = recv.poll();
					if (next != null) {
						lastData = next;
						try {
							lastVectors = getVectors(lastData);
							lastElements = KeplerElements.fromStateVector(lastVectors, EARTH_GRAV_PARAM);
							lastAttitude = getAttitude(lastData);
							elapsedTime = getDuration(lastData);
						} catch (NoSuchElementException f) {
							System.err.println("Invalid JSON telemetry received at generation "+lastData.genMicro);
							next = null;
						}
					}
					else if (System.in.available() > 0 && in.nextLine().equalsIgnoreCase("quit")) {
						break;
					}
					else {
						int status = webRequests.getStatusCode();
						if (status != 300 &&status/100 != 2) {
							System.err.println("HTTP error " + status + " when trying to request data");
							//System.out.println("Exiting...");
							break;
						}
					}
				}
			}
		}
		catch (IOException e) {

		}
		finally {
			System.out.println("Exiting...");
			requests.interrupt();
			recv.clear();
			in.close();
		}
	}

	private StateVector getVectors(ApiResponse data) {
		double xPos = data.getFromID(2003).getValueDouble()/FEET_PER_KILOMETER;
		double yPos = data.getFromID(2004).getValueDouble()/FEET_PER_KILOMETER;
		double zPos = data.getFromID(2005).getValueDouble()/FEET_PER_KILOMETER;

		double xVel = data.getFromID(2009).getValueDouble()/FEET_PER_KILOMETER;
		double yVel = data.getFromID(2010).getValueDouble()/FEET_PER_KILOMETER;
		double zVel = data.getFromID(2011).getValueDouble()/FEET_PER_KILOMETER;

		Vector3D pos = new Vector3D(xPos,yPos,zPos);
		Vector3D vel = new Vector3D(xVel,yVel,zVel);

		//Estimate acceleration vector

		Vector3D acc = pos.mul(-EARTH_GRAV_PARAM/(pos.magSquared()*pos.mag()));

		long micro = data.genMicro;
		Instant epoch = Instant.ofEpochMilli(micro/1000l).plusNanos((micro % 1000)*1000);

		return new StateVector(epoch,pos,vel,acc,null);
	}

	private EulerAngles getEulerAngles(ApiResponse data) {
		Quaternion state = getAttitude(data);
		if (state == null) {
			return null;
		}
		return EulerAngles.fromQuaternion(state);
	}

	private Quaternion getAttitude(ApiResponse data) {
		try {
			double r = data.getFromID(2012).getValueDouble();
			double i = data.getFromID(2013).getValueDouble();
			double j = data.getFromID(2014).getValueDouble();
			double k = data.getFromID(2015).getValueDouble();
			return new Quaternion(r,i,j,k);
		}
		catch (NoSuchElementException e) {
			return null;
		}
	}

	private Duration getDuration(ApiResponse data) {
		String time;
		if (data.containsID(5001)) {
			time = data.getFromID(5001).getValueString();
		} else if (data.containsID(5016)) {
			time = data.getFromID(5016).getValueString();
		} else if (data.containsID(5017)) {
			time = data.getFromID(5017).getValueString();
		} else {
			return null;
		}
		int decimalPoint = time.indexOf('.');
		long seconds;
		int nano;
		if (decimalPoint == -1) {
			seconds = Long.parseLong(time);
			nano = 0;
		} else {
			seconds = Long.parseLong(time, 0, decimalPoint, 10);
			int decimalDigits = time.length() - decimalPoint - 1;
			time = time + "0".repeat(Math.max(9 - decimalDigits, 0));
			nano = Integer.parseInt(time, decimalPoint + 1, decimalPoint + 10, 10);
		}
		return Duration.ofSeconds(seconds, nano);
	}

	private void wasInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
	}

	public static String formatDuration(Duration dur) {
		if (dur == null) {
			return "";
		}
		String format;
		if (dur.toDays() > 0) {
			format = "%1$d days, %2$d:%3$02d:%4$02d.%5$09d";
		}
		else if (dur.toHours() > 0) {
			format = "%2$d:%3$02d:%4$02d.%5$09d";
		}
		else if (dur.toMinutes() > 0) {
			format = "%3$d:%4$02d.%5$09d";
		}
		else {
			format = "%4$d.%5$09d";
		}
		return "Elapsed time: " + String.format(format,dur.toDays(),dur.toHoursPart(),dur.toMinutesPart(),dur.toSecondsPart(),dur.toNanosPart());
	}

	private void printDuration(Duration dur) {
		System.out.println(formatDuration(dur));
	}

	public static String formatAngles(EulerAngles angs) {
		return angs == null ? "" : String.format("Yaw: %f°%nPitch: %f°%nRoll: %f°",Math.toDegrees(angs.yaw),Math.toDegrees(angs.pitch),Math.toDegrees(angs.roll));
	}

	private void printAngles(EulerAngles angs) {
		System.out.println(formatAngles(angs));
	}

	public static String formatVectors(StateVector vecs) {
		StringBuilder result = new StringBuilder("Epoch of latest data: ");
		result.append(vecs.epoch);
		result.append("\nPosition vector: ");
		result.append(vecs.pos);
		result.append(" km\nDistance from center: ");
		result.append(vecs.pos.mag());
		result.append(" km\nVelocity vector: ");
		result.append(vecs.vel);
		result.append(" km/s\nSpeed: ");
		result.append(vecs.vel.mag());
		result.append(" km/s");
		if (vecs.acc != null) {
			result.append("\nAcceleration vector: ");
			result.append(vecs.acc);
			result.append(" km/s²\nMagnitude of acceleration: ");
			result.append(vecs.acc.mag());
			result.append(" km/s²\nCurvature: ");
			result.append(vecs.vel.cross(vecs.acc).mag()/Math.pow(vecs.vel.mag(),3));
			result.append(" km\u207B¹");
		}
		return result.toString();
	}

	private void printVectors(StateVector vecs) {
		System.out.println(formatVectors(vecs));
		/*System.out.print("Epoch of latest data: ");
		System.out.println(vecs.epoch);
		System.out.print("Position vector: ");
		System.out.print(vecs.pos);
		System.out.print(" km\nDistance from center: ");
		System.out.print(vecs.pos.mag());
		System.out.print(" km\nVelocity vector: ");
		System.out.print(vecs.vel);
		System.out.print(" km/s\nSpeed: ");
		System.out.print(vecs.vel.mag());
		System.out.println(" km/s");
		if (vecs.acc != null) {
			System.out.print("Acceleration vector: ");
			System.out.print(vecs.acc);
			System.out.print(" km/s²\nMagnitude of acceleration: ");
			System.out.print(vecs.acc.mag());
			System.out.print(" km/s²\nCurvature: ");
			System.out.print(vecs.vel.cross(vecs.acc).mag()/Math.pow(vecs.vel.mag(),3));
			System.out.println(" km\u207B¹");
		}*/
	}

	public static String formatElements(KeplerElements elems) {
		StringBuilder result = new StringBuilder("Semi-major axis (a): ");
		result.append(elems.sma);
		result.append(" km\nEccentricity (e): ");
		result.append(elems.ecc);
		result.append("\nInclination (i): ");
		result.append(elems.inc);
		result.append("°\nLongitude of ascending node (\u03a9): ");
		result.append(elems.raan);
		result.append("°\nArgument of periapsis (\u03c9): ");
		result.append(elems.arg);
		result.append("°\n");
		if (elems.isTrueAnomaly) {
			result.append("True anomaly (\u03bd): ");
		}
		else {
			result.append("Mean anomaly (M): ");
		}
		result.append(elems.anom);
		result.append("°\nStandard gravitational parameter (\u03bc): ");
		result.append(elems.gm);
		result.append(" km³/s²\n\nApoapsis: ");
		result.append(elems.getApoapsis());
		result.append(" km\nPeriapsis: ");
		result.append(elems.getPeriapsis());
		result.append(" km");
		return result.toString();
	}

	private void printElements(KeplerElements elems) {
		System.out.println(formatElements(elems));
		/*System.out.print("Semi-major axis (a): ");
		System.out.print(elems.sma);
		System.out.print(" km\nEccentricity (e): ");
		System.out.println(elems.ecc);
		System.out.print("Inclination (i): ");
		System.out.print(elems.inc);
		System.out.print("°\nLongitude of ascending node (\u03a9): ");
		System.out.print(elems.raan);
		System.out.print("°\nArgument of periapsis (\u03c9): ");
		System.out.print(elems.arg);
		System.out.println("°");
		if (elems.isTrueAnomaly) {
			System.out.print("True anomaly (\u03bd): ");
		}
		else {
			System.out.print("Mean anomaly (M): ");
		}
		System.out.print(elems.anom);
		System.out.print("°\nStandard gravitational parameter (\u03bc): ");
		System.out.print(elems.gm);
		System.out.println("km³/s²\n");
		System.out.print("Apoapsis: ");
		System.out.print(elems.getApoapsis());
		System.out.print("km\nPeriapsis: ");
		System.out.print(elems.getPeriapsis());
		System.out.println("km");*/
	}

	/**
	 * Quits the program
	 */
	public void quit() {
		quit = true;
		thisThread.interrupt();
	}
}
