package ODM;

import java.util.*;
import java.util.regex.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;

/**
 * This is just a class that holds regex helper methods for matching and reading data from the files
 */
abstract class Patterns {
	public static final String KEYWORD = "(?<keyword>[0-9A-Z_]*)";
	public static final String FIXEDORFLOAT = "[-+]?[0-9]+(?:\\.\\d*)?(?:[eE][-+]?\\d+)?";
	public static final String TIMESTAMP = "\\d{4}-(?:\\d\\d-\\d\\d|\\d{3})T\\d\\d:\\d\\d:\\d\\d(?:\\.\\d*)?Z?";
	public static final Pattern TIMECODESTRING = Pattern.compile(KEYWORD+" *= *(?<date>"+TIMESTAMP+")", Pattern.MULTILINE);//\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}(?:\\.\\d*)?
	public static final Pattern NONDECIMALSTRING = Pattern.compile(KEYWORD+" *= *(?<value>(?:[0-9A-Z_ /-]*|[0-9a-z_ /-]*)\\S)", Pattern.MULTILINE);
	public static final Pattern FREETEXTSTRING = Pattern.compile(KEYWORD+" *= *(?<value>[0-9A-Za-z_. -]*\\S)", Pattern.MULTILINE);
	public static final Pattern OPTIONALUNIT = Pattern.compile(KEYWORD+" *= *(?<value>"+FIXEDORFLOAT+")(?: +\\[(?<unit>[0-9A-Za-z/_*]*)])?", Pattern.MULTILINE);//This one had lowercase in keyword
	public static final Pattern NO_UNIT = Pattern.compile(KEYWORD+" *= *(?<value>"+FIXEDORFLOAT+")", Pattern.MULTILINE);
	public static final Pattern INTEGER = Pattern.compile(KEYWORD + " *= *(?<value>[+-]?\\d+)", Pattern.MULTILINE);

	public static final Pattern USER_DEFINED = Pattern.compile("USER_DEFINED_(?<x>[0-9A-Z_]*) *= *+(?<value>(?:.*\\S)?)", Pattern.MULTILINE);
	public static final Pattern COMMENT = Pattern.compile("COMMENT(?: (?<comment>(?:.*\\S)?))?", Pattern.MULTILINE);
	public static final Pattern VERSION = Pattern.compile("\\A\\s*CCSDS_(?<type>O[PMEC]M)_VERS *= *(?<version>\\d+\\.\\d+)", Pattern.MULTILINE);
	
