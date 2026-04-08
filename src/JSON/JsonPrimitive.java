package JSON;
import java.util.*;

public abstract class JsonPrimitive extends JsonType {
	public boolean getBooleanValue() {
		throw new ClassCastException();
	}

	public int getIntValue() {
		throw new ClassCastException();
	}

	public double getDoubleValue() {
		throw new ClassCastException();
	}

	/*public String getStringValue() {
		return this.toString();
	}*/

	public static JsonPrimitive parse(Scanner s) {
		if (s.hasNext("\".*")) {
			return JsonString.parse(s);
		}
		else if (s.hasNextDouble()) {
			return new JsonNumber(s.nextDouble());
		}
		else if (s.hasNext("null")) {
			s.next();
			return JsonNull.NULL;
		}
		else if (s.hasNextBoolean()) {
			return JsonBoolean.fromString(s.next());
		}
		throw new IllegalArgumentException("Next token was not a JSON primitive");
	}
}
