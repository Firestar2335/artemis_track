package ExtraMath;

public class Quaternion {
	/** The zero quaternion */
	public static final Quaternion ZERO = new Quaternion(0,0,0,0);
	/** The i basis quaternion */
	public static final Quaternion I_UNIT = new Quaternion(0,1,0,0);
	/** The j basis quaternion */
	public static final Quaternion J_UNIT = new Quaternion(0,0,1,0);
	/** The k basis quaternion */
	public static final Quaternion K_UNIT = new Quaternion(0,0,0,1);
	/** The real basis quaternion */
	public static final Quaternion REAL_UNIT = new Quaternion(1,0,0,0);

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

	private double[][] toRotationMatrix() {
		double[][] mat = new double[3][3];
		double s = 1/magSquared();
		mat[0][0] = 1-2*s*(j*j+k*k); mat[0][1] = 2*s*(i*j-k*r); mat[0][2] = 2*s*(i*k+j*r);
		mat[1][0] = 2*s*(i*j+k*r); mat[1][1] = 1-2*s*(i*i+k*k); mat[1][2] = 2*s*(j*k-i*r);
		mat[2][0] = 2*s*(i*k-j*r); mat[2][1] = 2*s*(j*k+i*r); mat[2][2] = 1-2*s*(i*i+j*j);
		return mat;
	}

	public Matrix toMatrix() {
		return new Matrix(toRotationMatrix());
	}

	/**
	 * Multiplies this quaternion by the provided scalar
	 * @param scalar
	 * @return
	 */
	public Quaternion mul(double scalar) {
		return new Quaternion(r*scalar,i*scalar,j*scalar,k*scalar);
	}

	/**
	 * Performs the multiplication of {@code this * other}
	 * @param other
	 * @return
	 */
	public Quaternion mul(Quaternion other) {
		double newR = r*other.r-i*other.i-j*other.j-k*other.k;
		double newI = r*other.i+i*other.r+j*other.k-k*other.j;
		double newJ = r*other.j-i*other.k+j*other.r+k*other.i;
		double newK = r*other.k+i*other.j-j*other.i+k*other.r;
		return new Quaternion(newR, newI, newJ, newK);
	}

	/**
	 * Performs the result of the multiplication {@code this * vector}, interpreting the vector as 
	 * a quaternion with a real part of 0.
	 * @param vector
	 * @return
	 */
	public Quaternion mul(Vector3D vector) {
		double newR = -i*vector.x-j*vector.y-k*vector.z;
		double newI = r*vector.x+j*vector.z-k*vector.y;
		double newJ = r*vector.y-i*vector.z+k*vector.x;
		double newK = r*vector.z+i*vector.y-j*vector.x;
		return new Quaternion(newR, newI, newJ, newK);
	}

	/**
	 * Performs the multiplication {@code vector * this}, interpreting the vector as a quaternion 
	 * with a real component of 0.
	 * @param vector
	 * @return
	 */
	public Quaternion rmul(Vector3D vector) {
		double newR = -vector.x*i-vector.y*j-vector.z*k;
		double newI = vector.x*r+vector.y*k-vector.z*j;
		double newJ = -vector.x*k+vector.y*r+vector.z*i;
		double newK = vector.x*j-vector.y*i+vector.z*r;
		return new Quaternion(newR, newI, newJ, newK);
	}

	/**
	 * Divides this quaternion by the provided scalar
	 * @param scalar
	 * @return
	 */
	public Quaternion div(double scalar) {
		return new Quaternion(r/scalar,i/scalar,j/scalar,k/scalar);
	}

	/**
	 * Performs the addition {@code this + other}
	 * @param other
	 * @return
	 */
	public Quaternion add(Quaternion other) {
		return new Quaternion(r+other.r,i+other.i,j+other.j,k+other.k);
	}

	/**
	 * Performs the subtraction {@code this - other}
	 * @param other
	 * @return
	 */
	public Quaternion sub(Quaternion other) {
		return new Quaternion(r-other.r,i-other.i,j-other.j,k-other.k);
	}

	/**
	 * Computes the norm of this quaternion
	 * @return
	 */
	public double mag() {
		return Math.hypot(Math.hypot(r,i),Math.hypot(j,k));
	}

	/**
	 * Computes the square of the norm of this quaternion
	 * @return
	 */
	public double magSquared() {
		return r*r+i*i+j*j+k*k;
	}

