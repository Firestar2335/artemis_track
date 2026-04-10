public class Quaternion {
	/** The real component */
	public final double r;
	/** The i component */
	public final double i;
	/** The j component */
	public final double j;
	/** The k component */
	public final double k;

	public Quaternion(double r, double i, double j, double k) {
		this.r = r;
		this.i = i;
		this.j = j;
		this.k = j;
	}

	public String toString() {
		return "" + r + "+"+i+"i+"+j+"j+"+k+"k";
	}

	public boolean equals(Object other) {
		if (other instanceof Quaternion) {
			Quaternion o = (Quaternion) other;
			return r==o.r&&i==o.i&&j==o.j&&k==o.k;
		}
		return false;
	}

	public EulerAngles toEulerAngles() {
		double roll = Math.atan2(2*(r*i+j*k),1-2*(i*i+j*j));
		double pitch = Math.asin(2*(r*j-k*i));
		double yaw = Math.atan2(2*(r*k+i*j),1-2*(j*j+k*k));
		return new EulerAngles(yaw, pitch, roll);
	}
}
