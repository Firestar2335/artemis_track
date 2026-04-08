package ExtraMath;

import java.math.*;
import java.util.Arrays;

/**
 * This is a column vector
 */
public class BigVector {
	private final BigDecimal[] vec;

	public BigVector(BigDecimal[] vec) {
		this.vec = new BigDecimal[vec.length];
		for (int i = 0; i < vec.length; i++) {
			this.vec[i] = vec[i];
		}
	}

	public String toString() {
		return Arrays.toString(vec);
	}

	public int getNumColumns() {
		return 1;
	}

	public int getNumRows() {
		return vec.length;
	}

	public BigDecimal get(int index) {
		return vec[index];
	}

	public BigDecimal[] getVector() {
		BigDecimal[] res = new BigDecimal[vec.length];
		for (int i = 0; i < vec.length; i++) {
			res[i] = vec[i];
		}
		return res;
	}

	public double[] getDoubleVector() {
		double[] res = new double[vec.length];
		for (int i = 0; i < vec.length; i++) {
			res[i] = vec[i].doubleValue();
		}
		return res;
	}
}
