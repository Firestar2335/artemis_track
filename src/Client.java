import ODM.*;
import java.io.*;
import java.util.*;
import java.time.*;
import java.util.concurrent.TimeUnit;

import ExtraMath.*;

//import java.math.*;

public class Client {
	/** The option to use network requests */
	private static final String NETWORK_KEYWORD = "network";
	/** The option to replay logs */
	private static final String REPLAY_KEYWORD = "replay";

	private static final String HELP_OPTION = "-h";

	/** The default update speed in milliseconds */
	private static final long DEFAULT_DELAY = 65000;

	private static final boolean DEFAULT_LOGGING = false;

	private static final boolean DEFAULT_GUI = true;
	/** Standard gravitational parameter of earth in km^3/s^2 */
	public static final double EARTH_GRAV_PARAM = 3.986004418e5;

	public static void main(String[] args) {
		if (args.length == 0) {
			programHelp();
			System.exit(0);
		}
		switch (args[0]) {
			case NETWORK_KEYWORD: fromNet(args); break;
			case REPLAY_KEYWORD: fromLocal(args); break;
			case HELP_OPTION: programHelp(); break;
			default:
				System.err.print("Unrecognized mode: ");
				System.err.println(args[0]);
				System.exit(1);
		}
		System.exit(0);
	}

	private static void programHelp() {
		System.out.println("Use: java -jar artemis_track.jar " + NETWORK_KEYWORD + "|" + REPLAY_KEYWORD + " [args...]\n"
			+ "\t- " + NETWORK_KEYWORD + ": This will take telemetry data from the NASA server to display the most recent information\n"
			+ "\t- " + REPLAY_KEYWORD + ": This will take past telemetry data from localy stored log files to replay recorded data");
	}

	private static void netHelp() {
		System.out.println("Use: java -jar artemis_track.jar " + NETWORK_KEYWORD + " [updatePeriod] [logs] [gui]\n"
			+ "This will update with information from the NASA web server\n"
			+ "\t- updatePeriod: Time in milliseconds to wait between printing data to the console. New data will always be printed immediately when received regardless of this option. Default: "+DEFAULT_DELAY+"\n"
			+ "\t- logs: Whether to save the received JSON files to the log folder. Default: "+DEFAULT_LOGGING+"\n"
			+ "\t- gui: Whether to open and output data to a GUI. Default: "+DEFAULT_GUI);
	}

	private static void localHelp() {
		System.out.println("Use: java -jar artemis_track.jar " + REPLAY_KEYWORD + " logDir firstGeneration [timeScale] [updatePeriod] [gui]\n"
			+ "This will update with information from locally stored logs to review prior data\n"
			+ "\t- logDir: The directory that the logs are stored in\n"
			+ "\t- firstGeneration: The generation of logs to start at\n"
			+ "\t- timeScale: the factor by which to scale the time between logs. Must be positive. Default: 1.0\n"
			+ "\t- updatePeriod: The time in milliseconds between each user interface refresh. Default: "+DEFAULT_DELAY+"\n"
			+ "\t- gui: Whether to use a GUI for output. Default: "+DEFAULT_GUI);
	}

	/**
	 * 
	 * @param args
	 */
	private static void fromNet(String[] args) {
		if (args.length == 0 || !args[0].equals(NETWORK_KEYWORD)) {
			throw new IllegalArgumentException("The improper action was specified");
		}
		if (args.length == 1) {
			netHelp();
			System.out.println("\nUsing default values for parameters.\n");
		}
		else if (args.length >= 2 && args[1].equals(HELP_OPTION)) {
			netHelp();
			System.exit(0);
		}
		long delay = DEFAULT_DELAY;
		if (args.length >= 2) {
			try {
				delay = Long.parseLong(args[1]);
			}
			catch (NumberFormatException e) {
				delay = DEFAULT_DELAY;
			}
		}
		boolean log = DEFAULT_LOGGING;
		if (args.length >= 3) {
			if (args[2].equalsIgnoreCase("true")) {
				log = true;
			}
			else if (args[2].equalsIgnoreCase("false")) {
				log = false;
			}
		}

		boolean gui = DEFAULT_GUI;
		if (args.length >= 4) {
			if (args[3].equalsIgnoreCase("true")) {
				gui = true;
			}
			else if (args[3].equalsIgnoreCase("false")) {
				gui = false;
			}
		}
		UserInterface ui = UserInterface.fromWebRequests(delay, log, gui, "https://storage.googleapis.com/storage/v1/b/p-2-cen1/o/October%2F1%2FOctober_105_1.txt", 5, TimeUnit.SECONDS);
		ui.run();
	}