	/**
	 * Returns the conjugate of this quaternion
	 * @return
	 */
	public Quaternion conjugate() {
		return new Quaternion(r,-i,-j,-k);
	}

	/**
	 * Computes the reciprocal of this quaternion
	 * @return
	 */
	public Quaternion inverse() {
		double m2 = magSquared();
		return new Quaternion(r/m2,-i/m2,-j/m2,-k/m2);
	}

	/**
	 * Extracts the vector part from this quaternion
	 * @return
	 */
	public Vector3D toVector() {
		return new Vector3D(i,j,k);
	}

	/**
	 * Extracts the vector part from this quaternion and converts it to a unit vector
	 * @return
	 */
	public Vector3D toUnitVector() {
		double vecMag = magVector();
		return new Vector3D(i/vecMag,j/vecMag,k/vecMag);
	}

	/**
	 * Extracts the vector part of this quaternion as a quaternion
	 * @return
	 */
	public Quaternion getVectorPart() {
		return new Quaternion(0,i,j,k);
	}

	/**
	 * Determmines whether this quaternion is a vector quaternion
	 * @return
	 */
	public boolean isVector() {
		return r == 0;
	}

	/**
	 * Determines whether this quaternion is a scalar quaternion
	 * @return
	 */
	public boolean isScalar() {
		return i==0 && j==0 && k == 0;
	}

	/**
	 * Returns the quaternion in the same direction of magnitude 1
	 * @return this scaled by 1/mag()
	 */
	public Quaternion unit() {
		return div(mag());
	}

	/**
	 * Computes the magnitude of the vector part of this quaternion
	 * @return
	 */
	public double magVector() {
		return Math.hypot(Math.hypot(i,j),k);
	}

	/**
	 * Returns a quaternion {@code v} such that {@code v*v=this}
	 * @return
	 */
	public Quaternion sqrt() {
		double m = mag();
		return fromVector(Math.sqrt((m+r)/2),toUnitVector().mul(Math.sqrt((m-r)/2)));
	}

	/**
	 * Computes the result of {@code exp(this)}
	 * @return
	 */
	public Quaternion exp() {
		double scale = Math.exp(r);
		double vecMag = magVector();

		return fromVector(scale*Math.cos(vecMag),toUnitVector().mul(scale*Math.sin(magVector())));
	}

	/**
	 * Computes the natural logarithm of {@code this}
	 * @return
	 */
	public Quaternion log() {
		double mag = mag();
		return fromVector(Math.log(mag),toUnitVector().mul(Math.acos(r/mag)));
	}

	/**
	 * Computes the result of raising {@code this} to the power of {@code x}
	 * @param x The power
	 * @return
	 */
	public Quaternion pow(double x) {
		double mag = mag();

		double phi = Math.acos(r/mag);
		double scale = Math.pow(mag,x);

		return fromVector(scale*Math.cos(x*phi),toUnitVector().mul(scale*Math.sin(x*phi)));
	}

	/**
	 * Computes the result of the expression {@code this * vector * this^-1}
	 * @param vector
	 * @return
	 */
	public Vector3D conjugation(Vector3D vector) {
		return mul(vector).mul(inverse()).toVector();
	}

	/**
	 * Computes the quaternion representing a counterclockwise rotation about the given axis
	 * @param angle The counterclockwise angle, in radians
	 * @param axis The axis of rotation
	 * @return
	 */
	public static Quaternion forRotation(double angle, Vector3D axis) {
		return fromVector(Math.sin(angle/2),axis.unit().mul(Math.sin(angle/2)));
	}

	/**
	 * Returns the quaternion with the specified real part and 0 imaginary part
	 * @param real
	 * @return
	 */
	public static Quaternion fromReal(double real) {
		return new Quaternion(real,0,0,0);
	}

	/**
	 * Transforms the provided vector into a quaternion with 0 real part
	 * @param vector
	 * @return
	 */
	public static Quaternion fromVector(Vector3D vector) {
		return fromVector(0.0,vector);
	}

	/**
	 * Returns the quaternion with the vector part of the provided vector and the provided real part
	 * @param vector The vector part of the quaternion
	 * @param real The real part of the quaternion
	 * @return
	 */
	public static Quaternion fromVector(double real, Vector3D vector) {
		return new Quaternion(real,vector.x,vector.y,vector.z);
	}
}
