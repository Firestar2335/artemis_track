package ExtraMath;

//import java.util.Arrays;
import java.math.*;

/**
 * This contains extra math functions;
 */
public class ExtraMath {
	@SuppressWarnings("unused")
	private static final double[] BESSEL_ZERO_ZEROS = new double[]{2.404825,5.520078,8.8653727,11.791534,14.930917,18.071063,21.211636,24.352471,27.493479,27.493479,30.634606,33.775820,36.917098,40.058425,43.199791,46.341188,49.482609,52.624051,55.765510,58.906983,62.048469,65.189964,68.331469,71.472981,74.614500};
	@SuppressWarnings("unused")
	private static final double[] BESSEL_ONE_ZEROS = new double[]{0,3.831705,7.015586,10.173468,13.323691,16.470630,19.615858,22.760084,25.903672,29.0468828,32.189679,35.332307,38.474766,41.617094,44.759318,47.901460,51.043535,54.185553,57.327525,60.469457,63.611356,66.753226,69.895071,73.036895};

	public static final BigDecimal PI = new BigDecimal("3.14159265358979323846264338327950288419716939937510582097494459230782");

	public static final BigDecimal SQRT_PI = new BigDecimal("1.77245385090551602729816748334114518279754945612238712821380778985291");

	//private static final BigDecimal K = BigDecimal.TWO.divide(PI,MathContext.DECIMAL128).sqrt(MathContext.DECIMAL128);
	private static final BigDecimal K = new BigDecimal("0.79788456080286535587989211986876373695171726232986931533185165934132");//Taken from mathematica

	private static BigDecimal[] LANCZOS_COEFS;

	private static double G = 9;

	private static final long BESSEL_ITERATIONS = 100l;

	private static final MathContext EXP_MC = new MathContext(68,RoundingMode.HALF_EVEN);


	public static long binomial(int n, int k) {
		if (n < 0) {
			return (k%2==0 ? 1l : -1l) * binomial(k-1-n,k);
		}
		else if (k < 0 || k > n) {
			return 0l;
		}
		else if (k == 0 || k == n) {
			return 1l;
		}
		else {
			return binomial(n-1,k-1)+binomial(n-1,k);
		}
	}

	public static int chebyshev(int n, int m) {
		if (n <= 0 || m <= 0) {
			return 0;
		}
		if (n == 1 && m == 1) {
			return 1;
		}
		else if (n ==2 && m == 2) {
			return 1;
		}
		else if (n == m) {
			return 2 * chebyshev(n-1,n-1);
		}
		else if (m == 1 && n >= 3) {
			return -chebyshev(n-2,1);
		}
		else if (n > m) {
			return 2 * chebyshev(n-1,m-1) - chebyshev(n-2,m);
		}
		else {
			return 0;
		}
	}

	/**
	 * Computes the bessel function of the first kind
	 * @param order
	 * @param x
	 * @return
	 */
	public static double besselFunction(double order, double x) {
		if (isInt(order) && order < 0) {
			if (order < 0) {
				return ((-order)%2 == 0 ? 1 : -1) * besselFunction(x,(int) -order,BESSEL_ITERATIONS);
			}
			return besselFunction((int) order, x,BESSEL_ITERATIONS);
		}
		return besselFunction(order, x, BESSEL_ITERATIONS);
	}

	private static double besselFunction(int order, double x, long iterations) {
		double accum = 0.0;
		for (long m = 0l; m < iterations; m++) {
			accum += (m%2==0 ? 1 : -1) * new BigDecimal(Math.pow(x/2.0,order+2*m)).divide(new BigDecimal(factorialBig(m).multiply(factorialBig(order+m))),MathContext.DECIMAL128).doubleValue();
		}
		return accum;
	}

	//Implementations from https://www.cl.cam.ac.uk/~jrh13/papers/bessel.pdf
	

