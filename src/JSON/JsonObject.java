package JSON;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;

/**
 * This class represents the JSON object data type, which resembles a mapping
 */
public class JsonObject extends JsonType implements SequencedMap<String, JsonType> {
	private static final Pattern SEP_OR_END = Pattern.compile(",|}");
	private SequencedMap<String, JsonType> entries;

	public JsonObject(Map<String,JsonType> entries) {
		this(new LinkedHashMap<>(entries));
	}

	private JsonObject(SequencedMap<String, JsonType> entries) {
		this.entries = entries;
	}

	public static JsonObject parse(Scanner s) {
		s.next("\\{");
		SequencedMap<String, JsonType> result = new LinkedHashMap<>();
		String follower;
		do {
			String key = JsonString.parse(s).getStringValue();
			s.next(":");
			JsonType val = JsonType.parse(s);
			result.putLast(key, val);
			follower = s.next(SEP_OR_END);
		} while (!follower.equals("}"));
		return new JsonObject(result);
	}

	public String toString() {
		StringBuilder result = new StringBuilder("{");
		for (String key : sequencedKeySet()) {
			result.append("\"");
			result.append(JsonString.escapeCharacters(key));
			result.append("\":");
			result.append(entries.get(key).toString());
			result.append(",");
		}
		result.deleteCharAt(result.length()-1);
		result.append("}");
		return result.toString();
	}

	/**
	 * Gets the value for the specified key as a {@code JsonObject}
	 * @param key The key to get
	 * @return The JSON object associated with the provided key if it exists
	 * @throw NoSuchElementException if there is not a mapping associated to the key or if the 
	 * value is not a JSON object
	 */
	public JsonObject getObject(String key) {
		JsonType pre = get(key);
		if (!(pre instanceof JsonObject)) {
			throw new NoSuchElementException("The value associated with \""+key+"\" was not a JSON object");
		}
		return (JsonObject) pre;
	}

	public Map<String, String> toStringMap() {
		Map<String,String> result = new LinkedHashMap<>();
		for (String el : sequencedKeySet()) {
			result.put(el, get(el).getStringValue());
		}
		return result;
	}
	
	//#region Map methods

	public int size() {
		return entries.size();
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public boolean containsKey(Object key) {
		if (! (key instanceof String)) {
			return false;
		}
		return entries.containsKey(key);
	}

	public boolean containsValue(Object value) {
		if (!(value instanceof JsonType)) {
			return false;
		}
		return entries.containsValue(value);
	}

	public JsonType get(Object key) {
		return entries.get(key);
	}

	public JsonType put(String key, JsonType value) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonType remove(Object key) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public void putAll(Map<? extends String, ? extends JsonType> m) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public void clear() {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public Set<String> keySet() {
		return Collections.unmodifiableSet(entries.keySet());//entries.keySet();
	}

	public Collection<JsonType> values() {
		return Collections.unmodifiableCollection(entries.values());
	}

	public Set<Map.Entry<String,JsonType>> entrySet() {
		return Collections.unmodifiableSet(entries.entrySet());
	}

	public boolean equals(Object other) {
		if (other instanceof JsonObject) {
			JsonObject o = (JsonObject) other;
			return entries.equals(o.entries);
		}
		return false;
	}

	public int hashCode() {
		return entries.hashCode();
	}

	public void replaceAll(BiFunction<? super String, ? super JsonType, ? extends JsonType> function) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}
	
	public JsonType putIfAbsent(String key, JsonType value) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public boolean remove(Object key, Object value) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
		//return false;
	}

	public boolean replace(String key, JsonType oldValue, JsonType newValue) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
		//return false;
	}

	public JsonType replace(String key, JsonType value) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonType computeIfAbsent(String key, Function<? super String, ? extends JsonType> mappingFunction) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonType computeIfPresent(String key, BiFunction<? super String, ? super JsonType, ? extends JsonType> remappingFunction) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonType compute(String key, BiFunction<? super String, ? super JsonType, ? extends JsonType> remappingFunction) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}
	public JsonType merge(String key, JsonType value, BiFunction<? super JsonType, ? super JsonType, ? extends JsonType> remappingFunction) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	//#endregion Map methods

	//#region SequencedMap methods

	public Map.Entry<String,JsonType> pollFirstEntry() {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public Map.Entry<String,JsonType> pollLastEntry() {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonObject reversed() {
		return new JsonObject(entries.reversed());
	}

	public JsonType putFirst(String key, JsonType value) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonType putLast(String key, JsonType value) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public SequencedSet<String> sequencedKeySet() {
		return Collections.unmodifiableSequencedSet(entries.sequencedKeySet());//entries.sequencedKeySet();
	}

	public SequencedCollection<JsonType> sequencedValues() {
		return Collections.unmodifiableSequencedCollection(entries.sequencedValues());
	}

	public SequencedSet<Map.Entry<String,JsonType>> sequencedEntrySet() {
		return Collections.unmodifiableSequencedSet(entries.sequencedEntrySet());
	}

	public Map.Entry<String,JsonType> firstEntry() {
		return entries.firstEntry();
	}

	public Map.Entry<String,JsonType> lastEntry() {
		return entries.lastEntry();
	}
	//#endregion SequencedMap methods
}
