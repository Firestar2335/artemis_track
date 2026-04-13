package ODM;
import java.time.*;
import java.util.*;
import java.util.regex.*;

import ExtraMath.Vector3D;

public class KeplerElements {
	private static final double EPSILON = 1e-9;

	/** Gravitational constant in km³·kg&#x207B;¹·s&#x207B;²: {@value} km³/kg·s²*/
	public static final double G = 6.67430e-20;

	public final String comment;
	/** The epoch of these elements (t&#x2080;) */
	public final Instant epoch;

	/** Semimajor axis (a) in km, or mean motion (n) in rev/day */
	public final double sma;
	/** Eccentricity (e) */
	public final double ecc;
	/** Inclination (i), in degrees */
	public final double inc;
	/** Longitude/Right ascension of ascending node (&#x03a9;), in degrees */
	public final double raan;
	/** Argument of periapsis (&#x03c9;), in degrees */
	public final double arg;
	/** Mean anomaly (M&#x2080;) or true anomaly (&#x03bd;&#x2080;), in degrees */
	public final double anom;
	/** Standard gravitational parameter (&#x03bc;), in km**3/s**2 */
	public final double gm;

	/** Whether the stored anomaly is the true anomaly. If false, the stored anomaly is the mean anomaly */
	public final boolean isTrueAnomaly;

	public KeplerElements(Instant epoch, double semiMajorAxis, double eccentricity, double inclination,
						  double rightAsc, double argPC, double anomaly, boolean isTrueAnomaly, double gm) {
		this(epoch, semiMajorAxis, eccentricity, inclination, rightAsc, argPC, anomaly, isTrueAnomaly, gm, null);
	}

	public KeplerElements(Instant epoch, double semiMajorAxis, double eccentricity, double inclination,
						  double rightAsc, double argPC, double anomaly, boolean isTrueAnomaly, double gm, String comment) {
		this.epoch = epoch;
		sma = semiMajorAxis;
		ecc = eccentricity;
		inc = inclination;
		raan = rightAsc;
		arg = argPC;
		anom = anomaly;
		this.isTrueAnomaly = isTrueAnomaly;
		this.gm = gm;
		this.comment = comment;
	}

	/**
	 * Parses the keplerian elements stored in KVN notation, using the provided epoch. Throws an 
	 * exception if not all parameters are present.
	 * @param s The {@code Scanner} to read from
	 * @param epoch The epoch the data is referenced to
	 * @return The Keplerian elements stored at the current position in the scanner
	 */
	public static KeplerElements fromScanner(Scanner s, Instant epoch, String comment) {
		//MatchResult sma = Patterns.mandatoryKVN(s, "SEMI_MAJOR_AXIS", Patterns.OPTIONALUNIT);
		//Patterns.checkUnit(sma, "km");
		MatchResult sma = Patterns.optionalKVN(s, "SEMI_MAJOR_AXIS", Patterns.OPTIONALUNIT);
		if (sma == null) {
			sma = Patterns.mandatoryKVN(s, "MEAN_MOTION", Patterns.OPTIONALUNIT);
			Patterns.checkUnit(sma, "rev/day");
		}
		else {
			Patterns.checkUnit(sma, "km");
		}
		return finishParsing(s, epoch, sma, comment);
	}

	/**
	 * Parses an optional block of Keplerian elements from the scanner, returning null if the 
	 * block is not present.
	 * @param s
	 * @param epoch
	 * @return {@code null} if the block is not present, otherwise the parsed keplerian elements
	 */
	public static KeplerElements fromScannerOptional(Scanner s, Instant epoch, String comment) {
		MatchResult sma = Patterns.optionalKVN(s, "SEMI_MAJOR_AXIS", Patterns.OPTIONALUNIT);
		if (sma != null) {
			Patterns.checkUnit(sma, "km");
			return finishParsing(s, epoch, sma, comment);
		}
		else {
			sma = Patterns.optionalKVN(s, "MEAN_MOTION", Patterns.OPTIONALUNIT);
			if (sma != null) {
				Patterns.checkUnit(sma, "rev/day");
				return finishParsing(s, epoch, sma, comment);
			}
		}
		return null;
	}

