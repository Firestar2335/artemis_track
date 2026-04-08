package ODM;
import java.util.*;
import java.util.regex.*;

public class SpacecraftParameters {
	public static final double DEFAULT = -1;

	public final String comment;

	/** The spacecraft mass, in kg */
	public final double mass;
	/** Solar Radiation Pressure Area (A_g), in m**2 */
	public final double solarRadArea;
	/** Solar Radiation pressure Coefficient (C_R) */
	public final double solarRadCoeff;
	/** Drag Area (A_D), in m**2 */
	public final double dragArea;
	/** Drag Coefficient (C_D) */
	public final double dragCoeff;

	private SpacecraftParameters(String comment, double mass, double radArea, double radCoeff, double dragArea, double dragCoeff) {
		this.mass = mass;
		solarRadArea = radArea;
		solarRadCoeff = radCoeff;
		this.dragArea = dragArea;
		this.dragCoeff = dragCoeff;
		this.comment = comment;
	}

	/**
	 * Attempts to parse a {@code SpacecraftParameters} object from the scanner {@code s}. If there
	 * is at least one field, an instance is returned with the provided comment. If no fields for 
	 * this class appear, then {@code null} is returned
	 * @param s
	 * @param comment
	 * @return
	 */
	public static SpacecraftParameters fromScannerOptional(Scanner s, String comment) {
		double mass = DEFAULT;
		double radArea = DEFAULT;
		double radCoef = DEFAULT;
		double dragArea = DEFAULT;
		double dragCoef = DEFAULT;
		MatchResult massMatch = Patterns.optionalKVN(s, "MASS", Patterns.OPTIONALUNIT);
		if (massMatch != null) {
			Patterns.checkUnit(massMatch, "kg");
			mass = Patterns.fromMatch(massMatch);
		}
		MatchResult radAreaMatch = Patterns.optionalKVN(s, "SOLAR_RAD_AREA", Patterns.OPTIONALUNIT);
		if (radAreaMatch != null) {
			Patterns.checkUnit(radAreaMatch, "m**2");
			radArea = Patterns.fromMatch(radAreaMatch);
		}
		MatchResult radCoefMatch = Patterns.optionalKVN(s, "SOLAR_RAD_COEFF", Patterns.NO_UNIT);
		if (radCoefMatch != null) {
			radCoef = Patterns.fromMatch(radCoefMatch);
		}
		MatchResult dragAreaMatch = Patterns.optionalKVN(s, "DRAG_AREA", Patterns.OPTIONALUNIT);
		if (radAreaMatch != null) {
			Patterns.checkUnit(dragAreaMatch, "m**2");
			dragArea = Patterns.fromMatch(dragAreaMatch);
		}
		MatchResult dragCoefMatch = Patterns.optionalKVN(s, "DRAG_COEFF", Patterns.NO_UNIT);
		if (dragCoefMatch != null) {
			dragCoef = Patterns.fromMatch(dragCoefMatch);
		}
		if (mass == DEFAULT && radArea == DEFAULT && radCoef == DEFAULT && dragArea == DEFAULT && dragCoef == DEFAULT) {
			return null;
		}
		else {
			return new SpacecraftParameters(comment, mass, radArea, radCoef, dragArea, dragCoef);
		}
	}
}
