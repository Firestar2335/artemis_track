package ODM;

import java.util.*;
import java.util.regex.MatchResult;
import java.time.*;

public class ManeuverParameters {
	public final String comment;

	public final Instant ignition;

	public final double duration;
	
	public final double deltaMass;
	
	public final String refFrame;

	public final Vector3D deltaV;

	private ManeuverParameters(String comment, Instant ignition, double duration, double deltaMass, String refFrame, Vector3D deltaV) {
		this.comment = comment;
		this.ignition = ignition;
		this.duration = duration;
		this.deltaMass = deltaMass;
		this.refFrame = refFrame;
		this.deltaV = deltaV;
	}

	public static ManeuverParameters fromScannerOptional(Scanner s, String timeSystem, String comment) {
		Instant ignition = null;
		double dur = 0.0;
		double dm = 0.0;
		String frame = null;
		double dx = 0.0;
		double dy = 0.0;
		double dz = 0.0;
		MatchResult epochMatch = Patterns.optionalKVN(s, "MAN_EPOCH_IGNITION", Patterns.TIMECODESTRING);
		if (epochMatch != null) {
			ignition = Patterns.inTimeSystem(Patterns.parseTimestamp(epochMatch.group("date")), timeSystem);
		}
		MatchResult durMatch = Patterns.optionalKVN(s, "MAN_DURATION", Patterns.OPTIONALUNIT);
		if (durMatch != null) {
			Patterns.checkUnit(durMatch, "s");
			dur = Patterns.fromMatch(durMatch);
		}
		MatchResult dmMatch = Patterns.optionalKVN(s, "MAN_DELTA_MASS", Patterns.OPTIONALUNIT);
		if (dmMatch != null) {
			Patterns.checkUnit(durMatch, "kg");
			dm = Patterns.fromMatch(dmMatch);
		}
		MatchResult frameMatch = Patterns.optionalKVN(s, "MAN_REF_FRAME", Patterns.NONDECIMALSTRING);
		if (frameMatch != null) {
			frame = frameMatch.group("value");
		}
		MatchResult dxMatch = Patterns.optionalKVN(s, "MAN_DV_1", Patterns.OPTIONALUNIT);
		if (dxMatch != null) {
			Patterns.checkUnit(dxMatch, "km/s");
			dx = Patterns.fromMatch(dxMatch);
		}
		MatchResult dyMatch = Patterns.optionalKVN(s, "MAN_DV_2", Patterns.OPTIONALUNIT);
		if (dyMatch != null) {
			Patterns.checkUnit(dyMatch, "km/s");
			dy = Patterns.fromMatch(dxMatch);
		}
		MatchResult dzMatch = Patterns.optionalKVN(s, "MAN_DV_3", Patterns.OPTIONALUNIT);
		if (dzMatch != null) {
			Patterns.checkUnit(dzMatch, "km/s");
			dz = Patterns.fromMatch(dxMatch);
		}
		if (epochMatch == null && durMatch == null && dmMatch == null && frameMatch == null && dxMatch == null && dyMatch == null && dzMatch == null) {
			return null;
		}
		return new ManeuverParameters(comment, ignition, dur, dm, frame, new Vector3D(dx, dy, dz));
	}
}
