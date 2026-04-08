package ODM;
import java.util.*;
import java.time.*;
import java.time.temporal.*;

public class OMMMetadata extends BaseMetadata {
	public final String meanElementTheory;

	public OMMMetadata(String comment, String name, String id, String center, String frame, Instant epoch, String timeSystem, String elementTheory) {
		super(comment, name, id, center, frame, epoch, timeSystem);
		this.meanElementTheory = elementTheory;
	}

	public OMMMetadata(Scanner s) {
		super(s);
		meanElementTheory = Patterns.mandatoryKVN(s, "MEAN_ELEMENT_THEORY", Patterns.NONDECIMALSTRING).group("value");
	}

	public String getString(String key) {
		if (key.equals("meanElementTheory")) {
			return meanElementTheory;
		}
		return super.getString(key);
	}

	public int getInt(String key) {
		if (key.equals("meanElementTheory")) {
			throw new ClassCastException(INT_ERROR);
		}
		return super.getInt(key);
	}

	public double getDouble(String key) {
		if (key.equals("meanElementTheory")) {
			throw new ClassCastException(DOUBLE_ERROR);
		}
		return super.getDouble(key);
	}

	public Temporal getDate(String key) {
		if (key.equals("meanElementTheory")) {
			throw new ClassCastException(TEMPORAL_ERROR);
		}
		return super.getDate(key);
	}

	public Set<String> getKeys() {
		Set<String> k = super.getKeys();
		k.add("meanElementTheory");
		return k;
	}

	public Set<String> getAllValidKeys() {
		Set<String> k = super.getAllValidKeys();
		k.add("meanElementTheory");
		return k;
	}

	public String toString() {
		return "OMMMetadata(" + collate() + ")";
	}
}
