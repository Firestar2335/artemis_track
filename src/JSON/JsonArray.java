package JSON;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;

public class JsonArray extends JsonType implements List<JsonType> {
	private static final Pattern SEP_OR_END = Pattern.compile("]|,");
	private JsonType[] arr;
	
	public JsonArray(Collection<? extends JsonType> elements) {
		arr = new JsonType[elements.size()];
		Iterator<? extends JsonType> iter = elements.iterator();
		for (int i = 0; i < arr.length; i++) {
			arr[i] = iter.next();
		}
	}

	public JsonArray(JsonType[] elements) {
		this(elements, 0, elements.length);
	}

	/**
	 * Copies the elements in the array between the specified indices to this object
	 * @param elements The elements
	 * @param from The start index, inclusive
	 * @param to The stop index, exclusive
	 */
	private JsonArray(JsonType[] elements, int from, int to) {
		arr = new JsonType[to-from];
		for (int i = from; i < to; i++) {
			arr[i] = elements[i];
		}
	}

	private JsonArray(JsonType[] elements, boolean copy) {
		if (copy) {
			arr = new JsonType[elements.length];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = elements[i];
			}
		}
		else {
			arr = elements;
		}
	}

	/**
	 * Creates an empty JsonArray
	 */
	public JsonArray() {
		arr = new JsonType[0];
	}

	public String toString() {
		StringBuilder result = new StringBuilder("\\[");
		for (JsonType el : arr) {
			result.append(el.toString());
			result.append(",");
		}
		result.deleteCharAt(result.length()-1);
		result.append("]");
		return result.toString();
	}

	public static JsonArray parse(Scanner s) {
		s.next("\\[");
		if (s.hasNext("]")) {//Empty array
			s.next();
			return new JsonArray();
		}
		String follower;
		List<JsonType> res = new ArrayList<>();
		do {
			res.add(JsonType.parse(s));
			follower = s.next(SEP_OR_END);
		} while (!follower.equals("]"));
		return new JsonArray(res);
	}

	//#region List methods

	public int size() {
		return arr.length;
	}

	public boolean isEmpty() {
		return arr.length == 0;
	}

	public boolean contains(Object o) {
		if (o instanceof JsonType) {
			JsonType other = (JsonType) o;
			for (int i = 0; i < arr.length; i++) {
				if (arr[i].equals(other)) {
					return true;
				}
			}
		}
		return false;
	}

	public Iterator<JsonType> iterator() {
		return new Iter();
	}

	public Object[] toArray() {
		Object[] res = new Object[arr.length];
		for (int i = 0; i < arr.length; i++) {
			res[i] = arr[i];
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		if (a == null) {
			throw new NullPointerException();
		}
		//Class<? extends T[]> arrType = (Class<? extends T[]>) a.getClass();
		Class<?> elemType = a.getClass().getComponentType();
		for (int i = 0; i < arr.length; i++) {
			if (!elemType.isAssignableFrom(JsonType.class)) {
				throw new ArrayStoreException();
			}
		}
		if (arr.length <= a.length) {
			for (int i = 0; i < arr.length; i++) {
				a[i] = (T) arr[i];//elemType.cast(arr[i]);
			}
			if (arr.length < a.length) {
				a[arr.length] = null;
			}
			return a;
		}
		return (T[]) Arrays.copyOf(arr,arr.length,a.getClass());
	}

	public boolean add(JsonType e) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public boolean remove(Object e) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public boolean containsAll(Collection<?> c) {
		Iterator<?> iter = c.iterator();
		while (iter.hasNext()) {
			if(!contains(iter.next())) {
				return false;
			}
		}
		return true;
	}

	public boolean addAll(Collection<? extends JsonType> c) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public boolean addAll(int index, Collection<? extends JsonType> c) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public void replaceAll(UnaryOperator<JsonType> operator) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public void sort(Comparator<? super JsonType> c) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public void clear() {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public boolean equals(Object o) {
		if (o instanceof JsonArray) {
			JsonArray other = (JsonArray) o;
			return Arrays.equals(arr,other.arr);
		}
		return false;
	}

	public int hashCode() {
		return Arrays.hashCode(arr);
	}

	public JsonType get(int index) {
		if (index < 0 || index >= arr.length) {
			throw new IndexOutOfBoundsException(index);
		}
		return arr[index];
	}
	public JsonType set(int index, JsonType element) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public void add(int index, JsonType element) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonType remove(int index) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public int indexOf(Object o) {
		if (o instanceof JsonType) {
			JsonType other = (JsonType) o;
			for (int i = 0; i < arr.length; i++) {
				if (arr[i].equals(other)) {
					return i;
				}
			}
		}
		return -1;
	}

	public int lastIndexOf(Object o) {
		if (o instanceof JsonType) {
			JsonType other = (JsonType) o;
			for (int i = arr.length-1; i >= 0; i--) {
				if (arr[i].equals(other)) {
					return i;
				}
			}
		}
		return -1;
	}

	public ListIterator<JsonType> listIterator() {
		return new Iter();
	}

	public ListIterator<JsonType> listIterator(int index) {
		if (index < 0 || index > arr.length) {
			throw new IndexOutOfBoundsException(index);
		}
		return new Iter(index);
	}

	public JsonArray subList(int fromIndex, int toIndex) {
		if (fromIndex < 0) {
			throw new IndexOutOfBoundsException(fromIndex);
		}
		else if (toIndex > arr.length) {
			throw new IndexOutOfBoundsException(toIndex);
		}
		else if (fromIndex > toIndex) {
			throw new IndexOutOfBoundsException("fromIndex ("+fromIndex+") was larger than toIndex ("+toIndex+")");
		}
		return new JsonArray(arr,fromIndex,toIndex);
	}

	public void addFirst(JsonType e) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public void addLast(JsonType e) {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonType getFirst() {
		return arr[0];
	}

	public JsonType getLast() {
		return arr[arr.length-1];
	}

	public JsonType removeFirst() {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonType removeLast() {
		throw new UnsupportedOperationException(READ_ONLY_ERROR);
	}

	public JsonArray reversed() {
		JsonType[] result = new JsonType[arr.length];
		for (int i = 0; i < arr.length; i++) {
			result[i] = arr[arr.length-1-i];
		}
		return new JsonArray(result, false);
	}
	
	private class Iter implements ListIterator<JsonType> {
		private int currentIndex;

		public Iter() {
			this(0);
		}

		public Iter(int start) {
			currentIndex = start;
		}

		public boolean hasNext() {
			return currentIndex < arr.length;
		}

		public boolean hasPrevious() {
			return currentIndex > 0;
		}

		public int nextIndex() {
			return currentIndex;
		}

		public int previousIndex() {
			return currentIndex-1;
		}

		public JsonType next() {
			if (currentIndex >= arr.length) {
				throw new NoSuchElementException("Iterator has reached the end of the list");
			}
			currentIndex++;
			return arr[currentIndex-1];
		}

		public JsonType previous() {
			if (currentIndex <= 0) {
				throw new NoSuchElementException("Iterator has reached the beginning of the list");
			}
			currentIndex--;
			return arr[currentIndex];
		}

		public void remove() {
			throw new UnsupportedOperationException(READ_ONLY_ERROR);
		}

		public void add(JsonType e) {
			throw new UnsupportedOperationException(READ_ONLY_ERROR);
		}

		public void set(JsonType e) {
			throw new UnsupportedOperationException(READ_ONLY_ERROR);
		}
	}
	//#endregion List methods
}
