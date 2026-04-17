package ExtraMath;

public class Quaternion {
	private static final double EPSILON = 1e-9;

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

	/** The conjugate of this quaternion */
	private Quaternion conj;

	private Quaternion inv;

	/**
	 * Constructs the quaternion with the specified components
	 * @param r The real component
	 * @param i The i component
	 * @param j The j component
	 * @param k The k component
	 */
	public Quaternion(double r, double i, double j, double k) {
		this.r = r;
		this.i = i;
		this.j = j;
		this.k = k;
		inv = null;
		conj = new Quaternion(r, -i, -j, -k, this);
	}

	/**
	 * Creates a quaternion with the specified components and the specified conjugate
	 * @param r The real component
	 * @param i The i component
	 * @param j The j component
	 * @param k The k component
	 * @param conjugate The conjugate of this quaternion
	 */
	private Quaternion(double r, double i, double j, double k, Quaternion conjugate) {
		/*if (conjugate.r != r || conjugate.i != -i || conjugate.j != -j || conjugate.k != -k) {
			throw new IllegalArgumentException("The provided conjugate was not the actual conjugate");
		}*/
		this.r = r;
		this.i = i;
		this.j = j;
		this.k = k;
		inv = null;
		this.conj = conjugate;
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

	/**
	 * Constructs a rotation matrix that represents the same rotation as this quaternion
	 * @return
	 */
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
	 * Returns {@code -this}, or {@code this.mul(-1)}
	 * @return The additive inverse of this quaternion
	 */
	public Quaternion negate() {
		return new Quaternion(-r,-i,-j,-k);
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
	 * Computes the norm of this quaternion. 
	 * Equivalent to {@code Abs[q]} in Mathemateica.
	 * @return
	 */
	public double mag() {
		return Math.hypot(Math.hypot(r,i),Math.hypot(j,k));
	}

	/**
	 * Computes the square of the norm of this quaternion. 
	 * Equivalent to {@code Norm[q]} in  Mathematica
	 * @return
	 */
	public double magSquared() {
		double result = r*r;
		result = Math.fma(i,i,result);
		result = Math.fma(j,j,result);
		result = Math.fma(k,k,result);
		return result;
	}

	/**
	 * Returns the conjugate of this quaternion
	 * @return
	 */
	public Quaternion conjugate() {
		if (conj == null) {
			conj = new Quaternion(r,-i,-j,-k,this);
		}
		return conj;//return new Quaternion(r,-i,-j,-k);
	}

	/**
	 * Computes the reciprocal of this quaternion
	 * @return
	 */
	public Quaternion inverse() {
		if (inv == null) {
			inv = conjugate().div(magSquared());
		}
		return inv;
		//double m2 = magSquared();
		//return conjugate().div(m2);//return new Quaternion(r/m2,-i/m2,-j/m2,-k/m2);
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
	 * Returns the quaternion in the same direction of magnitude 1. 
	 * Equivalent to {@code Sign[q]} in Mathematica
	 * @return this scaled by 1/mag()
	 */
	public Quaternion unit() {
		return div(mag());
	}

	/**
	 * Computes the magnitude of the vector part of this quaternion. 
	 * Equivalent to {@code AbsIJK} in Mathematica.
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

	public Quaternion pow(int n) {
		if (n == 0) {
			return REAL_UNIT;
		}
		else if (n == 1) {
			return this;
		}
		else if (n < 0) {
			return pow(-n).inverse();
		}
		else if (n % 2 == 0) {
			Quaternion half = pow(n/2);
			return half.mul(half);
		}
		else {
			return mul(pow(n-1));
		}
	}

	/**
	 * Computes the result of raising {@code this} to the power of {@code x}
	 * @param x The power
	 * @return
	 */
	public Quaternion pow(double x) {
		if (ExtraMath.isInt(x)) {
			return pow((int) x);
		}
		double mag = mag();

		double phi = Math.acos(r/mag);
		double scale = Math.pow(mag,x);

		return fromVector(scale*Math.cos(x*phi),toUnitVector().mul(scale*Math.sin(x*phi)));
	}

	/**
	 * Round to the nearest quaternion of either all integer  components or all odd fractions of 2.
	 * This corresponds to {@code Round[q]} in Mathematica
	 * @return
	 */
	public Quaternion round() {
		//Integers: Round ties to half
		//Half-integers: round ties toward positive infinity
		double intR = Math.rint(r), intI = Math.rint(i), intJ = Math.rint(j), intK = Math.rint(k);
		double halfR = Math.round(r+0.5)-0.5;
		double halfI = Math.round(i+0.5)-0.5;
		double halfJ = Math.round(j+0.5)-0.5;
		double halfK = Math.round(k+0.5)-0.5;

		double distInt = (r-intR)*(r-intR)+(i-intI)*(i-intI)+(j-intJ)*(j-intJ)+(k-intK)*(k-intK);
		double distHalf = (r-halfR)*(r-halfR)+(i-halfI)*(i-halfI)+(j-halfJ)*(j-halfJ)+(k-halfK)*(k-halfK);

		if (distInt <= distHalf) {
			return new Quaternion(intR,intI,intJ,intK);
		}
		else {
			return new Quaternion(halfR,halfI,halfJ,halfK);
		}
	}

	/**
	 * Computes the result of the expression {@code this * vector * this^-1}
	 * @param vector
	 * @return
	 */
	public Vector3D conjugation(Vector3D vector) {
		return mul(vector).mul(inverse()).toVector();
		//return mul(vector).mul(conjugate()).toVector().div(magSquared());
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

	/**
	 * Constructs a quaternion that represents the same rotation as the provided rotation matrix
	 * @param mat
	 * @return
	 */
	public static Quaternion fromMatrix(Matrix mat) {
		if (mat.getNumRows() != 3 || mat.getNumColumns() != 3) {
			throw new IllegalArgumentException("The provided matrix was not a 3x3");
		}
		double n4 = 1 + mat.get(0,0) + mat.get(1,1) + mat.get(2,2);
		double n1 = 1 + mat.get(0,0) - mat.get(1,1) - mat.get(2,2);
		double n2 = 1 - mat.get(0,0) + mat.get(1,1) - mat.get(2,2);
		double n3 = 1 - mat.get(0,0) - mat.get(1,1) + mat.get(2,2);

		double max = Math.max(Math.max(n1,n2),Math.max(n3,n4));

		double r,i,j,k;

		if (max == n4) {
			r = Math.sqrt(n4)/2;
			i = (mat.get(2,1)-mat.get(1,2))/(4*r);
			j = (mat.get(0,2)-mat.get(2,0))/(4*r);
			k = (mat.get(1,0)-mat.get(0,1))/(4*r);
		}
		else if (max == n1) {
			i = Math.sqrt(n1)/2;
			j = (mat.get(1,0)+mat.get(0,1))/(4*i);
			k = (mat.get(2,0)+mat.get(0,2))/(4*i);
			r = (mat.get(2,1)-mat.get(1,2))/(4*i);
		}
		else if (max == n2) {
			j = Math.sqrt(n2)/2;
			i = (mat.get(0,1)+mat.get(1,0))/(4*j);
			k = (mat.get(2,1)+mat.get(1,2))/(4*j);
			r = (mat.get(0,2)-mat.get(2,0))/(4*j);
		}
		else {//max  == n3
			k = Math.sqrt(n3)/2;
			i = (mat.get(0,2)+mat.get(2,0))/(4*k);
			j = (mat.get(1,2)+mat.get(2,1))/(4*k);
			r = (mat.get(1,0)-mat.get(0,1))/(4*k);
		}
		return new Quaternion(r,i,j,k);
	}

	private static boolean isNegativeZero(double n) {
		return n == 0 && 1.0/n < 0;
	}

	/**
	 * Returns the quaternion that represents the same rotation but with the roll reversed
	 * @return
	 */
	public Quaternion invertRoll() {
		//double roll = Math.atan2(2*(r*i+j*k),1-2*(i*i+j*j));
		//The quaternion cos(roll/2)+sin(roll/2)i is the quaternion that does the roll
		//return mul(new Quaternion(Math.cos(roll),-Math.sin(roll),0,0));
		double y = 2*(r*i+j*k);
		double x = 1-2*(i*i+j*j);
		double s,c;
		//Getting the same edge cases as atan2
		if (Double.isNaN(x) || Double.isNaN(y)) {
			s = Double.NaN;
			c = Double.NaN;
		}
		else if (x == 0 && y == 0) {
			s = y;
			c = (isNegativeZero(y) ? -1 : 1);
		}
		else if (Double.isInfinite(x) && Double.isInfinite(y)) {
			double root2 = Math.sqrt(2)/2;
			s = Math.copySign(root2,y);
			c = Math.copySign(root2,x);
		}
		else if (Double.isInfinite(x)) {
			s = Math.copySign(0.0,y);
			c = Math.copySign(1.0,x);
		}
		else if (x == 0 || Double.isInfinite(y)) {
			s = Math.copySign(1.0,y);
			c = 0;
		}
		else {
			double h = Math.hypot(x,y);
			s = y/h;
			c = x/h;
		}
		return mul(new Quaternion(c,-s,0,0));
	}

	/**
	 * Returns a quaternion that provides the same rotation effect in the new basis. That is,
	 * if this quaternion represents a rotation that takes vector v to vector u, the quaternion 
	 * that is returned will take the representation of vector v in the new basis to the 
	 * representation of vector u in the new basis.
	 * @param newX
	 * @param newY
	 * @param newZ
	 * @return
	 */
	public Quaternion changeBasis(Vector3D newX, Vector3D newY, Vector3D newZ) {
		if (Math.abs(newX.dot(newY)) > EPSILON || Math.abs(newY.dot(newZ)) > EPSILON || Math.abs(newZ.dot(newX)) > EPSILON) {
			throw new IllegalArgumentException("The vectors were not orthogonal");
		}
		Matrix B = Matrix.fromVectorColumns(newX.unit(), newY.unit(), newZ.unit());
		if (newX.dot(newY.cross(newZ)) < 0) {
			B = B.mul(-1);
		}
		Quaternion c = fromMatrix(B);
		return c.conjugate().mul(mul(c));
	}

	/**
	 * 
	 * @param newX
	 * @param newY
	 * @param newZ
	 * @return
	 */
	public Quaternion changeAttitudeBasis(Vector3D newX, Vector3D newY, Vector3D newZ) {
		if (Math.abs(newX.dot(newY)) > EPSILON || Math.abs(newY.dot(newZ)) > EPSILON || Math.abs(newZ.dot(newX)) > EPSILON) {
			throw new IllegalArgumentException("The vectors were not orthogonal");
		}
		Matrix B = Matrix.fromVectorColumns(newX.unit(), newY.unit(), newZ.unit());
		//System.out.println(B);
		if (newX.dot(newY.cross(newZ)) < 0) {
			B = B.mul(-1);
		}
		//System.out.println(B);
		Quaternion c = fromMatrix(B);
		/*System.out.println(c);
		System.out.println(c.conjugate());
		System.out.println(c.conjugate().mul(this));
		System.out.println(mul(c));
		System.out.println(c.mul(this));
		System.out.println(mul(c.conjugate()));
		System.out.println(c.mul(mul(c.conjugate())));*/
		return c.conjugate().mul(this);
		//return mul(c);
		//return c.mul(this);
		//return c.conjugate().mul(conjugate());
	}
}
