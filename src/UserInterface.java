import java.time.*;
import java.util.concurrent.*;
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

	private final DataRequester webRequests;

	private final BlockingQueue<ApiResponse> recv;

	public UserInterface(String objectURL, long millisecondsPerUpdate, boolean makeLogFiles, long webRequestTimer, TimeUnit unit) {
		milliDelay = millisecondsPerUpdate;
		recv = new ArrayBlockingQueue<>(1);
		webRequests = new DataRequester(objectURL, recv, Thread.currentThread(), makeLogFiles, webRequestTimer, unit);
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
		try {
			while (true) {
				try {
					if (lastData == null) {
						System.out.println("Waiting for web...");
						Thread.sleep(Math.max(500,milliDelay*5));
					}
					else if (next != null) {
						next = null;
						System.out.println();System.out.println();System.out.println();
						System.out.println("Latest data:");
						printVectors(lastVectors);
						System.out.println();
						printElements(lastElements);
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
					cycles = 0;
					next = recv.poll();
					if (next != null) {
						lastData = next;
						try {
							lastVectors = getVectors(lastData);
							lastElements = KeplerElements.fromStateVector(lastVectors, EARTH_GRAV_PARAM);
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
			recv.clear();
			in.close();
			requests.interrupt();
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

	private void wasInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
	}

	private void printVectors(StateVector vecs) {
		System.out.print("Epoch of latest data: ");
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
		}
	}

	private void printElements(KeplerElements elems) {
		System.out.print("Semi-major axis (a): ");
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
		System.out.println("km");
	}
}
