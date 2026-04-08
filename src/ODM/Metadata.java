package ODM;
import java.time.temporal.*;
import java.util.*;

public interface Metadata {
	/** Whether accessing a null field is an error */
	public static final boolean STRICTACCESS = true;

	/**
	 * Gets the associated metadata value as a String.
	 * @param key
	 * @return
	 * @throws NoSuchElementException if the key does not exist.
	 */
	public String getString(String key);

	/**
	 * Gets the associated metadata value as an integer.
	 * @param key
	 * @return
	 * @throws NoSuchElementException if the key does not exist
	 * @throws ClassCastException if the value cannot be converted to an integer.
	 */
	public int getInt(String key);

	/**
	 * Gets the associated metadata value as a double
	 * @param key
	 * @return
	 * @throws NoSuchElementException if the key does not exist
	 * @throws ClassCastException if the value cannot be converted to a double.
	 */
	public double getDouble(String key);

	/**
	 * Gets the associated metadata value as a Temporal object.
	 * @param key
	 * @return
	 * @throws NoSuchElementException if the key does not exist
	 * @throws ClassCastException if the valuecannot be converted to a temporal object.
	 */
	public Temporal getDate(String key);

	/**
	 * Returns the set of keys that are currently defined for this instance.
	 * <p>
	 * This is the set of keys that do not raise either {@code IllegalStateException} or 
	 * {@code NoSuchElementException} when used as the argument to {@code getString}
	 * @return
	 */
	public Set<String> getKeys();

	/**
	 * Returns the set of keys that could be defined for this object.
	 * <p>
	 * This is the set of keys that do not raise {@code NoSuchElementException} when used as the 
	 * argument to {@code getString}
	 * @return
	 */
	public Set<String> getAllValidKeys();
}
