package ODM;

import java.util.*;
import java.util.regex.*;
import java.time.*;

public class CovarianceMatrix {
	public final String comment;
	private static final int NUM_ROWS = 6;

	public final Instant epoch;
	public final String refFrame;

	/** The matrix in lower triangular form */
	private final double[][] mat;

	private CovarianceMatrix(String refFrame, double[][] mat, Instant epoch, String comment) {
		this.refFrame = refFrame;
		this.mat = mat;
		this.epoch = epoch;
		this.comment = comment;
	}

	/**
	 * If there is a covariance matrix block, returns the parsed result. Otherwise, returns 
	 * {@code null} if there is not a covariance block
	 * @param s
	 * @param epoch
	 * @return
	 */
	public static CovarianceMatrix fromScannerOptional(Scanner s, Instant epoch, String comment) {
		MatchResult frame = Patterns.optionalKVN(s, "COV_REF_FRAME", Patterns.NONDECIMALSTRING);
		if (frame != null) {
			return finishParsing1(s, epoch, frame.group("value"), comment);
		}
		return null;
	}

	/**
	 * If there is a covariance matrix block, returns the parsed result. Otherwise, returns 
	 * {@code null} if there is not a covariance block
	 * @param s
	 * @param epoch
	 * @return
	 */
	public static CovarianceMatrix fromScannerOptional(Scanner s, Instant epoch, String refFrame, String comment) {
		MatchResult frame = Patterns.optionalKVN(s, "COV_REF_FRAME", Patterns.NONDECIMALSTRING);
		if (frame != null) {
			return finishParsing1(s, epoch, frame.group("value"), comment);
		}
		else {
			MatchResult CX_X = Patterns.optionalKVN(s, "CX_X", Patterns.OPTIONALUNIT);
			if (CX_X != null) {
				Patterns.checkUnit(CX_X, "km**2");
				return finishParsing2(s, epoch, refFrame, CX_X, comment);
			}
		}
		return null;
	}

	/**
	 * Finishes parsing the matrix starting at the CX_X term
	 * @param s
	 * @param epoch
	 * @param refFram
	 * @param comment
	 * @return
	 */
	private static CovarianceMatrix finishParsing1(Scanner s, Instant epoch, String refFrame, String comment) {
		MatchResult CX_X = Patterns.mandatoryKVN(s, "CX_X", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CX_X, "km**2");
		return finishParsing2(s, epoch, refFrame, CX_X, comment);
	}

	/**
	 * Finishes parsing the matrix starting at the CY_X terms
	 * @param s
	 * @param epoch
	 * @param refFrame
	 * @param CX_X
	 * @param comment
	 * @return
	 */
	private static CovarianceMatrix finishParsing2(Scanner s, Instant epoch, String refFrame, MatchResult CX_X, String comment) {
		MatchResult CY_X = Patterns.mandatoryKVN(s, "CY_X", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CY_X, "km**2");
		MatchResult CY_Y = Patterns.mandatoryKVN(s, "CY_Y", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CY_Y, "km**2");
		MatchResult CZ_X = Patterns.mandatoryKVN(s, "CZ_X", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CZ_X, "km**2");
		MatchResult CZ_Y = Patterns.mandatoryKVN(s, "CZ_Y", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CZ_Y, "km**2");
		MatchResult CZ_Z = Patterns.mandatoryKVN(s, "CZ_Z", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CZ_Z, "km**2");

		MatchResult CX_DOT_X = Patterns.mandatoryKVN(s, "CX_DOT_X", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CX_DOT_X, "km**2/s");
		MatchResult CX_DOT_Y = Patterns.mandatoryKVN(s, "CX_DOT_Y", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CX_DOT_Y, "km**2/s");
		MatchResult CX_DOT_Z = Patterns.mandatoryKVN(s, "CX_DOT_Z", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CX_DOT_Z, "km**2/s");
		MatchResult CX_DOT_X_DOT = Patterns.mandatoryKVN(s, "CX_DOT_X_DOT", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CX_DOT_X_DOT, "km**2/s**2");

		MatchResult CY_DOT_X = Patterns.mandatoryKVN(s, "CY_DOT_X", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CY_DOT_X, "km**2/s");
		MatchResult CY_DOT_Y = Patterns.mandatoryKVN(s, "CY_DOT_Y", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CY_DOT_Y, "km**2/s");
		MatchResult CY_DOT_Z = Patterns.mandatoryKVN(s, "CY_DOT_Z", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CY_DOT_Z, "km**2/s");
		MatchResult CY_DOT_X_DOT = Patterns.mandatoryKVN(s, "CY_DOT_X_DOT", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CY_DOT_X_DOT, "km**2/s**2");
		MatchResult CY_DOT_Y_DOT = Patterns.mandatoryKVN(s, "CY_DOT_Y_DOT", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CY_DOT_Y_DOT, "km**2/s**2");

		MatchResult CZ_DOT_X = Patterns.mandatoryKVN(s, "CZ_DOT_X", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CZ_DOT_X, "km**2/s");
		MatchResult CZ_DOT_Y = Patterns.mandatoryKVN(s, "CZ_DOT_Y", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CZ_DOT_Y, "km**2/s");
		MatchResult CZ_DOT_Z = Patterns.mandatoryKVN(s, "CZ_DOT_Z", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CZ_DOT_Z, "km**2/s");
		MatchResult CZ_DOT_X_DOT = Patterns.mandatoryKVN(s, "CZ_DOT_X_DOT", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CZ_DOT_X_DOT, "km**2/s**2");
		MatchResult CZ_DOT_Y_DOT = Patterns.mandatoryKVN(s, "CZ_DOT_Y_DOT", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CZ_DOT_Y_DOT, "km**2/s**2");
		MatchResult CZ_DOT_Z_DOT = Patterns.mandatoryKVN(s, "CZ_DOT_Z_DOT", Patterns.OPTIONALUNIT);
		Patterns.checkUnit(CZ_DOT_Z_DOT, "km**2/s**2");

		double[][] mat = new double[][]{{fromMatch(CX_X),     0.0,                 0.0,                 0.0,                     0.0,                     0.0                    },
										{fromMatch(CY_X),     fromMatch(CY_Y),     0.0,                 0.0,                     0.0,                     0.0                    },
										{fromMatch(CZ_X),     fromMatch(CZ_Y),     fromMatch(CZ_Z),     0.0,                     0.0,                     0.0                    },
										{fromMatch(CX_DOT_X), fromMatch(CX_DOT_Y), fromMatch(CX_DOT_Z), fromMatch(CX_DOT_X_DOT), 0.0,                     0.0                    },
										{fromMatch(CY_DOT_X), fromMatch(CY_DOT_Y), fromMatch(CY_DOT_Z), fromMatch(CY_DOT_X_DOT), fromMatch(CY_DOT_Y_DOT), 0.0                    },
										{fromMatch(CZ_DOT_X), fromMatch(CZ_DOT_Y), fromMatch(CZ_DOT_Z), fromMatch(CZ_DOT_X_DOT), fromMatch(CZ_DOT_Y_DOT), fromMatch(CZ_DOT_Z_DOT)}};

		return new CovarianceMatrix(refFrame, mat, epoch, comment);
	}

