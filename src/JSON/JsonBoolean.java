package JSON;

public class JsonBoolean extends JsonPrimitive {
	public static final JsonBoolean TRUE = new JsonBoolean(true);
	public static final JsonBoolean FALSE = new JsonBoolean(false);

	public final boolean value;

	public JsonBoolean(boolean value) {
		this.value = value;
	}

	public static JsonBoolean fromString(String s) {
		s = s.toLowerCase();
		if (s.equals("true")) {
			return TRUE;
		}
		else if (s.equals("false")) {
			return FALSE;
		}
		throw new IllegalArgumentException("Provided string was not a boolean");
	}

	public boolean getBooleanValue() {
		return value;
	}

	public String toString() {
		return value ? "true" : "false";
	}

	public boolean equals(Object o) {
		if (o instanceof JsonBoolean) {
			JsonBoolean other = (JsonBoolean) o;
			return value == other.value;
		}
		return false;
	}
}