	public static final DateTimeFormatter DAYOFYEAR = new DateTimeFormatterBuilder()
			.appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
			.appendPattern("'-'DDD'T'HH':'mm':'ss")
			.appendFraction(ChronoField.NANO_OF_SECOND,0,9,true).appendPattern("['Z']").toFormatter();
	public static final DateTimeFormatter ISO_FORMAT = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME).appendPattern("['Z']").toFormatter();

	/**
	 * Tests to see if the unit in the matched group is what was expected.
	 * <p>
	 * If {@code toTest} has a named capturing gorup named "unit" that participated in the match 
	 * and matched something other than {@code unit}, then an {@code InputMismatchException} is 
	 * thrown.
	 * If {@code toTest} is null, then nothing is done.
	 * @param toTest
	 * @param unit
	 * @throws InputMismatchException If the group has a unit which does not match the expected unit.
	 */
	public static void checkUnit(MatchResult toTest, String unit) {
		if (toTest != null) {
			Map<String, Integer> groups = toTest.namedGroups();
			if (groups.containsKey("unit")) {
				String match = toTest.group(groups.get("unit").intValue());
				if (match != null && match.equals(unit)) {
					throw new InputMismatchException("Matched unit ("+match+") was not expected unit (" + unit+")");
				}
			}
		}
	}

	/**
	 * If the next lines are comment lines, returns the total content of the comments on separate 
	 * lines. Otherwise, returns {@code null} and doesn't change the position of scanner.
	 * @param s The scanner to look in
	 * @return The comment if there is a comment, {@code null} otherwise
	 */
	public static String optionalComment(Scanner s) {
		String c = "";
		boolean found = false;
		while (s.hasNext(COMMENT)) {
			c += (found ? "\n" : "") + s.match().group("comment");
			found = true;
			s.next();
			//return c;
		}
		return found ? c : null;
	}

	/**
	 * Gets and returns the match result, throwing an exception if the keyword is not the provided 
	 * keyword
	 * @param s The scanner to read
	 * @param keyword The expected keyword
	 * @param format The expected format of the line
	 * @return The {@code MatchResult} of the next line of input if the format matches and the 
	 * keyword is what is expected.
	 * @throws InputMismatchException if the next line of input does not match {@code format} or if 
	 * the keyword associated with the next line of input is not {@code keyword}
	 * @see #optionalKVN(Scanner, String, Pattern)
	 */
	public static MatchResult mandatoryKVN(Scanner s, String keyword, Pattern format) {
		//String next = s.next(format);
		//if (next == null) {
		//	throw new InputMismatchException("Format did not match");
		//}
		s.next(format);
		MatchResult m = s.match();
		//System.out.println(m.group());
		if (!m.group("keyword").equals(keyword)) {
			throw new InputMismatchException("Keyword was not what was expected (\"" + m.group("keyword")+"\" instead of \""+keyword+"\")");
		}
		return m;
	}

	/**
	 * Gets the match result for the next token if the keyword matches the provided keyword, 
	 * otherwise returning null and not advancing past any input
	 * @param s The {@code Scanner} to read
	 * @param keyword The expected kewyord
	 * @param format The expected format
	 * @return The {@code MatchResult} of the next line of input if the format and keyword match, 
	 * otherwise {@code null}
	 * @see #mandatoryKVN(Scanner, String, Pattern)
	 */
	public static MatchResult optionalKVN(Scanner s, String keyword, Pattern format) {
		if (s.hasNext(format)) {
			MatchResult m = s.match();
			if (m.group("keyword").equals(keyword)) {
				s.next();
				return m;
			}
		}
		return null;
	}

	public static Map<String, String> readUserDefined(Scanner s) {
		Map<String, String> result = new TreeMap<>();
		MatchResult m;
		while(s.hasNext(USER_DEFINED)) {
			m = s.match();
			result.put(m.group("x"), m.group("value"));
		}
		return result;
	}

	/**
	 * If {@code condition} is true, it parses the provided keyword and format as a mandatory KVN 
	 * item. Otherwise, it is parsed as an optional KVN item.
	 * @param s The {@code Scanner} to read from
	 * @param keyword The expected keyword
	 * @param format The expected format
	 * @param condition Whether the given item is currently mandatory
	 * @return The {@code MatchResult} of the next line of input if the format and keyword match, 
	 * otherwise {@code null} if {@code condition} is {@code false}
	 * @throws InputMismatchException if the next line did not match the expected conditions and 
	 * {@code condition} was {@code true}
	 * @see #mandatoryKVN(Scanner, String, Pattern)
	 * @see #optionalKVN(Scanner, String, Pattern)
	 */
	public static MatchResult conditionalKVN(Scanner s, String keyword, Pattern format, boolean condition) {
		return condition ? mandatoryKVN(s, keyword, format) : optionalKVN(s, keyword, format);
	}

	/**
	 * If {@code match} is null, returns {@code defaultValue}. Otherwise, returns the text matched by 
	 * the group {@code group}
	 * @param match The {@code MatchResult} to look in
	 * @param group the group name to query
	 * @param defaultValue The default value to return if {@code match} is {@code null}
	 * @return the proper value
	 */
	public static String getOrDefault(MatchResult match, String group, String defaultValue) {
		return match == null ? defaultValue : match.group(group);
	}

	public static int getOrDefault(MatchResult match, String group, int defaultValue) {
		return match == null ? defaultValue : Integer.parseInt(match.group(group));
	}

	/**
	 * Attempts to parse the provided string as a {@code LocalDateTime} in either 
	 * "yyyy-MMM-ddTHH:mm:ss[.n...n][Z]" or "yyyy-DDDTHH:mm:ss[.n...n][Z]" formats, using as many 
	 * nanoseconds as are available. The only acceptable value for the zone is "Z", for UTC.
	 * <p>
	 * If {@code s} is {@code null}, then {@code null} is returned
	 * @param s The {@code String} to parse
	 * @return {@code null} if {@code s} is {@code null}. Otherwise, te date and time represented 
	 * in the {@code s}
	 */
	public static LocalDateTime parseTimestamp(String s) {
		if (s == null) {
			return null;
		}
		try {
			return LocalDateTime.parse(s, ISO_FORMAT);
		}
		catch (DateTimeParseException e) {
			return LocalDateTime.parse(s, DAYOFYEAR);
		}
	}

	/**
	 * Converts the date and time in {@code time} from UTC to the provided time system
	 * @param time
	 * @param timeSystem
	 * @return
	 */
	public static Instant inTimeSystem(LocalDateTime time, String timeSystem) {
		return time.toInstant(ZoneOffset.UTC);
	}

	public static double fromMatch(MatchResult match) {
		return Double.parseDouble(match.group("value"));
	}
}
