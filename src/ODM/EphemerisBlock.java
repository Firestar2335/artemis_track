package ODM;
import java.util.*;
import java.util.regex.*;
import java.time.*;

public class EphemerisBlock {
	private static final Pattern EPHEMERIS = Pattern.compile(Patterns.TIMESTAMP+"(?: +"+Patterns.FIXEDORFLOAT+"){6}(?:(?: +"+Patterns.FIXEDORFLOAT+"){3})?");
	public final String comment;
	private final StateVector[] ephemerisData;

	public EphemerisBlock(String comment, Collection<? extends StateVector> data) {
		this.comment = comment;
		ephemerisData = new StateVector[data.size()];
		Iterator<? extends StateVector> iter = data.iterator();
		for (int i = 0; i < ephemerisData.length; i++) {
			ephemerisData[i] = iter.next();
		}
	}

	public static EphemerisBlock fromScanner(Scanner s, String timeSystem) {
		String overallComment = Patterns.optionalComment(s);
		String com = overallComment;
		List<StateVector> vecs = new ArrayList<>();
		while (s.hasNext(EPHEMERIS)) {
			vecs.add(StateVector.fromString(s.next(), timeSystem, com));
			com = Patterns.optionalComment(s);
		}
		return new EphemerisBlock(overallComment, vecs);
	}

	public StateVector[] getEphemerisData() {
		return Arrays.copyOf(ephemerisData, ephemerisData.length);
	}

	public String toString() {
		String result = "Ephemerides(";
		if (comment != null) {
			result += "comment=\"";
			result += comment;
			result += ", ";
		}
		result += Arrays.toString(ephemerisData);
		result += ")";
		return result;
	}

	/**
	 * Gets the state vector at index {@code index}
	 * @param index The index
	 * @return The state vector at the specified index
	 */
	public StateVector get(int index) {
		return ephemerisData[index];
	}

	/**
	 * Returns the largest index {@code i} where {@code ephemerisData[i].epoch <= t}
	 * @param t The instant to search for
	 * @return The largest index {@code i} whose data occurs at or before the provided instant, -1 
	 * if {@code t} occurs before all of the data in the list
	 */
	public int search(Instant t) {
		return floorBinarySearch(t, 0, ephemerisData.length);
	}

	/**
	 * Performs a binary search in ephemerisData
	 * @param searchItem the {@code Instant} to be searching for
	 * @param from The start index, inclusive
	 * @param to The stop index, exclusive
	 * @return The largest index {@code i} in {@code from <= i < to} such that ephemerisData[i].compareTo(searchItem) <= 0
	 */
	private int floorBinarySearch(Instant searchItem, int from, int to) {
		int mid = (from + to)/2;
		int c = ephemerisData[mid].epoch.compareTo(searchItem);
		if (c == 0) {
			return mid;
		}
		else if (c < 0) {
			if (from + 1 == to) {
				return from;
			}
			else {
				return floorBinarySearch(searchItem,mid, to);
			}
		}
		else {
			if (from + 1 == to) {
				return -1;
			}
			else {
				return floorBinarySearch(searchItem,from,mid);
			}
		}
	}
}
