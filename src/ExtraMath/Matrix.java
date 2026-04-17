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
	 * Performs {@code this * vector}, interpreting {@code vector} as a column vector, and returns 
	 * the resulting column vector
	 * @param vector
	 * @return
	 */
	public double[] mul(Vector3D vector) {
		if (mat[0].length != 3) {
			throw new ArithmeticException("Dimensions were not compatible");
		}
		double[] res = new double[mat.length];
		for (int r = 0; r < mat.length; r++) {
			res[r] = mat[r][0]*vector.x;
			res[r] = Math.fma(mat[r][1],vector.y,res[r]);
			res[r] = Math.fma(mat[r][2],vector.z,res[r]);
		}
		return res;
	}

	public Matrix mul(double scalar) {
		double[][] res = new double[mat.length][mat[0].length];
		for (int r = 0; r < mat.length; r++) {
			for (int c = 0; c < mat[0].length; c++) {
				res[r][c] = scalar * mat[r][c];
			}
		}
		return new Matrix(res,false);
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

	public double get(int row, int col) {
		if (row < 0 || row >= mat.length) {
			throw new IndexOutOfBoundsException(row);
		}
		else if (col < 0 || col >= mat[0].length) {
			throw new IndexOutOfBoundsException(col);
		}
		return mat[row][col];
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
		return new Matrix(result);
	}

	public static Matrix fromColumns(double[]... cols) {
		return new Matrix(transpose(cols),false);
	}

	public static Matrix fromVectorColumns(Vector3D... cols) {
		double[][] result = new double[3][cols.length];
		for (int i = 0; i < cols.length; i++) {
			result[0][i] = cols[i].x;
			result[1][i] = cols[i].y;
			result[2][i] = cols[i].z;
		}
		return new Matrix(result, false);
	}

	/**
	 * Creates the nxn identity matrix
	 * @param size
	 * @return
	 */
	public static Matrix identity(int size) {
		double[][] res = new double[size][size];
		for (int i = 0; i < size; i++) {
			res[i][i] = 1;
		}
		return new Matrix(res, false);
	}

	private static double[][] transpose(double[][] mat) {
		double[][] res = new double[mat[0].length][mat.length];
		for (int r = 0; r < mat.length; r++) {
			for (int c = 0; c < mat[r].length; c++) {
				res[c][r] = mat[r][c];
			}
		}
		return res;
	}

	public Matrix transpose() {
		return new Matrix(transpose(mat));
	}

	/**
	 * Computes the inverse of this matrix
	 * @return
	 * @throws ArithmeticException if this matrix is not invertible
	 */
	public Matrix inverse() {
		if (mat[0].length != mat.length) {
			throw new ArithmeticException("Matrix was not a square matrix");
		}

		if (mat.length == 1) {
			if (mat[0][0] == 0) {
				throw new ArithmeticException("This matrix is not invertible");
			}
			return new Matrix(new double[][]{{1/mat[0][0]}},false);
		}
		else if (mat.length == 2) {
			double det = mat[0][0]*mat[1][1]-mat[1][0]*mat[0][1];
			if (det == 0) {
				throw new ArithmeticException("This matrix is not invertible");
			}
			return new Matrix(new double[][]{{mat[1][1]/det,-mat[0][1]/det},{-mat[1][0]/det,mat[0][0]/det}},false);
		}
		else if (mat.length == 3) {
			double A = mat[1][1]*mat[2][2]-mat[1][2]*mat[2][1], D = -(mat[0][1]*mat[2][2]-mat[0][2]*mat[1][1]), G = mat[0][1]*mat[1][2]-mat[0][2]*mat[1][1];
			double B = -(mat[1][0]*mat[2][2]-mat[1][2]*mat[2][0]), E=mat[0][0]*mat[2][2]-mat[0][2]*mat[2][0], H = -(mat[0][0]*mat[1][2]-mat[0][2]*mat[1][0]);
			double C = mat[1][0]*mat[2][1]-mat[1][1]*mat[2][0], F = -(mat[0][0]*mat[2][1]-mat[0][1]*mat[2][0]), I = mat[0][0]*mat[1][1]-mat[0][1]*mat[1][0];
			
			double det = mat[0][0]*A+mat[0][1]*B+mat[0][2]*C;
			if (det == 0) {
				throw new ArithmeticException("This matrix is not invertible");
			}
			return new Matrix(new double[][]{{A/det,D/det,G/det},{B/det,E/det,H/det},{C/det,F/det,I/det}},false);
		}

		double[][] aug = new double[mat.length][mat.length*2];
		for (int r = 0; r < mat.length; r++) {
			aug[r][r+mat.length] = 1;
			for (int c = 0; c < mat.length; c++) {
				aug[r][c] = mat[r][c];
			}
		}

		for (int c = 0; c < mat.length; c++) {
			//pivot row;
			int k = -1;
			for (int j = c; j < mat.length; j++) {
				if (aug[j][c] != 0 && (k == -1 || Math.abs(aug[j][c]) > Math.abs(aug[k][c]))) {
					k = j;
				}
			}
			if (k == -1) {
				throw new ArithmeticException("This matrix was not invertible");
			}
			if (k != c) {
				double[] tmp = aug[k];
				aug[k] = aug[c];
				aug[c] = tmp;
			}
			for (int r = c+1; r < mat.length; r++) {
				double scale = aug[r][c] / aug[c][c];
				aug[r][c] = 0;
				for (int i = c+1; i < aug[r].length; i++) {
					aug[r][i] -= aug[c][i]*scale;
				}
			}
		}
		//work backwards
		for (int r = aug.length-1; r >= 0; r--) {
			double f = aug[r][r];
			aug[r][r] = 1;
			for (int c = r+1; c < aug[r].length; c++) {
				aug[r][c] /= f;
			}
			for (int r2 = 0; r2 < r; r2++) {
				f = aug[r2][r];
				aug[r2][r] = 0;
				for (int c = r+1; c < aug[r2].length; c++) {
					aug[r2][c] -= aug[r][c]*f;
				}
			}
		}
		double[][] result = new double[mat.length][mat.length];
		for (int r = 0; r < aug.length; r++) {
			for (int c = mat.length; c < aug[r].length; c++) {
				result[r][c-mat.length] = aug[r][c];
			}
		}
		return new Matrix(result, false);
	}
}
