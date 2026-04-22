import JSON.*;
import java.io.*;
import java.time.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class LogReplay extends DataGetter {

	/** The directory to read the logged JSON files from */
	private final File logDir;
	/** The time that the replay started */
	private Instant startTime;
	/** The first generation to display */
	private long startGen;
	/** The rate at which to scale playback */
	private final double timeScale;

	private Instant pauseStart;

	/** The most recent timestamp, in microseconds */
	private long lastGen;

	private int httpCode;

	private Thread thisThread;

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

	public LogReplay(File logDir, long startGen, double timeScale, BlockingQueue<ApiResponse> recv, Thread parent, long delayMilli) {
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
		thisThread = Thread.currentThread();
		try {
			while (!shouldQuit()) {
				try {
					if (isPaused()) {
						pauseWait();
						continue;
					}
					files = logDir.listFiles(new LogFilter(lastGen + 1, getCurrentGen()));
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
				} catch (InterruptedException e) {

				}
			}
		}
		finally {
			httpCode = -1;
			parent.interrupt();
			thisThread = null;
		}
	}

	public long getMinGeneration() {
		try {
			File[] files = logDir.listFiles(new LogFilter());
			return getFileGen(getFirstGen(files));
		} catch (NumberFormatException e) {
			return Long.MIN_VALUE;
		}
	}
	
	public long getMaxGeneration() {
		try {
			File[] files = logDir.listFiles(new LogFilter());
			return getFileGen(getLatestGen(files));
		} catch (NumberFormatException e) {
			return Long.MAX_VALUE;
		}
	}

	public long getGeneration() {
		return lastGen;
	}

	public void requestGeneration(long gen) {
		startTime = Instant.now();
		pauseStart = null;
		startGen = gen;
		lastGen = gen-1;
		if (thisThread != null) {
			thisThread.interrupt();
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

	/**
	 * Returns the file with the smallest generation
	 * @param files
	 * @return
	 */
	private static File getFirstGen(File[] files) {
		File best = null;
		long bestGen = Long.MAX_VALUE;
		long g;
		for (File f : files) {
			try {
				g = getFileGen(f);
			} catch (NumberFormatException e) {
				continue;
			}
			if (g < bestGen) {
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

	public void pause() {
		super.pause();
		pauseStart = Instant.now();
	}

	public synchronized void unpause() {
		if (isPaused()) {
			Instant pauseStop = Instant.now();
			if (pauseStart == null) {
				startTime = pauseStop;
			}
			else {
				startTime = startTime.plus(Duration.between(pauseStart, pauseStop));
				pauseStart = null;
			}
		}
		super.unpause();
	}

	private static class LogFilter implements FilenameFilter {
		private long minGen;
		private long maxGen;

		/**
		 * Creates a filter that accepts files with any generation
		 */
		public LogFilter() {
			this(Long.MIN_VALUE, Long.MAX_VALUE);
		}
		
		/**
		 * Creates a filter that only accepts files with generations between the min and max, 
		 * inclusive
		 * @param min
		 * @param max
		 */
		public LogFilter(long min, long max) {
			minGen = min;
			maxGen = max;
		}

		public boolean accept(File dir, String name) {
			Matcher m = TELEMETRY_PATTERN.matcher(name);
			if (m.matches()) {
				long g = Long.parseLong(m.group(1));
				if (minGen <= g && g <= maxGen) {
					return true;
				}
			}
			return false;
		}
	}
}
