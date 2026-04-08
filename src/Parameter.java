import java.time.*;
import java.time.format.*;
import java.util.Map;

public class Parameter {
	public static final DateTimeFormatter PARAMETER_FORMAT = DateTimeFormatter.ofPattern("yyyy':'DDD':'HH':'mm':'ss'.'SSS").withZone(ZoneOffset.UTC);
	
	public static final boolean TYPE_CHECK = false;

	public final int number;
	public final int length;
	public final String status;
	public final ZonedDateTime time;
	/** The type of the data field. <p>
	 * Presumed mappings:
	 * <ul>
	 * <li> 2: double/floating point
	 * <li> 3: binary/hex value
	 * <li> 6: integer
	 * </li>
	 */
	public final int type;
	public final String value;

	public Parameter(int number, int length, String status, ZonedDateTime time, int type, String value) {
		this.number = number;
		this.length = length;
		this.status = status;
		this.time = time;
		this.type = type;
		this.value = value;
	}

	public static Parameter fromMap(Map<String, String> mapping) {
		int number = Integer.parseInt(mapping.get("Number"));
		int length = Integer.parseInt(mapping.get("Length"));
		ZonedDateTime time = ZonedDateTime.parse(mapping.get("Time"),PARAMETER_FORMAT);
		int type = Integer.parseInt(mapping.get("Type"));
		return new Parameter(number, length, mapping.get("Status"),time, type, mapping.get("Value"));
	}

	public String toString() {
		String result = "{\n\t\t\"";
		String indent = "\",\n\t\t\"";
		result += "Number\": \"";
		result += number;
		result += indent;
		result += "Length\": \"";
		result += length;
		result += indent;
		result += "Status\": \"";
		result += status;
		result += indent;
		result += "Time\": \"";
		result += PARAMETER_FORMAT.format(time);
		result += indent;
		result += "Type\": \"";
		result += type;
		result += indent;
		result += "Value\": \"";
		result += value;
		result += "\"\n\t}";
		return result;
	}

	/**
	 * Gets the value in this object as a String
	 * @return The value as a string
	 */
	public String getValueString() {
		return value;
	}

	/**
	 * Gets the parameter value as an int. If it is a double, it is converted to an int
	 * @return The value as an int
	 */
	public int getValueInt() {
		if (type == 6) {
			return Integer.parseInt(value);
		}
		else if (type == 2) {
			return (int) Double.parseDouble(value);
		}
		throw new IllegalStateException("Value was not numeric");
	}

	/**
	 * Gets the value of this parameter as a byte stored in hexadecimal form
	 * @return The byte stored by this data
	 */
	public byte getValueByte() {
		if (type == 3) {
			return (byte) Integer.parseUnsignedInt(value);
		}
		throw new IllegalStateException("Value was not a hexadecimal value");
	}

	public double getValueDouble() {
		if (type == 2 || type == 6) {
			return Double.parseDouble(value);
		}
		throw new IllegalStateException("Value was not numeric");
	}
}