	private static KeplerElements finishParsing(Scanner s, Instant epoch, MatchResult sma, String comment) {
		String ecc = Patterns.mandatoryKVN(s, "ECCENTRICITY", Patterns.NO_UNIT).group("value");
		MatchResult inc = Patterns.mandatoryKVN(s, "INCLINATION", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(inc, "deg");
		MatchResult ra = Patterns.mandatoryKVN(s, "RA_OF_ASC_NODE", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(ra, "deg");
		MatchResult arg = Patterns.mandatoryKVN(s, "ARG_OF_PERICENTER", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(arg, "deg");
		MatchResult anom = Patterns.optionalKVN(s, "TRUE_ANOMALY", Patterns.OPTIONALUNIT);
		boolean isTrue = true;
		if (anom == null) {
			anom = Patterns.mandatoryKVN(s, "MEAN_ANOMALY", Patterns.OPTIONALUNIT);
			isTrue = false;
		}
		Patterns.checkUnit(anom, "deg");
		MatchResult gm = Patterns.mandatoryKVN(s, "GM", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(gm, "km**3/s**2");
		return new KeplerElements(epoch, Patterns.fromMatch(sma), Double.parseDouble(ecc), Patterns.fromMatch(inc), 
										 Patterns.fromMatch(ra), Patterns.fromMatch(arg), Patterns.fromMatch(anom), isTrue, Patterns.fromMatch(gm), comment);
	}

	public String toString() {
		String result = "KeplerElements(";
		if (comment != null) {
			result += "comment=\"";
			result += comment;
			result += "\", ";
		}
		result += "a=";
		result += sma;
		result += " km, e=";
		result += ecc;
		result += ", i=";
		result += inc;
		result += "°, \u03a9=";
		result += raan;
		result += "°, \u03c9=";
		result += arg;
		result += "°, ";
		result += isTrueAnomaly ? "\u03bd" : "M";
		result += "\u2080=";
		result += anom;
		result += "°, \u03bc=";
		result += gm;
		result += "km³/s², t\u2080=";
		result += epoch.toString();
		result += ")";
		return result;
	}

	/**
	 * Returns the periapsis, in km
	 * @return the periapsis
	 */
	public double getPeriapsis() {
		return sma * (1 - ecc);
	}

	/**
	 * Calculates the apoapsis distance in km
	 * @return
	 */
	public double getApoapsis() {
		return sma * (1 + ecc);
	}

	/**
	 * Computes the orbital elements from the given state vector using the acceleration vector.
	 * @param state The state vector to compute the elements for
	 * @return The orbital elements corresponding to the given state vector
	 * @throws IllegalArgumentException if the provide state vector does not include an acceleration vector
	 */
	public static KeplerElements fromStateVector(StateVector state) {
		if (state.acc == null) {
			throw new IllegalArgumentException("Provided state vector did not have an acceleration vector, so elements could not be computed");
		}
		return fromStateVector(state, computeGravParam(state.pos,state.acc));
	}

	/**
	 * Computes the orbital elements for the given state and mass
	 * @param state The state vector to compute the orbital elements for
	 * @param mass The mass of the center body in kilograms
	 * @return The orbital elements corresponding to the provided state vector
	 */
	public static KeplerElements fromStateVectorAndMass(StateVector state, double mass) {
		return fromStateVector(state, mass * G);
	}

	/**
	 * Computes the orbital elements from the provided state vector and the standard gravitational parameter
	 * @param state The state vector
	 * @param stdGrav The standard gravitational parameter, in km**3/s**2
	 * @return The orbital elements corresponding to the current state vector
	 */
	public static KeplerElements fromStateVector(StateVector state, double stdGrav) {
		/*double r = state.pos.mag();
		Vector3D rHat = state.pos.unit();
		double vRadial = state.vel.dot(state.pos)/r;
		Vector3D velPerp = state.vel.sub(state.pos.mul(state.vel.dot(state.pos)/state.vel.dot(state.vel)));
		double vNormal = velPerp.mag();
		Vector3D tHat = velPerp.div(vNormal);

		// Semi-latus rectum
		double p = Math.pow(r * vNormal,2)/stdGrav;*/
		Vector3D angMoment = state.pos.cross(state.vel);
		Vector3D ascNodeVec = Vector3D.Z_UNIT.cross(angMoment);
		Vector3D eccVec = state.vel.cross(angMoment).div(stdGrav).sub(state.pos.unit());

		double ecc = eccVec.mag();
		double sma = (ecc == 1.0) ? Double.POSITIVE_INFINITY : angMoment.magSquared()/(stdGrav*(1-ecc*ecc));
		double inc = angMoment.angle(Vector3D.Z_UNIT);
		//double lan = Vector3D.X_UNIT.angle(ascNodeVec, Vector3D.Z_UNIT);
		double lan = Math.acos(ascNodeVec.x/ascNodeVec.mag());
		if (ascNodeVec.y < 0) {
			lan = -lan;
		}
		double arg = ascNodeVec.angle(eccVec);
		if (eccVec.z < 0) {
			arg = -arg;
		}
		double anom = eccVec.angle(state.pos);
		if (state.pos.dot(state.vel) < 0) {
			anom = -anom;
		}

		return new KeplerElements(state.epoch, sma, ecc, Math.toDegrees(inc),Math.toDegrees(lan),Math.toDegrees(arg),Math.toDegrees(anom), true, stdGrav);
	}

	/**
	 * Computes the standard gravitational parameter from the provided position and velocity vectors.
	 * @param pos
	 * @param acc
	 * @return
	 * @throws IllegalArgumentException if the provided vectors are not parallel and opposing
	 */
	private static double computeGravParam(Vector3D pos, Vector3D acc) {
		double aMag = acc.mag();
		double pMag = pos.mag();
		if (Math.abs(pos.dot(acc)/(aMag*pMag) + 1.0) > EPSILON) {
			throw new IllegalArgumentException("Position and Acceleration vectors were not opposing");
		}
		return aMag * (pMag * pMag);
	}
}
