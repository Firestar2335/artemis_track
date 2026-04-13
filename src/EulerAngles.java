import ExtraMath.Quaternion;

public class EulerAngles {
	/** The yaw in radians */
	public final double yaw;
	/** The pitch in radians */
	public final double pitch;
	/** The roll in radians */
	public final double roll;

	public EulerAngles(double yaw, double pitch, double roll) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
	}

	public String toString() {
		return String.format("EulerAngles(%f, %f, %f)", yaw, pitch, roll);
	}

	public boolean equals(Object other) {
		if (other instanceof EulerAngles) {
			EulerAngles o = (EulerAngles) other;
			return yaw==o.yaw&&pitch==o.pitch&&roll==o.roll;
		}
		return false;
	}

	public static EulerAngles fromQuaternion(Quaternion q) {
		double roll = Math.atan2(2*(q.r*q.i+q.j*q.k),1-2*(q.i*q.i+q.j*q.j));
		double pitch = Math.asin(2*(q.r*q.j-q.k*q.i));
		double yaw = Math.atan2(2*(q.r*q.k+q.i*q.j),1-2*(q.j*q.j+q.k*q.k));
		return new EulerAngles(yaw, pitch, roll);
	}
}
