package ODM;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.regex.MatchResult;

public class OEMMetadata extends BaseMetadata {
	private static final int DEGREE_DEFAULT = -1;

	public final Instant startTime;
	public final Instant useableStartTime;
	public final Instant useableStopTime;
	public final Instant stopTime;
	public final String interpolation;
	public final int interpolationDegree;

	public OEMMetadata(String comment, String name, String id, String center, String frame, Instant epoch, String timeSystem, 
		Instant start, Instant useableStart, Instant useableStop, Instant stop, String interp, int interpDegree) {
		super(comment, name, id, center, frame, epoch, timeSystem);
		startTime = start;
		useableStartTime = useableStart;
		useableStopTime = useableStop;
		stopTime = stop;
		interpolation = interp;
		interpolationDegree = interpDegree;
	}

	public OEMMetadata(Scanner s) {
		if (s.hasNext("META_START")) {
			s.next();
		}
		super(s);
		MatchResult m = Patterns.mandatoryKVN(s, "START_TIME", Patterns.TIMECODESTRING);
		startTime = Patterns.inTimeSystem(Patterns.parseTimestamp(m.group("date")), timeSystem);

		m = Patterns.optionalKVN(s, "USEABLE_START_TIME", Patterns.TIMECODESTRING);
		useableStartTime =  (m != null) ? Patterns.inTimeSystem(Patterns.parseTimestamp(m.group("date")), timeSystem) : null;
		m = Patterns.optionalKVN(s, "USEABLE_STOP_TIME", Patterns.TIMECODESTRING);
		useableStopTime = (m != null) ? Patterns.inTimeSystem(Patterns.parseTimestamp(m.group("date")), timeSystem) : null;

		m = Patterns.mandatoryKVN(s, "STOP_TIME", Patterns.TIMECODESTRING);
		stopTime = Patterns.inTimeSystem(Patterns.parseTimestamp(m.group("date")), timeSystem);
		
		m = Patterns.optionalKVN(s, "INTERPOLATION", Patterns.NONDECIMALSTRING);
		interpolation = Patterns.getOrDefault(m, "value", null);
		m = Patterns.conditionalKVN(s, "INTERPOLATION_DEGREE", Patterns.INTEGER, m != null);
		interpolationDegree = Patterns.getOrDefault(m, "value", DEGREE_DEFAULT);
		s.next("META_STOP");
	}

	@SuppressWarnings("unused")
	public String getString(String key) {
		switch (key) {
			case "startTime": return startTime.toString();
			case "useableStartTime":
				if (useableStartTime == null && STRICTACCESS) {
					throw new IllegalStateException("useableStartTime was never defined");
				}
				return useableStartTime.toString();
			case "useableStopTime":
				if (useableStopTime == null && STRICTACCESS) {
					throw new IllegalStateException("useableStartTime was never defined");
				}
				return useableStartTime.toString();
			case "stopTime": return stopTime.toString();
			case "interpolation":
				if (interpolation == null && STRICTACCESS) {
					throw new IllegalStateException("interpolation was never defined");
				}
				return interpolation;
			case "interpolationDegree":
				if (interpolationDegree == DEGREE_DEFAULT && STRICTACCESS) {
					throw new IllegalStateException("interpolationDegree was never defined");
				}
				return Integer.toString(interpolationDegree);
		}
		return super.getString(key);
	}

	@SuppressWarnings("unused")
	public int getInt(String key) {
		switch (key) {
			case "interpolationDegree":
				if (interpolationDegree == DEGREE_DEFAULT && STRICTACCESS) {
					throw new IllegalStateException("interpolationDegree was never defined");
				}
				return interpolationDegree;
			case "useableStartTime":
				if (useableStartTime == null && STRICTACCESS) {
					throw new IllegalStateException("useableStartTime was never defined");
				}
				throw new ClassCastException(INT_ERROR);
			case "useableStopTime":
				if (useableStopTime == null && STRICTACCESS) {
					throw new IllegalStateException("useableStopTime was never defined");
				}
				throw new ClassCastException(INT_ERROR);
			case "interpolation":
				if (interpolation == null && STRICTACCESS) {
					throw new IllegalStateException("interpolation was never defined");
				}
			case "startTime":
			case "stopTime":
				throw new ClassCastException(INT_ERROR);
		}
		return super.getInt(key);
	}

	@SuppressWarnings("unused")
	public double getDouble(String key) {
		switch (key) {
			case "interpolationDegree":
				if (interpolationDegree == DEGREE_DEFAULT && STRICTACCESS) {
					throw new IllegalStateException("interpolationDegree was never defined");
				}
				return interpolationDegree;
			case "useableStartTime":
				if (useableStartTime == null && STRICTACCESS) {
					throw new IllegalStateException("useableStartTime was never defined");
				}
				throw new ClassCastException(DOUBLE_ERROR);
			case "useableStopTime":
				if (useableStopTime == null && STRICTACCESS) {
					throw new IllegalStateException("useableStopTime was never defined");
				}
				throw new ClassCastException(DOUBLE_ERROR);
			case "interpolation":
				if (interpolation == null && STRICTACCESS) {
					throw new IllegalStateException("interpolation was never defined");
				}
			case "startTime":
			case "stopTime":
				throw new ClassCastException(DOUBLE_ERROR);
		}
		return super.getInt(key);
	}

	@SuppressWarnings("unused")
	public Temporal getDate(String key) {
		switch (key) {
			case "startTime": return startTime;
			case "stopTime": return stopTime;
			case "useableStartTime":
				if (useableStartTime == null && STRICTACCESS) {
					throw new IllegalStateException("useableStartTime was never defined");
				}
				return useableStartTime;
			case "useableStopTime":
				if (useableStopTime == null && STRICTACCESS) {
					throw new IllegalStateException("useableStopTime was never defined");
				}
				return useableStopTime;
			case "interpolation":
				if (interpolation == null && STRICTACCESS) {
					throw new IllegalStateException("interpolation was never defined");
				}
				throw new ClassCastException(TEMPORAL_ERROR);
			case "interpolationDegree":
				if (interpolationDegree == DEGREE_DEFAULT && STRICTACCESS) {
					throw new IllegalStateException("interpolationDegree was never defined");
				}
				throw new ClassCastException(TEMPORAL_ERROR);
		}
		return super.getDate(key);
	}

	@SuppressWarnings("unused")
	public Set<String> getKeys() {
		Set<String> keys = super.getKeys();
		keys.add("startTime");
		keys.add("stopTime");
		if (!STRICTACCESS || useableStartTime != null) {
			keys.add("useableStartTime");
		}
		if (!STRICTACCESS || useableStopTime != null) {
			keys.add("useableStopTime");
		}
		if (!STRICTACCESS || interpolation != null) {
			keys.add("interpolation");
		}
		if (!STRICTACCESS || interpolationDegree != DEGREE_DEFAULT) {
			keys.add("interpolationDegree");
		}
		return keys;
	}

	public Set<String> getAllValidKeys() {
		Set<String> keys = super.getAllValidKeys();
		keys.add("startTime");
		keys.add("useableStartTime");
		keys.add("useableStopTime");
		keys.add("stopTime");
		keys.add("interpolation");
		keys.add("interpolationDegree");
		return keys;
	}

	public String toString() {
		return "OEMMetadata(" + collate() + ")";
	}
}
