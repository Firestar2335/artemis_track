package ODM;
import java.util.*;
//import java.util.regex.*;
import java.time.*;

public class OrbitalParameterMessage extends OrbitalDataMessage {
	public final StateVector state;
	public final KeplerElements elems;
	public final SpacecraftParameters scParams;
	public final CovarianceMatrix covar;
	private final ManeuverParameters[] maneuvers;
	private final Map<String, String> userDefined;

	protected OrbitalParameterMessage(Header header, BaseMetadata metadata, StateVector s, KeplerElements el,
									  SpacecraftParameters params, CovarianceMatrix covar, 
									  List<ManeuverParameters> mans, Map<String, String> userDef) {
		super(header, metadata);
		state = s;
		elems = el;
		scParams = params;
		this.covar = covar;
		maneuvers = new ManeuverParameters[mans.size()];
		Iterator<ManeuverParameters> iter = mans.iterator();
		for (int i = 0; i < maneuvers.length; i++) {
			maneuvers[i] = iter.next();
		}
		userDefined = new TreeMap<>(userDef);
	}

	public static OrbitalParameterMessage fromScanner(Scanner s, Header h) {
		BaseMetadata meta = new BaseMetadata(s);
		String timeSystem = meta.timeSystem;
		String com = Patterns.optionalComment(s);
		Instant epoch = Patterns.inTimeSystem(Patterns.parseTimestamp(Patterns.mandatoryKVN(s, "EPOCH", Patterns.TIMECODESTRING).group("date")), timeSystem);
		StateVector vectors = StateVector.fromScanner(s, epoch, com);

		com = Patterns.optionalComment(s);
		KeplerElements elem = KeplerElements.fromScannerOptional(s, epoch, com);

		if (elem != null) {
			com = Patterns.optionalComment(s);
		}
		SpacecraftParameters scParam = SpacecraftParameters.fromScannerOptional(s, com);
		
		if (scParam != null) {
			com = Patterns.optionalComment(s);
		}
		CovarianceMatrix covar = CovarianceMatrix.fromScannerOptional(s, epoch, com);

		if (covar != null) {
			com = Patterns.optionalComment(s);
		}
		List<ManeuverParameters> mans = new ArrayList<>();
		ManeuverParameters man = ManeuverParameters.fromScannerOptional(s, timeSystem, com);
		while (man != null) {
			mans.add(man);
			com = Patterns.optionalComment(s);
			man = ManeuverParameters.fromScannerOptional(s, timeSystem, com);
		}

		Map<String, String> userDef = Patterns.readUserDefined(s);
		return new OrbitalParameterMessage(h, meta, vectors, elem, scParam, covar, mans, userDef);
	}

	public MessageType messageType() {
		return MessageType.OPM;
	}

	public Map<String, String> getUserDefined() {
		return new TreeMap<>(userDefined);
	}
}
