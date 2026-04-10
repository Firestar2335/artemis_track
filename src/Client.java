import ODM.*;
import java.io.*;
import java.util.*;
import java.time.*;
import java.util.concurrent.TimeUnit;

import ExtraMath.*;

//import java.math.*;

public class Client {
	/** The default update speed in milliseconds */
	private static final long DEFAULT_DELAY = 65000;

	private static final boolean DEFAULT_LOGGING = false;

	private static final boolean DEFAULT_GUI = true;
	/** Standard gravitational parameter of earth in km^3/s^2 */
	public static final double EARTH_GRAV_PARAM = 3.986004418e5;

	public static void main(String[] args) {
		//GUI Gui = new GUI(null);
		if (args.length == 0) {
			System.out.println("Use: java -jar artemis_track.jar [updatePeriod] [logs] [gui]");
			System.out.println("\t-updatePeriod: Time in milliseconds to wait between printing data to the console. New data will always be printed immediately when received regardless of this option. Default: 65000");
			System.out.println("\t-logs: Whether to save the received JSON files to the log folder. Default: false");
			System.out.println("\t-gui: Whether to open and output data to a GUI. Default: true");
			System.out.println("\nUsing default values for parameters.\n");
		}
		long delay = DEFAULT_DELAY;
		if (args.length >= 1) {
			try {
				delay = Long.parseLong(args[0]);
			}
			catch (NumberFormatException e) {
				delay = DEFAULT_DELAY;
			}
		}
		boolean log = DEFAULT_LOGGING;
		if (args.length >= 2) {
			if (args[1].equalsIgnoreCase("true")) {
				log = true;
			}
			else if (args[1].equalsIgnoreCase("false")) {
				log = false;
			}
		}

		boolean gui = DEFAULT_GUI;
		if (args.length >= 3) {
			if (args[3].equalsIgnoreCase("true")) {
				gui = true;
			}
			else if (args[1].equalsIgnoreCase("false")) {
				gui = false;
			}
		}
		UserInterface ui = new UserInterface("https://storage.googleapis.com/storage/v1/b/p-2-cen1/o/October%2F1%2FOctober_105_1.txt", delay, log, gui, 5, TimeUnit.SECONDS);
		ui.run();
		//File testJson = new File("C:\\Users\\thoma\\Documents\\artemisII\\october1.txt");
		//JsonDocument doc = JsonDocument.read(testJson);
		//System.out.println(doc);
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
