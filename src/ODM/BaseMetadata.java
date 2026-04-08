package ODM;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.*;

public class BaseMetadata implements Metadata {
	private static final Set<String> MANDATORY_KEYS = new TreeSet<>(Set.of("objectName","objectID","centerName","refFrame","timeSystem"));
	protected static final String INT_ERROR = "Value could not be converted to an int";
	protected static final String DOUBLE_ERROR = "Value could not be converted to a double";
	protected static final String TEMPORAL_ERROR = "Value could not be converted to a temporal object";

	public final String comment;
	public final String objectName;
	public final String objectID;
	public final String centerName;
	public final String refFrame;
	public final Instant refFrameEpoch;
	public final String timeSystem;

	public BaseMetadata(String comment, String name, String id, String center, String frame, Instant epoch, String timeSystem) {
		this.comment = comment;
		objectName = name;
		objectID = id;
		centerName = name;
		refFrame = frame;
		refFrameEpoch = epoch;
		this.timeSystem = timeSystem;
	}

	public static void peekNext(Scanner s) {
		s.hasNext(".*");
		System.out.println(s.match().group());
	}
	public static void bracketNext(Scanner s) {
		System.out.print('"');
		System.out.print(s.next());
		System.out.println('"');
	}

	public BaseMetadata(Scanner s) {
		String com = Patterns.optionalComment(s);
		String name = Patterns.mandatoryKVN(s, "OBJECT_NAME", Patterns.NONDECIMALSTRING).group("value");
		String id = Patterns.mandatoryKVN(s, "OBJECT_ID", Patterns.NONDECIMALSTRING).group("value");
		String center = Patterns.mandatoryKVN(s, "CENTER_NAME", Patterns.NONDECIMALSTRING).group("value");
		String frame = Patterns.mandatoryKVN(s, "REF_FRAME", Patterns.NONDECIMALSTRING).group("value");
		MatchResult m = Patterns.optionalKVN(s, "REF_FRAME_EPOCH", Patterns.TIMECODESTRING);
		Instant epoch = null;
		if (m != null) {
			epoch = Patterns.parseTimestamp(m.group("date")).toInstant(ZoneOffset.UTC);
		}
		String timeSys = Patterns.mandatoryKVN(s, "TIME_SYSTEM", Patterns.NONDECIMALSTRING).group("value");
		this(com,name,id,center,frame,epoch,timeSys);
	}

	public String getString(String key) {
		switch (key) {
			case "comment": return comment;
			case "objectName": return objectName;
			case "objectID": return objectID;
			case "centerName": return centerName;
			case "refFrame": return refFrame;
			case "refFrameEpoch": return refFrameEpoch.toString();
			case "timeSystem": return timeSystem;
		}
		throw new NoSuchElementException("Invalid key");
	}

	public int getInt(String key) {
		switch (key) {
			case "comment":
			case "objectName":
			case "objectID":
			case "centerName":
			case "refFrame":
			case "refFrameEpoch":
			case "timeSystem": throw new ClassCastException(INT_ERROR);
		}
		throw new NoSuchElementException("Invalid key");
	}

	public double getDouble(String key) {
		switch (key) {
			case "comment":
			case "objectName":
			case "objectID":
			case "centerName":
			case "refFrame":
			case "refFrameEpoch":
			case "timeSystem": throw new ClassCastException(DOUBLE_ERROR);
		}
		throw new NoSuchElementException("Invalid key");
	}

	public Temporal getDate(String key) {
		switch (key) {
			case "refFrameEpoch": return refFrameEpoch;
			case "comment":
			case "objectName":
			case "objectID":
			case "centerName":
			case "refFrame":
			case "timeSystem": throw new ClassCastException(TEMPORAL_ERROR);
		}
		throw new NoSuchElementException("Invalid key");
	}

	public Set<String> getKeys() {
		Set<String> result = new TreeSet<>(MANDATORY_KEYS);
		if (!STRICTACCESS || comment != null) {
			result.add("comment");
		}
		if (!STRICTACCESS || refFrameEpoch != null) {
			result.add("refFrameEpoch");
		}
		return result;
	}

	public Set<String> getAllValidKeys() {
		Set<String> result = new TreeSet<>(MANDATORY_KEYS);
		result.add("comment");
		result.add("refFrameEpoch");
		return result;
	}

	public String toString() {
		return "BaseMetadata(" + collate() + ")";
	}

	protected String collate() {
		String result = "";
		Set<String> keys = this.getKeys();
		for (String key : keys) {
			if (!result.isEmpty()) {
				result += ", ";
			}
			result += key + "="+getString(key);
		}
		return result;
	}
}