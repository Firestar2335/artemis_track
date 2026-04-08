package JSON;

public final class JsonNull extends JsonPrimitive {
	public static final JsonNull NULL = new JsonNull();

	public String toString() {
		return "null";
	}

	public boolean equals(Object other) {
		return (other instanceof JsonNull);
	}
}
