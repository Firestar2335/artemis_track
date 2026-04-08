import java.io.*;
import java.util.concurrent.*;
import java.util.*;
import java.net.*;
import java.net.http.*;
import JSON.*;


public class DataRequester implements Runnable {
	private static final boolean LOG = true;


	/** The google api storage url to retrieve from */
	private final String URL; //"https://storage.googleapis.com/storage/v1/b/p-2-cen1/o/October%2F1%2FOctober_105_1.txt";
	/** The most recent generation that was retrieved */
	private long lastGeneration;
	
	/** The queue to the main process */
	private final BlockingQueue<ApiResponse> snd;

	/** The milliseconds of delay to wait */
	private final long delayMilli;
	/** The extra nanoseconds to wait */
	private final int delayNano;

	/** The parent thread */
	private final Thread parent;

	private volatile int httpCode;

	public DataRequester(String URL, BlockingQueue<ApiResponse> recv, Thread parent, long delayMilli, int delayNano) {
		int k = Math.floorDiv(delayNano, 1_000_000);
		if (k > 0 && delayMilli > Long.MAX_VALUE - k) {
			k = (int)(Long.MAX_VALUE - delayMilli);
		}
		else if (k < 0 && delayMilli < Long.MIN_VALUE - k){
			k = (int) (Long.MIN_VALUE - delayMilli);
		}
		this.delayMilli = delayMilli + k;
		this.delayNano = Math.min(999_999, delayNano - 1_000_000 * k);
		//this.delayMilli = delayMilli + Math.floorDiv(delayNano, 1_000_000);
		//this.delayNano = Math.floorMod(delayNano,1_000_000);
		if (delayMilli < 0) {
			throw new IllegalArgumentException("Total delay time was negative");
		}
		else if (delayMilli == 0 && delayNano == 0) {
			throw new IllegalArgumentException("Total delay time was 0");
		}
		try {
			new URI(URL);
			this.URL = URL;
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Provided URL was invalid", e);
		}
		this.snd = recv;
		lastGeneration = 0;
		this.parent = parent;
		httpCode = 200;
	}

	public DataRequester(String URL, BlockingQueue<ApiResponse> recv, Thread parent, long delayMilli) {
		this(URL, recv, parent, delayMilli, 0);
	}

	public DataRequester(String URL, BlockingQueue<ApiResponse> recv, Thread parent, long timeout, TimeUnit unit) {
		if (timeout <= 0) {
			throw new IllegalArgumentException("Timeout was not positive");
		}
		long milli;
		int nano;
		switch (unit) {
			case NANOSECONDS:
				nano = (int) (timeout % 1_000_000l);
				milli = timeout / 1_000_000l;
				break;
			case MICROSECONDS:
				nano = 1000 * (int) (timeout % 1000);
				milli = timeout / 1000l;
				break;
			case MILLISECONDS:
				nano = 0;
				milli = timeout;
				break;
			default:
				milli = unit.toMillis(timeout);
				if (unit.convert(milli, TimeUnit.MILLISECONDS) != timeout) {
					nano = 999999;
				}
				else {
					nano = 0;
				}
		}
		this(URL, recv, parent, milli, nano);
	}

	public void run() {
		HttpClient client = HttpClient.newHttpClient();
		httpCode = 200;
		try {
			HttpRequest  metaRequest = HttpRequest.newBuilder(URI.create(URL + "?fields=generation")).GET().build();
			while (true) {
				HttpResponse<String> metaResponse = client.send(metaRequest, HttpResponse.BodyHandlers.ofString());
				long gen = retrieveGeneration(metaResponse);
				if (gen > lastGeneration) {
					HttpRequest dataRequest = HttpRequest.newBuilder(URI.create(URL+"?alt=media&generation="+gen)).GET().build();
					HttpResponse<String> dataResponse = client.send(dataRequest,HttpResponse.BodyHandlers.ofString());
					httpCode = dataResponse.statusCode();
					verifyCode();
					if (httpCode != 304) {
						JsonDocument newData = parseJSON(dataResponse);
						if (LOG) {
							logJSON(newData,gen);
						}
						snd.put(parseData(newData, gen));
						lastGeneration = gen;
						parent.interrupt();
					}
				}
				Thread.sleep(delayMilli, delayNano);
			}
		}
		catch (IOException e) {
			httpCode = -1;
			throw new IllegalArgumentException(e.getMessage(),e);
		}
		catch (InterruptedException e) {

		}
		finally {
			client.close();
			parent.interrupt();
		}
	}

	
	private static void logJSON(JsonDocument doc, long generation) {
		try{
			File logFile = new File("./log/Telemetry-"+generation+".json");
			if (!logFile.exists()){
				doc.write(logFile);
			}
		}
		catch (FileNotFoundException e) {
			
		}
	}

	/**
	 * Gets the HTTP status code of the most recent completed request
	 * @return
	 */
	public int getStatusCode() {
		return httpCode;
	}

	/**
	 * Checks that the status code of the most recent response is not an error
	 * @throws IOException if the most recent status code is neither 304 nor a 2XX code
	 */
	private void verifyCode() throws IOException {
		if (httpCode != 304 && httpCode/100 != 2) {
			throw new IOException("HTTP "+httpCode);
		}
	}

	/**
	 * Parses the JSON document in the response. If there is an error, {@code null} is returned
	 * @param response
	 * @return
	 */
	private static JsonDocument parseJSON(HttpResponse<String> response) {
		try {
			return JsonDocument.read(response.body());
		}
		catch (Exception e) {
			return null;
		}
	}

	private static ApiResponse parseData(JsonDocument json, long generation) {
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

	private long retrieveGeneration(HttpResponse<String> response) throws IOException {
		httpCode = response.statusCode();
		verifyCode();
		if (httpCode == 304) {
			return lastGeneration;
		}
		JsonDocument resp = parseJSON(response);
		if (resp == null) {
			return lastGeneration;
		}
		return Long.parseLong(resp.root.get("generation").getStringValue());
	}
}
