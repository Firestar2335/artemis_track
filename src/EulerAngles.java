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
}
