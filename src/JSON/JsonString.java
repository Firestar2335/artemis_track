package JSON;
import java.util.Scanner;
import java.util.regex.*;

public class JsonString extends JsonPrimitive {
	private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"\\\\]|\\\\.)*+\"");
	private static final Pattern WHITESPACE = Pattern.compile("\\p{javaWhitespace}*");
	//private static final Pattern STRING_PATTERN = Pattern.compile("\\p{javaWhitespace}*?\"([^\"\\\\]|\\\\.)*+\"",Pattern.MULTILINE);

	public final String value;

	public JsonString(String value) {
		this.value = value;
	}

	public String getStringValue() {
		return value;
	}

	public String toString() {
		return "\"" + escapeCharacters(value)+"\"";
	}

	public boolean equals(Object o) {
		if (o instanceof JsonString) {
			JsonString other = (JsonString) o;
			return value.equals(other.value);
		}
		return false;
	}

	public static JsonString parse(Scanner s) {
		s.skip(WHITESPACE);
		String result = s.findInLine(STRING_PATTERN);
		if (result == null) {
			throw new IllegalArgumentException("A string was not found at the given position");
		}
		return new JsonString(unescapeCharacters(result.substring(1,result.length()-1)));
	}

	public static String unescapeCharacters(String str) {
		StringBuilder result = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\\' && i+1 < str.length()) {
				switch (str.charAt(i+1)) {
					case '\\': result.append("\\");i++;break;
					case '"': result.append("\"");i++;break;
					case '/': result.append("/");i++;break;
					case 'b': result.append("\b");i++;break;
					case 'f': result.append("\f");i++;break;
					case 'n': result.append("\n");i++;break;
					case 'r': result.append("\r");i++;break;
					case 't': result.append("\t");i++;break;
					case 'u':
						if (i+5 < str.length()) {
							char enc = (char) Integer.parseUnsignedInt(str,i+2,i+6,16);
							i+=5;
							result.append(enc);
							break;
						}
					default: result.append(c);
				}
			}
			else {
				result.append(c);
			}
		}
		return result.toString();
	}

	public static String escapeCharacters(String str) {
		//str = str.replaceAll("([/\"\\])","\\\\$1");
		//special 2 character escape sequences
		//str = str.replace("\b","\\b").replace("\f","\\f").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
		StringBuilder result = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			//int c = str.codePointAt(i);
			switch (c) {
				case '\\':result.append("\\\\");break;
				case '"':result.append("\\\"");break;
				case '/':result.append("\\/");break;
				case '\b':result.append("\\b");break;
				case '\f':result.append("\\f");break;
				case '\n':result.append("\\n");break;
				case '\r':result.append("\\r");break;
				case '\t':result.append("\\t");break;
				default:
					if (c <= 0x1f) {
						result.append(String.format("\\u00%02x",(int) c));
					}
					else {
						result.append(c);
					}
			}
		}
		return result.toString();
	}
}
