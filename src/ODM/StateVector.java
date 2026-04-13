package ODM;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.regex.*;

import ExtraMath.Vector3D;
import cern.jet.math.Bessel;

public class StateVector {
	private static final long K_MAX = 100l;
	private static final long N_MIN = -100l;
	private static final long N_MAX = 100l;

	public final String comment;
	/** The timestamp for the data */
	public final Instant epoch;

	/** The position vector, in km */
	public final Vector3D pos;
	
	/** The velocity vector, in km/s */
	public final Vector3D vel;

	/** The acceleration vector, in km/s**2. This value is optional. If it is not provided, the value will be null. */
	public final Vector3D acc;

	public StateVector(Instant epoch, double x,      double y,      double z,
									  double x_dot,  double y_dot,  double z_dot,
									  double x_ddot, double y_ddot, double z_ddot) {
		this(epoch, BigDecimal.valueOf(x), BigDecimal.valueOf(y), BigDecimal.valueOf(z), BigDecimal.valueOf(x_dot), BigDecimal.valueOf(y_dot), BigDecimal.valueOf(z_dot), BigDecimal.valueOf(x_ddot), BigDecimal.valueOf(y_ddot), BigDecimal.valueOf(z_ddot));
	}

	public StateVector(Instant epoch, double x,      double y,      double z,
									  double x_dot,  double y_dot,  double z_dot,String comment) {
		this(epoch, BigDecimal.valueOf(x), BigDecimal.valueOf(y), BigDecimal.valueOf(z), BigDecimal.valueOf(x_dot), BigDecimal.valueOf(y_dot), BigDecimal.valueOf(z_dot), comment);
	}

	public StateVector(Instant epoch, BigDecimal x,      BigDecimal y,      BigDecimal z,
									  BigDecimal x_dot,  BigDecimal y_dot,  BigDecimal z_dot,
									  BigDecimal x_ddot, BigDecimal y_ddot, BigDecimal z_ddot) {
		this(epoch, x, y, z, x_dot, y_dot, z_dot, x_ddot, y_ddot, z_ddot, null);
	}

	public StateVector(Instant epoch, BigDecimal x,      BigDecimal y,      BigDecimal z,
									  BigDecimal x_dot,  BigDecimal y_dot,  BigDecimal z_dot,String comment) {
		this(epoch, x, y, z, x_dot, y_dot, z_dot, null, null, null, comment);
	}

	
	public StateVector(Instant epoch, BigDecimal x,      BigDecimal y,      BigDecimal z,
									  BigDecimal x_dot,  BigDecimal y_dot,  BigDecimal z_dot,
									  BigDecimal x_ddot, BigDecimal y_ddot, BigDecimal z_ddot, String comment) {
		this.comment = comment;
		if (x_ddot == null && y_ddot == null && z_ddot == null) {
			acc = null;
		}
		else if (x_ddot != null && y_ddot == null && z_ddot == null) {
			acc = new Vector3D(x_ddot, y_ddot, z_ddot);
		}
		else {
			throw new IllegalArgumentException("Not all of the acceleration components were null");
		}
		this.epoch = epoch;
		pos = new Vector3D(x,y,z);
		vel = new Vector3D(x_dot, y_dot, z_dot);
	}

	public StateVector(Instant epoch, Vector3D pos, Vector3D vel, Vector3D acc, String comment) {
		this.epoch = epoch;
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
		this.comment = comment;
	}

	public static StateVector fromString(String str, String timeSystem, String comment) {
		Scanner tokens = new Scanner(str);
		Instant epoch = Patterns.inTimeSystem(Patterns.parseTimestamp(tokens.next()),timeSystem);
		BigDecimal x = tokens.nextBigDecimal();
		BigDecimal y = tokens.nextBigDecimal();
		BigDecimal z = tokens.nextBigDecimal();
		BigDecimal x_dot = tokens.nextBigDecimal();
		BigDecimal y_dot = tokens.nextBigDecimal();
		BigDecimal z_dot = tokens.nextBigDecimal();
		BigDecimal x_ddot = tokens.hasNextBigDecimal() ? tokens.nextBigDecimal() : null;
		BigDecimal y_ddot = tokens.hasNextBigDecimal() ? tokens.nextBigDecimal() : null;
		BigDecimal z_ddot = tokens.hasNextBigDecimal() ? tokens.nextBigDecimal() : null;
		tokens.close();
		return new StateVector(epoch, x,y,z,x_dot,y_dot,z_dot,x_ddot,y_ddot,z_ddot, comment);
	}

