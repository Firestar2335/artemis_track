package ExtraMath;
import java.math.BigDecimal;

/**
 * This is a 3 dimensional vector of BigDecimals
 */
public class Vector3D {
	/** Unit vector in the direction of the x-axis */
	public static final Vector3D X_UNIT = new Vector3D(1,0,0);
	/** Unit vector in the direction of the y-axis */
	public static final Vector3D Y_UNIT = new Vector3D(0,1,1);
	/** Unit vector in the direction of the z-axis */
	public static final Vector3D Z_UNIT = new Vector3D(0,0,1);

	public final double x;
	public final double y;
	public final double z;

	public Vector3D(BigDecimal x, BigDecimal y, BigDecimal z) {
		this.x = x.doubleValue();
		this.y = y.doubleValue();
		this.z = z.doubleValue();
	}

	public Vector3D(double x, double y, double z) {
		this.x=x;
		this.y=y;
		this.z=z;
	}

	public Vector3D(double[] comp) {
		if (comp.length != 3) {
			throw new IllegalArgumentException("Wrong number of components were provided");
		}
		x=comp[0];
		y=comp[1];
		z=comp[2];
	}

	public String toString() {
		return "<" + x + "," + y + "," + z + ">";
	}

	public boolean equals(Object other) {
		if (other instanceof Vector3D) {
			Vector3D o = (Vector3D) other;
			return x==o.x && y==o.y && z==o.z;
		}
		return false;
	}

	/**
	 * Performs {@code this * scalar}
	 * @param scalar The scalar to multiply by
	 * @return The result of the multiplication {@code this * scalar}
	 */
	public Vector3D mul(double scalar) {
		return new Vector3D(x*scalar, y*scalar, z*scalar);
	}

	/**
	 * Performs {@code this / scalar}
	 * @param scalar The scalar to divide by
	 * @return The result of the division {@code this / scalar}
	 */
	public Vector3D div(double scalar) {
		return new Vector3D(x/scalar,y/scalar,z/scalar);
	}

	/**
	 * Performs {@code this + other}
	 * @param other The vector to add
	 * @return The result of the addition {@code this + other}
	 */
	public Vector3D add(Vector3D other) {
		return new Vector3D(x+other.x,y+other.y,z+other.z);
	}

	/**
	 * Performs {@code this - other}
	 * @param other The vector to subtract
	 * @return the result of {@code this - other}
	 */
	public Vector3D sub(Vector3D other) {
		return new Vector3D(x-other.x,y-other.y,z-other.z);
	}

	/**
	 * Performs {@code this . other}
	 * @param other 
	 * @return The result of the dot product {@code this . other}
	 */
	public double dot(Vector3D other) {
		return x*other.x+y*other.y+z*other.z;
	}

	/**
	 * Computes the cross product of {@code this x other}
	 * @param other The vector to cross with
	 * @return The result of the cross product {@code this x other}
	 */
	public Vector3D cross(Vector3D other) {
		return new Vector3D(y*other.z-z*other.y,z*other.x-x*other.z,x*other.y-y*other.x);//Error: y*other.z
	}

	/**
	 * Computes the magnitude of this vector
	 * @return The magnitude of this vector, or {@code sqrt(x^2+y^2+z^2)}
	 */
	public double mag() {
		return Math.hypot(Math.hypot(x,y),z);
	}

	/**
	 * Computes the magnitude of this vector squared
	 * @return The square of the magnitude of the vector
	 */
	public double magSquared() {
		return dot(this);
	}

	/**
	 * Produces the unit vector in the direction of this vector
	 * @return A vector of magnitude 1 with the same direction as this vector.
	 */
	public Vector3D unit() {
		double mag = mag();
		return div(mag);
	}

	/**
	 * Returns the angle between this vector and {@code other} in radians
	 * @param other The other vector
	 * @return The angle between the two vectors in radians
	 */
	public double angle(Vector3D other) {
		return Math.acos(dot(other)/(mag()*other.mag()));
	}

	/**
	 * Returns the counter-clockwise angle starting at {@code this} and ending at {@code end},
	 * relative to the provided normal vector. The result is not necessarily positive
	 * @param end The ending vector of the angle
	 * @param normal The normal of the plane to reference the rotations to
	 * @return The clockwise angle from {@code this} to {@code end}
	 */
	public double angle(Vector3D end, Vector3D normal) {
		double ang = angle(end);
		if (normal.cross(this).dot(end) < 0) {
			ang *= -1;
		}
		return ang;
	}

	/**
	 * Returns an array of numbers that represents this vector in polar form. The numbers returned, 
	 * in order, are: 
	 * <ul>
	 * <li> r: the radial distance from this vector to the origin.
	 * <li> &#x03c6;: the azimuth; the angular distance clockwise from the x-axis to this vector 
	 * projected onto the XY plane
	 * <li> &#x03b8;: the elevation; the angle between this vector and the XY-plane
	 * </ul>
	 * @return The tuple (r,&#x03c6;,&#x03b8;)
	 */
	public double[] toPolarForm() {
		double r = mag();
		double azimuth = Math.atan2(y,x);
		double elevation = Math.atan2(z,Math.hypot(x,y));
		return new double[]{r,azimuth,elevation};
	}
}
