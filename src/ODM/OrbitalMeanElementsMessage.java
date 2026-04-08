package ODM;

import java.time.*;
//import java.time.temporal.Temporal;
import java.util.*;

public class OrbitalMeanElementsMessage extends OrbitalDataMessage {
	public final KeplerElements elems;
	public final SpacecraftParameters scParams;
	public final TLEParameters tleParams;
	public final CovarianceMatrix covar;
	private final Map<String,String> userDefined;

	protected OrbitalMeanElementsMessage(Header h, OMMMetadata meta, KeplerElements elems, 
			SpacecraftParameters scParam, TLEParameters tleParams, CovarianceMatrix covar, Map<String,String> userDef) {
		super(h, meta);
		this.elems = elems;
		this.scParams = scParam;
		this.tleParams = tleParams;
		this.covar = covar;
		this.userDefined = new TreeMap<>(userDef);
	}
	

	public static OrbitalMeanElementsMessage fromScanner(Scanner s, Header h) {
		OMMMetadata meta = new OMMMetadata(s);
		String timeSystem = meta.timeSystem;
		String com = Patterns.optionalComment(s);
		Instant epoch = Patterns.inTimeSystem(Patterns.parseTimestamp(Patterns.mandatoryKVN(s, "EPOCH", Patterns.TIMECODESTRING).group("date")), timeSystem);
		KeplerElements elem = KeplerElements.fromScanner(s, epoch, com);

		com = Patterns.optionalComment(s);
		SpacecraftParameters params = SpacecraftParameters.fromScannerOptional(s, com);

		if (params != null) {
			com = Patterns.optionalComment(s);
		}
		TLEParameters tleParams = TLEParameters.fromScannerOptional(s, meta, com);
		
		if (tleParams != null) {
			com = Patterns.optionalComment(s);
		}
		CovarianceMatrix covar = CovarianceMatrix.fromScannerOptional(s, epoch, meta.refFrame, com);

		Map<String,String> userDef = Patterns.readUserDefined(s);
		return new OrbitalMeanElementsMessage(h,meta,elem,params,tleParams,covar,userDef);
	}

	public MessageType messageType() {
		return MessageType.OMM;
	}

	public Map<String, String> getUserDefined() {
		return new TreeMap<>(userDefined);
	}
}