	private static double besselFunction(double order, double x, long iterations) {
		double accum = 0.0;
		BigDecimal num;
		BigDecimal den;
		for (long m = 0l; m < iterations; m++) {
			num = new BigDecimal((m%2==0 ? 1 : -1) * Math.pow(x/2.0,2*m+order));
			den = new BigDecimal(factorialBig(m)).multiply(new BigDecimal(gammaFunction(m+order+1)));
			System.out.println(den);
			accum += num.divide(den,MathContext.DECIMAL128).doubleValue();
			
		}
		return accum;
	}

	public static double gammaFunction(double z) {
		if (isInt(z)) {
			if (z == 0) {
				return Double.POSITIVE_INFINITY;
			}
			else if (z < 0) {
				throw new IllegalArgumentException("The gamma function is undefined for negative integers");
				//return Double.NaN
			}
			return factorial((long)z-1);
		}
		else if (isInt(2*z)) {
			if (z > 0) {
				int k = (int) (2 * z);
				if (k-2 >= 34) {
					return gammaFunction(z-1)*(z-1);
				}
				return doubleFactorial((long)(k-2)) * Math.sqrt(Math.PI) / Math.pow(2.0,(k-1.0)/2.0);
			}
			else {
				int n = (int) (0.5 -z);
				if (2*n-1 >= 34) {
					return gammaFunction(z+1)/z;
				}
				return Math.sqrt(Math.PI) * Math.pow(-2,n) / doubleFactorial((long) (2*n-1));
			}
		}
		if (z +G+0.5 <= 0) {
			return Math.PI/(Math.sin(Math.PI*z)*gammaFunction(1-z));
		}
		return lanczos(z-1);
	}

	public static BigDecimal gammaFunctionBig(double z) {
		if (isLong(z)) {
			if (z == 0) {
				throw new IllegalArgumentException("Gamma function is undefined at zero");//return Double.POSITIVE_INFINITY;
			}
			else if (z < 0) {
				throw new IllegalArgumentException("The gamma function is undefined for negative integers");
				//return Double.NaN
			}
			return new BigDecimal(factorialBig((long)z-1));
		}
		else if (isLong(2*z)) {
			if (z > 0) {
				long k = (long) (2 * z);
				if (k-2 >= 34) {
					return gammaFunctionBig(z-1).multiply(new BigDecimal(z-1));
				}
				return SQRT_PI.multiply(new BigDecimal(doubleFactorialBig((long)k - 2))).divide( BigDecimal.valueOf(Math.pow(2.0,(k-1)/2)));
			}
			else {
				long n = (long) (0.5 -z);
				if (2*n-1 >= 34) {
					return gammaFunctionBig(z+1).divide(new BigDecimal(z),MathContext.DECIMAL128);
				}
				return SQRT_PI.multiply(new BigDecimal(Math.pow(-2,n)).divide(new BigDecimal(doubleFactorialBig(2*n-1)),MathContext.DECIMAL128));
			}
		}
		if (z +G+0.5 <= 0) {
			return PI.divide(gammaFunctionBig(1-z).multiply(new BigDecimal(Math.sin(Math.PI*z))),MathContext.DECIMAL128);
		}
		return lanczosBig(z-1);
	}

	/*private static double gammaFunctionA(double z) {
		if (z < 1) {
			return gammaFunctionA(z+1)/z;
		}
		else if (z > 2) {
			return gammaFunctionA(z-1)*(z-1);
		}
		return 0;
	}*/

