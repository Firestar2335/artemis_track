package ODM;

import java.time.*;

import ExtraMath.Vector3D;

public class Segment {
	public final OEMMetadata metadata;
	public final EphemerisBlock ephemerides;
	public final CovarianceBlock covariance;

	public Segment(OEMMetadata metadata, EphemerisBlock ephemerides, CovarianceBlock covariance) {
		this.metadata = metadata;
		this.ephemerides = ephemerides;
		this.covariance = covariance;
	}

	public String toString() {
		String result = "Segment(metadata";
		result += metadata.toString();
		result += ", ";//ephemerides=
		result += ephemerides.toString();
		if (covariance != null) {
			result += ", ";//covariance=";
			result += covariance.toString();
		}
		result += ")";
		return result;
	}

	public StateVector interpolate(Instant t) {
		switch (metadata.interpolation) {
			case null:
				System.err.println("Interpolation method was not given. Using linear intepolation");
			case "LINEAR":
				return interpolateLinear(t);

			default:
				System.err.println("Interpolation method \""+metadata.interpolation+"\" not recognized. Using linear interpolation");
				return interpolateLinear(t);
		}
	}

	private StateVector interpolateLinear(Instant t) {
		int floorIndex = ephemerides.search(t);
		StateVector before = ephemerides.get(floorIndex);
		if (before.epoch.equals(t)) {
			return before;
		}
		StateVector after = ephemerides.get(floorIndex+1);

		double k = getFrac(before.epoch, t, after.epoch);
		Vector3D pos = before.pos.mul(1-k).add(after.pos.mul(k));
		Vector3D vel = before.vel.mul(1-k).add(after.vel.mul(k));
		Vector3D acc = null;
		if (before.acc != null && after.acc != null) {
			acc = before.acc.mul(1-k).add(after.acc.mul(k));
		}
		return new StateVector(t,pos,vel,acc,null);
	}

	/**
	 * Computes the fractional progress of {@code mid} to {@code end} starting at {@code start}
	 * @param start
	 * @param mid
	 * @param end
	 * @return The result of {@code (mid - start) / (end - start)}
	 */
	private static double getFrac(Instant start, Instant mid, Instant end) {
		Duration d1 = start.until(mid);
		Duration d2 = start.until(end);
		return (d1.getSeconds() + d1.getNano()/1_000_000_000.0) / (d2.getSeconds() + d2.getNano()/1_000_000_000.0);
	}

	/*private static boolean willOverflowNano(Duration dur) {
		//1_000_000_000 * sec + nano > Long.MAX_VALUE
		//sec > (Long.MAX_VALUE-nano)/1_000_000_000
		//1_000_000_000 * sec + nano < Long.MIN_VALUE
		//sec < Long.MIN_VALUE/1_000_000_000 - nano/1_000_000_000
		long s = dur.getSeconds();
		int n = dur.getNano();
		if (s >= 0) {
			return s > (Long.MAX_VALUE - n) / 1_000_000_000;
		}
		else {
			//return s < Long.MIN_VALUE/1_000_000_000 - n/1_000_000_000;
			return s < Long.MIN_VALUE/1_000_000_000;
		}
	}*/
}
