import java.io.*;
import java.util.*;
import java.util.concurrent.*;
//import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import JSON.*;

public abstract class DataGetter implements Runnable {
	protected static final String PREFIX = "Telemetry-";
	protected static final String SUFFIX = ".json";
	protected static final Pattern TELEMETRY_PATTERN = Pattern.compile("\\.*"+PREFIX+"(\\d+)\\"+SUFFIX);
	
	/** The milliseconds of delay to wait */
	protected final long delayMilli;
	/** The extra nanoseconds to wait */
	protected final int delayNano;
	/** The queue to the main process */
	protected final BlockingQueue<ApiResponse> snd;

	/** The parent thread */
	protected final Thread parent;

	private boolean shouldQuit;

	private boolean paused;


	public DataGetter(BlockingQueue<ApiResponse> recv, Thread parent, long delayMilli, int delayNano) {
		int k = Math.floorDiv(delayNano, 1_000_000);
		if (k > 0 && delayMilli > Long.MAX_VALUE - k) {
			k = (int)(Long.MAX_VALUE - delayMilli);
		}
		else if (k < 0 && delayMilli < Long.MIN_VALUE - k){
			k = (int) (Long.MIN_VALUE - delayMilli);
		}
		this.delayMilli = delayMilli + k;
		this.delayNano = Math.min(999_999, delayNano - 1_000_000 * k);
		if (delayMilli < 0) {
			throw new IllegalArgumentException("Total delay time was negative");
		}
		else if (delayMilli == 0 && delayNano == 0) {
			throw new IllegalArgumentException("Total delay time was 0");
		}
		snd = recv;
		this.parent = parent;
		shouldQuit = false;
		paused = false;
	}

	public DataGetter(BlockingQueue<ApiResponse> recv, Thread parent, long delayMilli) {
		if (delayMilli < 0) {
			throw new IllegalArgumentException("Total delay time was negative");
		}
		if (delayMilli == 0) {
			throw new IllegalArgumentException("Total delay time was 0");
		}
		this.delayMilli = delayMilli;
		delayNano = 0;
		snd = recv;
		this.parent = parent;
		shouldQuit = false;
		paused = false;
	}

	public DataGetter(BlockingQueue<ApiResponse> recv, Thread parent, long timeout, TimeUnit unit) {
		if (timeout <= 0) {
			throw new IllegalArgumentException("Timeout was not positive");
		}
		switch (unit) {
			case NANOSECONDS:
				delayNano = (int) (timeout % 1_000_000l);
				delayMilli = timeout / 1_000_000l;
				break;
			case MICROSECONDS:
				delayNano = 1000 * (int) (timeout % 1000l);
				delayMilli = timeout / 1000l;
				break;
			case MILLISECONDS:
				delayNano = 0;
				delayMilli = timeout;
				break;
			default:
				delayMilli = unit.toMillis(timeout);
				if (unit.convert(delayMilli, TimeUnit.MILLISECONDS) != timeout) {
					delayNano = 999999;
				}
				else {
					delayNano = 0;
				}
		}
		snd = recv;
		this.parent = parent;
		shouldQuit = false;
		paused = false;
	}

	/**
	 * Gets the HTTP status code of the most recent completed request
	 * @return
	 */
	public abstract int getStatusCode();

	/**
	 * Returns the minimum generation accesible by this object
	 * @return
	 */
	public abstract long getMinGeneration();

	/**
	 * Returns the maximum generation accessible by this object
	 * @return
	 */
	public abstract long getMaxGeneration();

	/**
	 * Returns the current generation of this object
	 * @return
	 */
	public abstract long getGeneration();

	/**
	 * Submits a request to obtain the telemetry for the given generation, if possible.
	 * @param gen The timestamp of the telemetry file, in microseconds since the epoch.
	 */
	public abstract void requestGeneration(long gen);

	public void quit() {
		shouldQuit = true;
	}

	public boolean shouldQuit() {
		return shouldQuit;
	}

	public boolean isPaused() {
		return paused;
	}

	public void pause() {
		paused = true;
	}

	public synchronized void unpause() {
		paused = false;
		notifyAll();
	}

	protected synchronized void pauseWait() {
		while (isPaused() && !shouldQuit()) {
			try {
				wait();
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Logs the provided JSON document in the log directory with the provided generation
	 * @param doc
	 * @param generation
	 */
	protected static void logJSON(JsonDocument doc, long generation) {
		try{
			File logFile = new File("./log/"+PREFIX+generation+SUFFIX);
			if (!logFile.exists()){
				doc.write(logFile);
			}
		}
		catch (FileNotFoundException e) {
			
		}
	}

	/**
	 * Produces an {@code ApuResponse} object from the provided JSON document
	 * @param json The JSON document to parse the response from
	 * @param generation The generation of the document
	 * @return
	 */
	public static ApiResponse parseData(JsonDocument json, long generation) {
		JsonType rootType = json.getRoot();
		if (!(rootType instanceof JsonObject)) {
			throw new IllegalArgumentException("Provided JSON document was not an object");
		}
		JsonObject root = (JsonObject) rootType;
		JsonObject fileData = root.getObject("File");
		List<Parameter> parameters = new ArrayList<>();
		for (String key : root.keySet()) {
			if (key.equals("File")) {
				continue;
			}
			parameters.add(Parameter.fromMap(root.getObject(key).toStringMap()));
		}

		return new ApiResponse(generation, fileData.toStringMap(), parameters);
	}

	/**
	 * Sends the ApiResponse object corresponding to the given document on the connection
	 * @param doc
	 * @param generation
	 * @throws InterruptedException
	 */
	protected void send(JsonDocument doc, long generation) throws InterruptedException {
		try {// (newData.getRoot().getObject("File").get("Type").getIntValue() == 4) {
			snd.put(parseData(doc, generation));
			parent.interrupt();
		}
		catch (NoSuchElementException e) {
			System.err.println("Malformed JSON telemetry received at generation "+generation);
		}
	}
}
