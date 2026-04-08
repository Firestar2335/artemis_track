package ExtraMath;

//import java.util.Arrays;
import java.math.*;

public class Matrix {
	private static final MathContext INTERMEDIATE_PRECISION = new MathContext(0,RoundingMode.UNNECESSARY);//MathContext.DECIMAL128;

	private final double[][] mat;

	public Matrix(double[][] mat) {
		this(mat, true);
	}

	private Matrix(double[][] mat, boolean copy) {
		if (copy) {
			this.mat = new double[mat.length][mat[0].length];
			for (int r = 0; r < mat.length; r++) {
				for (int c = 0; c < mat[0].length; c++) {
					this.mat[r][c] = mat[r][c];
				}
			}
		}
		else {
			this.mat = mat;
		}
	}

	/**
	 * Multiplies the two matrices
	 * @param other
	 * @return
	 */
	public Matrix mul(Matrix other) {
		if (mat[0].length != other.mat.length) {
			throw new IllegalArgumentException("Dimensions were not compatible");
		}
		double[][] result = new double[mat.length][other.mat[0].length];
		double accum;
		for (int r = 0; r < mat.length; r++) {
			for (int c = 0; c < other.mat[0].length; c++) {
				accum = 0;
				for (int i = 0; i < other.mat.length; i++) {
					accum = Math.fma(mat[r][i],other.mat[i][c],accum);
				}
				result[r][c] = accum;
			}
		}
		return new Matrix(result, false);
	}

	/**
	 * Multiplies the two matrices using extended precision.
	 * @param other
	 * @return
	 */
	public Matrix mulExtended(Matrix other) {
		if (mat[0].length != other.mat.length) {
			throw new IllegalArgumentException("Dimensions were not compatible");
		}
		double[][] result = new double[mat.length][other.mat[0].length];
		BigDecimal accum;
		for (int r = 0; r < mat.length; r++) {
			for (int c = 0; c < other.mat[0].length; c++) {
				accum = BigDecimal.ZERO;
				for (int i = 0; i < other.mat.length; i++) {
					//accum = fma(BigDecimal.valueOf(mat[r][i]),BigDecimal.valueOf(other.mat[i][c]), accum, INTERMEDIATE_PRECISION);
					accum = bigFMA(mat[r][i],other.mat[i][c], accum, INTERMEDIATE_PRECISION);
				}
				result[r][c] = accum.doubleValue();
			}
		}
		return new Matrix(result, false);
	}

	public BigVector mulExtended(BigVector other) {
		if (mat[0].length != other.getNumRows()) {
			throw new IllegalArgumentException("Dimensions were not compatible");
		}
		BigDecimal[] result = new BigDecimal[mat.length];
		BigDecimal accum;
		for (int r = 0; r < mat.length; r++) {
			accum = BigDecimal.ZERO;
			for (int i = 0; i < mat[0].length; i++) {
				accum = fma(mat[r][i],other.get(i),accum);
			}
			result[r] = accum;
		}
		return new BigVector(result);
	}

	/**
	 * Computes the result of a * b + c in the specified context
	 * @param a
	 * @param b
	 * @param c
	 * @param context
	 * @return
	 */
	private static BigDecimal bigFMA(double a, double b, BigDecimal c, MathContext context) {
		return new BigDecimal(a, context).multiply(new BigDecimal(b,context),context).add(c, context);
	}

	private static BigDecimal fma(double a, BigDecimal b, BigDecimal c) {
		return new BigDecimal(a).multiply(b).add(c);
	}

	public Matrix add(Matrix other) {
		if (mat.length != other.mat.length || mat[0].length != other.mat[0].length) {
			throw new IllegalArgumentException("Dimensions were not compatible");
		}
		double[][] result = new double[mat.length][mat[0].length];
		for (int r = 0; r < mat.length; r++) {
			for (int c = 0; c < mat[0].length; c++) {
				result[r][c] = mat[r][c]+other.mat[r][c];
			}
		}
		return new Matrix(result, false);
	}

	public int getNumRows() {
		return mat.length;
	}

	public int getNumColumns() {
		return mat[0].length;
	}

	public double[][] getMatrix() {
		double[][] result = new double[mat.length][mat[0].length];
		for (int r = 0; r < mat.length; r++) {
			for (int c = 0; c < mat[0].length; c++) {
				result[r][c] = mat[r][c];
			}
		}
		return result;
	}

	public static Matrix fromDiagonal(double[] diag) {
		double[][] result = new double[diag.length][diag.length];
		for (int i = 0; i < diag.length; i++) {
			result[i][i] = diag[i];
		}
		return new Matrix(result, false);
	}

	public static Matrix fromDiagonal(long[] diag) {
		double[][] result = new double[diag.length][diag.length];
		for (int i = 0; i < diag.length; i++) {
			result[i][i] = diag[i];
		}
		return new Matrix(result, false);
	}

	public static Matrix fromColumnVector(double[] vec) {
		double[][] result = new double[vec.length][1];
		for (int i = 0; i < vec.length; i++) {
			result[i][0] = vec[i];
		}
		return new Matrix(result, false);
	}

	/**
	 * If this matrix is a column vector, it is flattened. Otherwise, 
	 * @return the first column of the matrix
	 * @throws IllegalStateException if the matrix has more than one column
	 */
	public double[] getColumnVector() {
		if (mat[0].length != 1) {
			throw new IllegalStateException("Matrix was not a column vector");
		}
		double[] res = new double[mat[0].length];
		for (int r = 0; r < res.length; r++) {
			res[r] = mat[r][0];
		}
		return res;
	}

	public String toString() {
		String[][] strs = new String[mat.length][mat[0].length];
		int[] maxWidths = new int[mat[0].length];
		for (int r = 0; r < mat.length; r++) {
			for (int c = 0; c < maxWidths.length; c++) {
				strs[r][c] = makeString(mat[r][c]);
				if (strs[r][c].length() > maxWidths[c]) {
					maxWidths[c] = strs[r][c].length();
				}
			}
		}
		String[] rows = new String[strs.length];
		for (int r = 0; r < strs.length; r++) {
			rows[r] = "["+leftPad(strs[r][0],maxWidths[0], ' ');
			for (int c = 1; c < maxWidths.length; c++) {
				rows[r] +=", " + leftPad(strs[r][c],maxWidths[c],' ');
			}
			rows[r] += "]";
		}
		return "[" + String.join(",\n ", rows) + "]";
	}

	private static String makeString(double val) {
		if (ExtraMath.isLong(val)) {
			return "" + (long) val;
		}
		else if (ExtraMath.isInt(val)) {
			return "" + (int) val;
		}
		else {
			return "" + val;
		}
	}

	private static String leftPad(String str, int length, char fillChar) {
		if (str.length() >= length) {
			return str;
		}
		String r = "";
		for (int i = 0; i < length - str.length(); i++) {
			r += fillChar;
		}
		return r + str;
	}

	public static Matrix fromBigVector(BigVector vec) {
		double[][] result = new double[vec.getNumRows()][1];
		for (int i = 0; i < result.length; i++) {
			result[i][0] = vec.get(i).doubleValue();
		}
		return new Matrix(result,false);
	}
}
