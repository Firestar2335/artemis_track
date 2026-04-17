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
		double pitch = Math.asin(Math.clamp(2*(q.r*q.j-q.k*q.i),-1,1));
		double yaw = Math.atan2(2*(q.r*q.k+q.i*q.j),1-2*(q.j*q.j+q.k*q.k));
		return new EulerAngles(yaw, pitch, roll);
	}

	public Quaternion toQuaternion() {
		double sinYaw = Math.sin(yaw/2), cosYaw = Math.cos(yaw/2);
		double sinPitch = Math.sin(pitch/2), cosPitch = Math.cos(pitch/2);
		double sinRoll = Math.sin(roll/2), cosRoll = Math.cos(roll/2);
		double r = cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw;
		double i = sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw;
		double j = cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw;
		double k = cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw;
		return new Quaternion(r,i,j,k);
	}
}