	private static double fromMatch(MatchResult match) {
		return Patterns.fromMatch(match);//return Double.parseDouble(match.group("value"));
	}

	/**
	 * Parses the covariance block in the specified string, not including "COVARIANCE_START" or 
	 * "COVARIANCE_END" tags
	 * @param s
	 * @param timeSystem
	 * @return
	 */
	public static CovarianceMatrix fromString(String str, String timeSystem, String oldRefFrame) {
		if (str.isBlank()) {
			return null;
		}
		Scanner r = new Scanner(str);
		r.useDelimiter(OrbitalDataMessage.DELIM);
		MatchResult date = Patterns.mandatoryKVN(r, "EPOCH", Patterns.TIMECODESTRING);
		Instant epoch = Patterns.inTimeSystem(Patterns.parseTimestamp(date.group("date")), timeSystem);
		String refFrame = Patterns.getOrDefault(Patterns.optionalKVN(r, "COV_REF_FRAME", Patterns.NONDECIMALSTRING), "value", oldRefFrame);
		r.useDelimiter("\\p{javaWhitespace}+");
		double[][] mat = new double[NUM_ROWS][NUM_ROWS];
		for (int row = 0; row < NUM_ROWS; row++) {
			for (int col = 0; col <= row; col++) {
				mat[row][col] = r.nextDouble();
			}
		}
		r.close();
		return new CovarianceMatrix(refFrame, mat, epoch, null);
	}

	public static CovarianceMatrix fromScannerNonKVN(Scanner s, String timeSystem, String oldRefFrame) {
		if (s.hasNext("COVARIANCE_STOP")) {
			return null;
		}
		Instant epoch = null;
		String frame = oldRefFrame;
		if (s.hasNext("EPOCH")) {
			s.next();
			epoch = Patterns.inTimeSystem(Patterns.parseTimestamp(s.next(Patterns.TIMESTAMP)),timeSystem);
		}
		if (s.hasNext("COV_REF_FRAME")) {
			s.next();
			frame = s.next();
		}
		if (!s.hasNextDouble()) {
			return null;
		}

		double[][] mat = new double[NUM_ROWS][NUM_ROWS];
		for (int row = 0; row < NUM_ROWS; row++) {
			for (int col = 0; col <= row; col++) {
				mat[row][col] = s.nextDouble();
			}
		}
		return new CovarianceMatrix(frame, mat, epoch, null);
	}

	public double get(int row, int col) {
		return mat[row][col];
	}

	public double get(String parameter) {
		return get(Parameter.valueOf(parameter));
	}

	private double get(Parameter param) {
		return get(param.row, param.col);
	}

	public String toString() {
		return "CovarianceMatrix("+Arrays.deepToString(mat)+",refFrame=\""+refFrame+"\" @ " + epoch.toString() + ")";
	}

	private static enum Parameter {
		CX_X (0,0),
		CY_X (1,0),
		CY_Y (1,1),
		CZ_X (2,0),
		CZ_Y (2,1),
		CZ_Z (2,2),
		CX_DOT_X (3,0),
		CX_DOT_Y (3,1),
		CX_DOT_Z (3,2),
		CX_DOT_X_DOT (3,3),
		CY_DOT_X (4,0),
		CY_DOT_Y (4,1),
		CY_DOT_Z (4,2),
		CY_DOT_X_DOT (4,3),
		CY_DOT_Y_DOT (4,4),
		CZ_DOT_X (5,0),
		CZ_DOT_Y (5,1),
		CZ_DOT_Z (5,2),
		CZ_DOT_X_DOT (5,3),
		CZ_DOT_Y_DOT (5,4),
		CZ_DOT_Z_DOT (5,5);

		public final int row;
		public final int col;

		Parameter(int row, int col) {
			this.row = row;
			this.col = col;
		}
	}
}
