package ODM;
import java.util.regex.*;
import java.util.*;

public class TLEParameters {
	public final String comment;

	public final int ephemerisType;

	public final String classificationType;

	public final int noradCatID;
	public final int elementSetNum;

	public final double revAtEpoch;

	public final double bStar;
	public final double bTerm;
	public final double meanMotionDot;
	public final double meanMotionDDot;
	public final double AGOM;

	public TLEParameters(int ephemerisType, String classificationType, int noradCatID, int elementSetNum,
						 double revNum, double bStar, double bTerm, double mm_dot, double mm_ddot, double AGOM, String comment) {
		this.ephemerisType = ephemerisType;
		this.classificationType = classificationType;
		this.noradCatID = noradCatID;
		this.elementSetNum = elementSetNum;
		this.revAtEpoch = revNum;
		this.bStar = bStar;
		this.bTerm = bTerm;
		this.meanMotionDot = mm_dot;
		this.meanMotionDDot = mm_ddot;
		this.AGOM = AGOM;
		this.comment = comment;
	}

	public static TLEParameters fromScannerOptional(Scanner s, OMMMetadata meta, String comment) {
		MatchResult eTypeMatch = Patterns.optionalKVN(s, "EPHEMERIS_TYPE", Patterns.INTEGER);
		int eType = (eTypeMatch == null ? 0 : Integer.parseInt(eTypeMatch.group("value")));
		MatchResult classTypeMatch = Patterns.optionalKVN(s, "CLASSIFICATION_TYPE", Patterns.NONDECIMALSTRING);
		String classType = (classTypeMatch == null ? "U" : classTypeMatch.group("value"));
		MatchResult noradCatMatch = Patterns.optionalKVN(s, "NORAD_CAT_ID", Patterns.INTEGER);
		int cat = 0;
		if (noradCatMatch != null) {
			cat = Integer.parseInt(noradCatMatch.group("value"));
		}
		else if (meta.meanElementTheory.equals("SGP") || meta.meanElementTheory.equals("SGP4")) {
			throw new InputMismatchException("\"NORAD_CAT_ID\" missing for "+meta.meanElementTheory+" model");
		}
		int setNo = 0;
		MatchResult setNoMatch = Patterns.optionalKVN(s, "ELEMENT_SET_NO", Patterns.INTEGER);
		if (setNoMatch != null) {
			setNo = Integer.parseInt(setNoMatch.group("value"));
		}
		double revNo = 0.0;
		MatchResult revNoMatch = Patterns.optionalKVN(s, "REV_AT_EPOCH", Patterns.NO_UNIT);
		if (revNoMatch != null) {
			revNo = Patterns.fromMatch(revNoMatch);
		}
		double bstar=0.0;
		double bterm=0.0;
		MatchResult bstarMatch = Patterns.optionalKVN(s, "BSTAR", Patterns.OPTIONALUNIT);
		MatchResult btermMatch = null;
		if (bstarMatch != null) {
			bstar = Patterns.fromMatch(bstarMatch);
		}
		else {
			btermMatch = Patterns.optionalKVN(s, "BTERM", Patterns.OPTIONALUNIT);
			if (btermMatch != null) {
				Patterns.checkUnit(btermMatch, "m**2/kg");
				bterm = Patterns.fromMatch(btermMatch);
			}
		}
		if (meta.meanElementTheory.equals("SGP4") && bstarMatch == null) {
			throw new InputMismatchException("\"BSTAR\" keyword was missing for SGP4 model");
		}
		else if (meta.meanElementTheory.equals("SGP4-XP") && btermMatch == null) {
			throw new InputMismatchException("\"BTErM\" keyword was missing for SGP4-XP model");
		}

		double mm_dot = 0.0;
		MatchResult mm_dotMatch = Patterns.optionalKVN(s, "MEAN_MOTION_DOT", Patterns.OPTIONALUNIT);
		if (mm_dotMatch != null) {
			Patterns.checkUnit(mm_dotMatch, "rev/day**2");
			mm_dot = Patterns.fromMatch(mm_dotMatch);
		}
		else if ((meta.meanElementTheory.equals("SGP") || meta.meanElementTheory.equals("PPT3"))) {
			throw new InputMismatchException("\"MEAN_MOTION_DOT\" keyword missing for " + meta.meanElementTheory + " model");
		}

		double mm_ddot=0.0;
		double agom = 0.0;
		MatchResult mm_ddotMatch = Patterns.optionalKVN(s, "MEAN_MOTION_DDOT", Patterns.OPTIONALUNIT);
		MatchResult agomMatch = null;
		if (mm_ddotMatch != null) {
			Patterns.checkUnit(mm_ddotMatch, "rev/day**3");
			mm_ddot = Patterns.fromMatch(mm_ddotMatch);
		}
		else if (meta.meanElementTheory.equals("SGP") || meta.meanElementTheory.equals("PPT3")) {
			throw new InputMismatchException("\"MEAN_MOTION_DDOT\" keyword missing for "+meta.meanElementTheory+" model");
		}
		else {
			agomMatch = Patterns.optionalKVN(s, "AGOM", Patterns.OPTIONALUNIT);
			if (agomMatch != null) {
				Patterns.checkUnit(agomMatch, "m**2/kg");
				agom = Patterns.fromMatch(agomMatch);
			}
		}
		if (meta.meanElementTheory.equals("SGP4-XP") && agomMatch == null) {
			throw new InputMismatchException("\"AGOM\" keyword missing for SGP4-XP model");
		}

		if (eTypeMatch == null && classTypeMatch == null && noradCatMatch == null && setNoMatch == null && 
				revNoMatch == null && bstarMatch == null && btermMatch == null && mm_dotMatch == null &&
				mm_ddotMatch == null && agomMatch == null) {
			return null;
		}
		return new TLEParameters(eType,classType,cat,setNo,revNo,bstar,bterm,mm_dot,mm_ddot,agom,comment);
	}
}