	private static void fromLocal(String[] args) {
		if (args.length == 0 || !args[0].equals(REPLAY_KEYWORD)) {
			throw new IllegalArgumentException("The improper action was specified");
		}
		if (args.length == 1) {
			localHelp();
			System.err.println("Missing value for logDir");
			System.exit(1);//System.out.println("Using default values for parameters.\n");
		}
		else if (args.length >= 2 && args[1].equals(HELP_OPTION)) {
			localHelp();
			System.exit(0);
		}
		else if (args.length == 2) {
			localHelp();
			System.err.println("Missing value for firstGeneration");
			System.exit(1);
		}
		String dirName = args[1];
		long firstGen = 0;
		try {
			firstGen = Long.parseLong(args[2]);
		} catch (NumberFormatException e) {
			System.err.println("The value provided for firstGeneration was not a number");
			System.exit(1);
		}
		double timeScale = 1.0;
		long delay = DEFAULT_DELAY;
		boolean gui = DEFAULT_GUI;
		if (args.length >= 4) {
			try {
				timeScale = Double.parseDouble(args[3]);
				if (timeScale <= 0) {
					System.err.println("Provided value for timeScale was negative");
					System.exit(1);
				}
				else if (!Double.isFinite(timeScale)) {
					System.err.println("Provided value for timeScale was not a finite value");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.err.println("Value provided for timeScale was not a number");
				System.exit(1);
			}
		}
		if (args.length >= 5) {
			try {
				delay = Long.parseLong(args[4]);
				if (delay <= 0) {
					System.err.println("delay was not positive");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.err.println("The value provided for delay was not a number");
				System.exit(1);
			}
		}
		if (args.length >= 6) {
			if (args[5].equalsIgnoreCase("true")) {
				gui = true;
			}
			else if (args[5].equalsIgnoreCase("false")) {
				gui = false;
			}
			else {
				System.err.println("The value provided for gui was not a boolean");
				System.exit(1);
			}
		}
		UserInterface ui = UserInterface.fromLogReplay(delay, gui, dirName, firstGen, timeScale, 100, TimeUnit.MILLISECONDS);
		ui.run();
	}

	public static void rmain(String[] args) {
		System.out.println((byte) Integer.parseUnsignedInt("FF",16));
		System.exit(0);
		//Instant selected = Instant.parse("2026-04-02T03:09:34.583Z");//"2026-04-06T05:11:39.109Z");
		Instant cur = Instant.now();
		String filePath = "C:\\Users\\thoma\\Documents\\artemisII\\artemis-ii-oem-2026-04-06-pre-otc3-to-ei\\Artemis_II_OEM_2026_04_06_Pre-OTC3_to_EI.asc";
		OrbitalDataMessage res = OrbitalDataMessage.read(new File(filePath));
		OrbitalEphemerisMessage m = (OrbitalEphemerisMessage) res;
		//StateVector selVector = m.getStateVector(selected);
		
		/*KeplerElements selElements = KeplerElements.fromStateVector(selVector, EARTH_GRAV_PARAM);
		StateVector sanCheck = StateVector.fromElements(selElements);
		
		System.out.print("Selected vector: ");
		System.out.println(selVector);
		System.out.print("Selected orbital parameters: ");
		System.out.println(selElements);*/
		//System.out.print("Sanity check on conversion: ");
		//System.out.println(sanCheck);
		//System.out.print("Sanity check part 2: ");
		//System.out.println(KeplerElements.fromStateVector(sanCheck, EARTH_GRAV_PARAM));
		StateVector curVector = m.getStateVector(cur);
		KeplerElements curElements = KeplerElements.fromStateVector(curVector, EARTH_GRAV_PARAM);
		System.out.print("Current vector: ");
		System.out.println(curVector);
		System.out.print("Current orbital parameters: ");
		System.out.println(curElements);
		//System.out.print("Sanity check on conversion: ");
		//System.out.println(StateVector.fromElements(curElements));
	}

	public static void lmain(String[] args) {
		//String filePath = "C:\\Users\\thoma\\Documents\\artemisII\\artemis-ii-oem-2026-04-02-to-ei-v3\\Artemis_II_OEM_2026_04_02_to_EI_v3.asc";
		//OrbitalDataMessage res = OrbitalDataMessage.read(new File(filePath));
		//System.out.println(res);
		Scanner in = new Scanner(System.in);
		Scanner line;
		double n = 0;
		double k = 0;
		boolean quit = false;

		ExtraMath.initCoefs(9,11);
		//in.close();
		//System.exit(0);
		//System.out.println(available());
		//System.out.println(in.hasNextLine());
		//double n = Double.NaN;
		while(true) {
			System.out.print("Type your number (s): ");
			line = new Scanner(in.nextLine());
			while (line.hasNext()) {
				if (!line.hasNextDouble()) {
					String next = line.next();
					if (next.equalsIgnoreCase("quit")) {
						quit = true;
						break;
					}
					else {
						System.out.print("Unknown number: ");
						System.out.println(next);
						continue;
					}
				}
				if (n == 0) {
					n = line.nextDouble();
					//continue;
				}
				else {
					k = line.nextDouble();
				}
				if (k == 0) {
					continue;
				}
				//n=line.nextDouble();
				//System.out.print("     Math.exp(");
				//System.out.print(n);
				//System.out.print(",");
				//System.out.print(k);
				//System.out.print(") = ");
				//System.out.println(Math.exp(n));
				System.out.print("bessel(");
				System.out.print(n);
				System.out.print(",");
				System.out.print(k);
				System.out.print(") = ");
				System.out.println(ExtraMath.besselFunction(n,k));
				//BigDecimal r = ExtraMath.exp(new BigDecimal(n));
				//System.out.println(r);
				//System.out.print("(");
				//System.out.print(r.round(MathContext.DECIMAL128));
				//System.out.println(")");
				//System.out.println(r.ulp());
				n = 0;
				k = 0;
			}
			line.close();
			if (quit) {
				break;
			}
		}
		in.close();
	}


	public static int available() {
		try {
			return System.in.available();
		}
		catch (IOException e) {
			return 0;
		}
	}
}
