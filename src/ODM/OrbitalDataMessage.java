package ODM;
import java.util.regex.*;
import java.io.*;
import java.util.*;
import java.time.*;

public abstract class OrbitalDataMessage {

	public static final Pattern DELIM = Pattern.compile("\\s*\\R\\s*");

	public final Header header;
	public final Metadata metadata;

	protected OrbitalDataMessage(Header header, Metadata metadata) {
		this.header = header;
		this.metadata = metadata;
	}

	public static OrbitalDataMessage read(File file) /*throws FileNotFoundException*/ {
		Scanner reader;
		try {
			reader = new Scanner(file);
			
		}
		catch (FileNotFoundException e) {
			return null;
		}
		OrbitalDataMessage result = read(reader);
		reader.close();
		return result;
	}

	public static OrbitalDataMessage read(InputStream in) {
		Scanner reader = new Scanner(in);
		OrbitalDataMessage result = read(reader);
		reader.close();
		System.out.println(result.metadata);
		return result;
	}

	public static OrbitalDataMessage read(Scanner reader) {
		reader.useDelimiter(DELIM);
		reader.findWithinHorizon(Patterns.VERSION,0);
		MatchResult v = reader.match();
		String comm = Patterns.optionalComment(reader);
		MatchResult clas = Patterns.optionalKVN(reader, "CLASSIFICATION",Patterns.FREETEXTSTRING);
		MatchResult date = Patterns.mandatoryKVN(reader, "CREATION_DATE", Patterns.TIMECODESTRING);
		MatchResult origin = Patterns.mandatoryKVN(reader, "ORIGINATOR", Patterns.NONDECIMALSTRING);
		MatchResult id = Patterns.optionalKVN(reader, "MESSAGE_ID", Patterns.NONDECIMALSTRING);
		Header h = new Header(v.group("version"), comm, Patterns.getOrDefault(clas, "value", null), Patterns.parseTimestamp(date.group("date")).atZone(ZoneOffset.UTC), origin.group("value"), Patterns.getOrDefault(id, "value", null));
		System.out.println(h);
		switch (v.group("type")) {
			case "OPM":
				return OrbitalParameterMessage.fromScanner(reader, h);
			case "OMM":
				return OrbitalParameterMessage.fromScanner(reader, h);
			case "OEM":
				return OrbitalEphemerisMessage.fromScanner(reader, h);
			case "OCM":
				throw new UnsupportedOperationException();//return null;
			default:
				throw new IllegalArgumentException("Invalid message type");
		}
	}
	
	public abstract MessageType messageType();
}