	private static double lanczos(double z) {
		if (LANCZOS_COEFS == null) {
			initCoefs(9,11);
		}
		BigDecimal s = LANCZOS_COEFS[0];
		for (int i = 1; i < LANCZOS_COEFS.length; i++) {
			//System.out.println(s);
			s = s.add(LANCZOS_COEFS[i].divide(new BigDecimal(z+i),EXP_MC));
		}
		return s.doubleValue()*Math.sqrt(Math.TAU)*Math.pow(z+G+0.5,z+0.5)*Math.exp(-z-G-0.5);
		/*double s = LANCZOS_COEFS[0].doubleValue();
		for (int i = 1; i < LANCZOS_COEFS.length; i++) {
			s += LANCZOS_COEFS[i].doubleValue()/(z+i);
		}
		return s;*/
	}
	private static BigDecimal lanczosBig(double z) {
		if (LANCZOS_COEFS == null) {
			initCoefs(9,11);
		}
		BigDecimal s = LANCZOS_COEFS[0];
		for (int i = 1; i < LANCZOS_COEFS.length; i++) {
			//System.out.println(s);
			s = s.add(LANCZOS_COEFS[i].divide(new BigDecimal(z+i),EXP_MC));
		}
		return s.multiply(new BigDecimal(Math.pow(z+G+0.5,z+0.5)*Math.exp(-z-G-0.5)*Math.sqrt(Math.TAU)),MathContext.DECIMAL128);
	}


	/**
	 * Computes the result of z * (z+1) * (z+2) * ... * (z+n)
	 * @param z
	 * @param n
	 * @return
	 */
	public static double iterateMultiplication(double z, long n) {
		double r = 1.0;
		while (n >= 0) {
			r *= z+n--;
		}
		return r;
	}

	public static BigInteger factorialBig(long x) {
		return x <= 1 ? BigInteger.ONE : BigInteger.valueOf(x--).multiply(factorialBig(x));
	}

	public static int factorial(int x) {
		return x<=1 ? 1 : x-- * factorial(x);//return (x==0 || x==1) ? 1 : x * factorial(x-1);
	}

	public static long factorial(long x) {
		return x<=1l ? 1l : x-- * factorial(x);//return (x==0l || x==1l) ? 1l : x * factorial(x-1l);
	}

	public static int doubleFactorial(int x) {
		int prod = 1;
		while (x > 0) {
			prod *= x;
			x -= 2;
		}
		return prod;
	}

	public static long doubleFactorial(long x) {
		long prod = 1l;
		while (x > 0) {
			prod *= x;
			x -= 2l;
		}
		return prod;
	}

	public static BigInteger doubleFactorialBig(long x) {
		BigInteger prod = BigInteger.ONE;
		while (x > 0) {
			prod = prod.multiply(BigInteger.valueOf(x));
			x -= 2l;
		}
		return prod;
	}

	/*public static BigDecimal expOcto(BigDecimal n) {
		return exp(n, EXP_MC, EXP_ITERATIONS);
	}*/

	public static BigDecimal exp(BigDecimal n) {
		return exp(n, EXP_MC).round(MathContext.DECIMAL128);
	}
	public static BigDecimal exp(double n) {
		return exp(new BigDecimal(n));
	}

	private static BigDecimal exp(BigDecimal n, MathContext context) {
		BigDecimal sum = BigDecimal.ONE;
		BigDecimal inter;
		BigDecimal fact;
		BigDecimal pow;
		long i = 1;
		while (true) {
		//for (long i = 1; i < iters; i++) {
			if (i <= 20) {
				fact = BigDecimal.valueOf(factorial(i));
			}
			else {
				fact = new BigDecimal(factorialBig(i));
			}
			pow = n.pow((int) i);
			try {
				inter = pow.divide(fact);
			}
			catch (ArithmeticException e) {
				inter = pow.divide(fact, context);
			}
			sum = sum.add(inter);
			//System.out.println(inter);
			if (inter.abs().compareTo(sum.round(context).ulp().abs().scaleByPowerOfTen(-5)) < 0) {
				//System.out.println(inter);
				break;
			}
			i++;
		}
		return sum;//.round(context);
	}

	public static boolean isInt(double n) {
		return n == (int) n;
	}

	public static boolean isLong(double n) {
		return n == (long) n;
	}

	public static int stirlingNumber(int n, int k) {
		if (n == 0 && k == 0) {
			return 1;
		}
		else if (n == 0 || k == 0) {
			return 0;
		}
		return -(n-1) * stirlingNumber(n-1,k) + stirlingNumber(n-1,k-1);
	}

