import JSON.*;
import java.io.*;
import java.time.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class LogReplay extends DataGetter {

	/** The directory to read the logged JSON files from */
	private final File logDir;
	/** The time that the replay started */
	private final Instant startTime;
	/** The first generation to display */
	private final long startGen;
	/** The rate at which to scale playback */
	private final double timeScale;

	/** The most recent timestamp, in microseconds */
	private long lastGen;

	private int httpCode;

	public LogReplay(File logDir, long startGen, double timeScale, BlockingQueue<ApiResponse> recv, Thread parent, long delayMilli, int delayNano) {
		super(recv, parent, delayMilli, delayNano);
		if (!logDir.isDirectory()) {
			throw new IllegalArgumentException("logDir was not a directory");
		}
		this.logDir = logDir;
		this.startGen = startGen;
		this.timeScale = timeScale;
		this.startTime = Instant.now();
		httpCode = 200;
	}

	public LogReplay(File logDir, long startGen, double timeScale, BlockingQueue<ApiResponse> recv, Thread parent,  long delayMilli) {
		super(recv, parent, delayMilli);
		if (!logDir.isDirectory()) {
			throw new IllegalArgumentException("logDir was not a directory");
		}
		this.logDir = logDir;
		this.startGen = startGen;
		this.timeScale = timeScale;
		this.startTime = Instant.now();
		httpCode = 200;
	}

	public LogReplay(File logDir, long startGen, double timeScale, BlockingQueue<ApiResponse> recv, Thread parent, long timeout, TimeUnit unit) {
		super(recv, parent, timeout, unit);
		if (!logDir.isDirectory()) {
			throw new IllegalArgumentException("logDir was not a directory");
		}
		this.logDir = logDir;
		this.startGen = startGen;
		this.timeScale = timeScale;
		this.startTime = Instant.now();
		httpCode = 200;
	}

	public void run() {
		httpCode = 200;
		File[] files;
		try {
			while (true) {
				files = logDir.listFiles(new LogFilter(getCurrentGen()));
				File best = getLatestGen(files);
				if (best == null) {
					httpCode = 304;
				}
				else {
					httpCode = 200;
					lastGen = getFileGen(best);
					JsonDocument doc = JsonDocument.read(best);
					send(doc, lastGen);//ApiResponse resp = parseData(doc,curGen);
				}
				Thread.sleep(delayMilli,delayNano);
			}
		}
		catch (InterruptedException e) {

		}
		finally {
			parent.interrupt();
		}
	}

	/**
	 * Returns the file with the largest generation 
	 * @param files
	 * @return
	 */
	private static File getLatestGen(File[] files) {
		File best = null;
		long bestGen = 0;
		long g;
		for (File f : files) {
			try {
				g = getFileGen(f);
			} catch (NumberFormatException e) {
				continue;
			}
			if (g > bestGen) {
				bestGen = g;
				best = f;
			}
		}
		return best;
	}

	private static long getFileGen(File file) throws NumberFormatException {
		String n = file.getName();
		return Long.parseLong(n,PREFIX.length(),n.length()-SUFFIX.length(),10);
	}

	/**
	 * Computes the proper generation for the current timestamp
	 * @return
	 */
	private long getCurrentGen() {
		Duration delta = Duration.between(startTime, Instant.now());
		return startGen + (long) ((delta.getSeconds()*1_000_000.0 + delta.getNano()/1000.0)*timeScale);
	}

	public int getStatusCode() {
		return httpCode;
	}

	private class LogFilter implements FilenameFilter {
		private long maxGen;
		
		public LogFilter(long max) {
			maxGen = max;
		}

		public boolean accept(File dir, String name) {
			Matcher m = TELEMETRY_PATTERN.matcher(name);
			if (m.matches()) {
				long g = Long.parseLong(m.group(1));
				if (lastGen < g && g <= maxGen) {
					return true;
				}
			}
			return false;
		}
	}
}