	public static StateVector fromScanner(Scanner s, Instant epoch, String comment) {
		double x;
		double y;
		double z;
		double x_dot;
		double y_dot;
		double z_dot;

		MatchResult m = Patterns.mandatoryKVN(s, "X", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(m, "km");
		x = Patterns.fromMatch(m);
		m = Patterns.mandatoryKVN(s, "Y", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(m, "km");
		y = Patterns.fromMatch(m);
		m = Patterns.mandatoryKVN(s, "Z", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(m, "km");
		z = Patterns.fromMatch(m);

		m = Patterns.mandatoryKVN(s, "X_DOT", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(m, "km/s");
		x_dot = Patterns.fromMatch(m);
		m = Patterns.mandatoryKVN(s, "Y_DOT", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(m, "km/s");
		y_dot = Patterns.fromMatch(m);
		m = Patterns.mandatoryKVN(s, "Z_DOT", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(m ,"km/s");
		z_dot = Patterns.fromMatch(m);

		return new StateVector(epoch, x,y,z,x_dot,y_dot,z_dot, comment);
	}

	public String toString() {
		String result = "StateVector(";
		if (comment != null) {
			result += "comment=\"";
			result += comment;
			result += "\", ";
		}
		result += "pos=";
		result += pos.toString();
		result += " km, vel=";
		result += vel.toString();
		result += " km/s";
		if (acc != null) {
			result += ", acc=";
			result += acc.toString();
			result += " km/s²";
		}
		result += "@";
		result += epoch.toString();
		result += ")";
		return result;
	}

	public static StateVector fromElements(KeplerElements elem) {
		//double anom = elem.isTrueAnomaly ? elem.anom : getTrueAnomaly(elem);
		double T = elem.isTrueAnomaly ? Math.toRadians(elem.anom) : getTrueAnomaly(elem);
		double ta = T + Math.toRadians(elem.arg);
		double l = Math.toRadians(elem.raan);
		double i = Math.toRadians(elem.inc);
		double sta = Math.sin(ta);
		double cta = Math.cos(ta);
		double ci = Math.cos(i);
		double si = Math.sin(i);
		double d2 = sta * ci;
		double cl = Math.cos(l);
		double sl = Math.sin(l);
		double d3 = cta*ci;
		Vector3D radial = new Vector3D(cta*cl-d2*sl,cta*sl+d2*cl,sta*si);
		Vector3D tangent = new Vector3D(-sta*cl-d3*sl,-sta*sl+d3*cl,cta*si);

		double cT=Math.cos(T);
		double radius = elem.sma * (1-elem.ecc*elem.ecc)/(1+elem.ecc*cT);
		double k = Math.sqrt(elem.gm/(elem.sma*(1-elem.ecc*elem.ecc)));
		double radialVelocity = elem.ecc*Math.sin(T)*k;
		double tangentialVelocity = (1+elem.ecc*cT)*k;

		Vector3D pos = radial.mul(radius);
		Vector3D vel = radial.mul(radialVelocity).add(tangent.mul(tangentialVelocity));
		Vector3D acc = radial.mul(-elem.gm/(radius*radius));
		return new StateVector(elem.epoch, pos, vel, acc, null);
	}

	/**
	 * Computes the true anomaly &#x03bd in radians; from the mean anomaly in degrees in the 
	 * provided elements
	 * @param elems
	 * @return
	 */
	public static double getTrueAnomaly(KeplerElements elems) {
		System.out.println("converting");
		if (elems.isTrueAnomaly) {
			System.err.println("Warning: Elements provided to getTrueAnomaly had the true anomay instead of the mean anomaly");
			return Math.toRadians(elems.anom);
		}
		double beta = (1-Math.sqrt(1-elems.ecc * elems.ecc))/elems.ecc;
		double M = Math.toRadians(elems.anom);
		double sum = 0.0;
		for (long k = 1l; k <= K_MAX; k++) {
			double innerSum = 0.0;
			for (long n = N_MIN; n <= N_MAX; n++) {
				innerSum = Math.fma(Bessel.jn((int)n,-k*elems.ecc),Math.pow(beta,Math.abs(k+n)),innerSum);
			}
			sum = Math.fma(Math.sin(k*M),innerSum/k, sum);
		}
		return M + sum;
	}
}