	//private static double p(int k, double g) {
	//	double accum = 0.0;
	//	for (int l = 0; l <= k; l++) {
	//		accum += chebyshev(2*k+1,2*l+1) * gammaFunction(l+0.5)*Math.pow(l+g+0.5,-l-0.5)*Math.exp(l+g+0.5);
	//	}
	//	return accum * Math.sqrt(2.0) / Math.PI;
	//}

	public static double calcFg(double g, int a) {
		return Math.sqrt(2/Math.PI) * (doubleFactorial(2*a-1) * Math.exp(a + g + 0.5) / (Math.pow(2,a)*Math.pow(a+g+0.5,a+0.5)));
	}

	public static BigDecimal calcFgBig(double g, int a) {
		BigDecimal p = new BigDecimal(g+a+0.5);
		return K.multiply(exp(a+g+0.5).multiply(BigDecimal.valueOf(doubleFactorial(2*a-1)).divide(p.pow(a).multiply(p.sqrt(MathContext.DECIMAL128)).multiply(new BigDecimal(Math.powExact(2,a))),MathContext.DECIMAL128)));
	}

	public static void initCoefs(double g, int len) {
		G = g;
		/*double[] FgMat = new double[len];
		double k = Math.sqrt(2/Math.PI);
		for (int a = 0; a < len; a++) {
			//FgMat[a] = calcFg(g,a);
			FgMat[a] = k * (doubleFactorial(2*a-1) * Math.exp(a + g + 0.5) / (Math.pow(2.0, a) * Math.pow(a+g+0.5, a+0.5)));
			System.out.println(FgMat[a]);
			System.out.println(calcFgBig((int)g,a));
		}
			Matrix f = Matrix.fromColumnVector(FgMat);*/
		BigDecimal[] FgMat = new BigDecimal[len];
		for (int a = 0; a < len; a++) {
			FgMat[a] = calcFgBig(g,a);
		}
		BigVector f = new BigVector(FgMat);
		double[][] CMat = new double[len][len];
		CMat[0][0] = 0.5;
		for (int r = 1; r < len; r++) {
			for (int c = 0; c <= r; c++) {
				CMat[r][c] = chebyshev(2*r+1,2*c+1);
			}
		}
		Matrix C = new Matrix(CMat);
		long[] DMat = new long[len];
		DMat[0] = 1;
		for (int i = 1; i < len; i++) {
			DMat[i] = -i * binomial(2*i-1,i);
		}
		Matrix D = Matrix.fromDiagonal(DMat);

		double[][] BMat = new double[len][len];
		for (int c = 0; c < len; c++) {
			BMat[0][c] = 1;
		}
		for (int r = 1; r < len; r++) {
			for (int c = r; c < len; c++) {
				BMat[r][c] = (((r+c) % 2 == 0) ? 1 : -1) * binomial(c+(r-1),2*r-1);
			}
		}
		Matrix B = new Matrix(BMat);
		Matrix mid = D.mul(B).mul(C);
		BigVector c = mid.mulExtended(f);
		/*System.out.println("f: ");
		System.out.println(f);
		System.out.println("C:");
		System.out.println(C);
		System.out.println("D:");
		System.out.println(D);
		System.out.println("B:");
		System.out.println(B);
		System.out.println("D*B*C:");
		System.out.println(mid);
		System.out.println("c:");
		System.out.println(c);
		System.out.println(D.mul(B));*/
		LANCZOS_COEFS = c.getVector();
		//System.out.println(Arrays.toString(LANCZOS_COEFS));
		//System.out.println(Matrix.fromBigVector(c));
		//LANCZOS_COEFS = c.getColumnVector();
		//System.out.println(mid.mul(f));
	}

	//static {
	//	System.out.println("a");
	//	initCoefs(G,7);
	//}
	//static {
	//	ExtraMath.initCoefs(G, 7);
	//}
}