package JSON;

public class JsonNumber extends JsonPrimitive {
	public final double value;

	public JsonNumber(double value) {
		this.value = value;
	}

	public int getIntValue() {
		if (isInt(value)) {
			return (int) value;
		}
		else {
			throw new ClassCastException();
		}
	}

	public double getDoubleValue() {
		return value;
	}

	public String toString() {
		if (isInt(value)) {
			return Integer.toString((int) value);
		}
		else {
			return Double.toString(value);
		}
	}

	private static boolean isInt(double d) {
		return d == (int) d;
	}

	public boolean equals(Object other) {
		if (other instanceof JsonNumber) {
			JsonNumber o = (JsonNumber) other;
			return value == o.value;
		}
		return false;
	}
}
