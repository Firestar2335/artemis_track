package JSON;
import java.util.*;

/**
 * This is the base class for JSON types
 */
public abstract class JsonType {
	protected static final String READ_ONLY_ERROR = "This is a read-only map";


	public static JsonType parse(Scanner s) {
		if (s.hasNext("\\{")) {
			return JsonObject.parse(s);
		}
		else if (s.hasNext("\\[")) {
			return JsonArray.parse(s);
		}
		else {
			return JsonPrimitive.parse(s);
		}
	}

	public JsonType get(int index) {
		throw new UnsupportedOperationException();
	}

	public JsonType get(Object key) {
		throw new UnsupportedOperationException();
	}

	public JsonObject getObject(String key) {
		throw new UnsupportedOperationException();
	}

	public int getIntValue() {
		throw new UnsupportedOperationException();
	}

	public double getDoubleValue() {
		throw new UnsupportedOperationException();
	}

	public boolean getBooleanValue() {
		throw new UnsupportedOperationException();
	}

	public String getStringValue() {
		return toString();//throw new UnsupportedOperationException();
	}
}
