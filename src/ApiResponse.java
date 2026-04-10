import java.util.*;
import java.time.*;
import java.time.format.*;

public class ApiResponse {
	public static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy'/'MM'/'dd' 'HH':'mm':'ss").withZone(ZoneOffset.ofHours(-5));

	private static final String PREFIX = "Parameter_";

	/** The generation of the file, representing a date in microseconds */
	public final long genMicro;

	public final ZonedDateTime fileDate;
	public final String activity;
	public final int type;

	private final Map<Integer,Parameter> parameters;

	public ApiResponse(long generation, ZonedDateTime fileDate, String activity, int type, Collection<? extends Parameter> params) {
		this.genMicro = generation;
		this.fileDate = fileDate;
		this.activity = activity;
		this.type = type;
		parameters = new TreeMap<>();
		Iterator<? extends Parameter> iter = params.iterator();
		while (iter.hasNext()) {
			Parameter p = iter.next();
			parameters.put(p.number, p);
		}
	}

	public ApiResponse(long generation, Map<String,String> fileData, Collection<? extends Parameter> parameters) {
		this(generation, ZonedDateTime.parse(fileData.get("Date"),FILE_FORMAT),fileData.get("Activity"),Integer.parseInt(fileData.get("Type")),parameters);
	}

	//public Map<Integer, Parameter> getParameters() {
	//	return new TreeMap<>(parameters);
	//}

	public Parameter getFromID(int num) {
		if (parameters.containsKey(num)) {
			return parameters.get(num);
		}
		throw new NoSuchElementException("There was not a parameter with the given ID");
	}

	public Parameter getFromKey(String key) {
		try {
			if (key.startsWith(PREFIX)) {
				return getFromID(Integer.valueOf(key.substring(PREFIX.length())));
			}
			else {
				return getFromID(Integer.valueOf(key));
			}
		}
		catch (NumberFormatException e) {
			throw new NoSuchElementException("There was not a parameter corresponding to the given key");
		}
	}

	public boolean containsID(int num) {
		return parameters.containsKey(num);
	}

	public boolean containsKey(String key) {
		try {
			if (key.startsWith(PREFIX)) {
				return parameters.containsKey(Integer.valueOf(key.substring(PREFIX.length())));
			}
			else {
				return parameters.containsKey(Integer.valueOf(key));
			}
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	public String toString() {
		String result = "{\n\t\"File\": {\n\t\t\"Date\": \"";
		result += fileDate.format(FILE_FORMAT);
		result += "\",\n\t\t\"Activity\": \"";
		result += activity;
		result += "\",\n\t\t\"Type: ";
		result += type;
		result += "\n\t}";
		for (Integer i : parameters.keySet()) {
			result += ",\n\t\""+PREFIX+i.toString()+"\": ";
			result += parameters.get(i);
		}
		result += "\n}";
		return result;
	}

	/**
	 * Returns the generation of this file as an instant
	 * @return
	 */
	public Instant getGenerationInstant() {
		return Instant.ofEpochSecond(genMicro/1_000_000l, genMicro % 1_000_000l);
	}
}
