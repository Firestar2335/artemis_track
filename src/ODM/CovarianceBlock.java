package ODM;
import java.util.*;

public class CovarianceBlock {
	public final String comment;
	private final CovarianceMatrix[] matrices;

	public CovarianceBlock(String comment, Collection<? extends CovarianceMatrix> matrices) {
		this.comment = comment;
		this.matrices = new CovarianceMatrix[matrices.size()];
		Iterator<? extends CovarianceMatrix> iter = matrices.iterator();
		for (int i = 0; i < this.matrices.length; i++) {
			this.matrices[i] = iter.next();
		}
	}

	public static CovarianceBlock fromString(String str, String timeSystem, String oldRefFrame) {
		Scanner reader = new Scanner(str);
		reader.useDelimiter("\\p{javaWhitespace}*=\\p{javaWhitespace}*|\\p{javaWhitespace}+");
		if (reader.hasNext("COVARIANCE_START")) {
			reader.next();
		}
		String com = "";
		String line = null;
		while (reader.hasNext("COMMENT")) {
			reader.next();
			line = reader.nextLine().strip();
			com += (com.isEmpty() ? "" : "\n") + line;
		}
		if (line == null) {
			com = null;
		}
		List<CovarianceMatrix> mats = new ArrayList<>();
		while (!reader.hasNext("COVARIANCE_STOP")) {
			mats.add(CovarianceMatrix.fromScannerNonKVN(reader, timeSystem, oldRefFrame));
		}
		return new CovarianceBlock(com, mats);
	}

	public CovarianceMatrix[] getMatrices() {
		return Arrays.copyOf(matrices, matrices.length);
	}

	public String toString() {
		String result = "Covariances(";
		if (comment != null) {
			result += "comment = \"";
			result += comment;
			result += "\", ";
		}
		result += Arrays.toString(matrices);
		result += ")";
		return result;
	}
}