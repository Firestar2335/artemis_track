import java.io.*;
import java.util.concurrent.*;
import java.net.*;
import java.net.http.*;
import JSON.*;


public class NetRequester extends DataGetter {
	/** The google api storage url to retrieve from */
	private final String URL; //"https://storage.googleapis.com/storage/v1/b/p-2-cen1/o/October%2F1%2FOctober_105_1.txt";
	/** The most recent generation that was retrieved */
	private long lastGeneration;

	/* * The milliseconds of delay to wait */
	//private final long delayMilli;
	/* * The extra nanoseconds to wait */
	//private final int delayNano;

	private volatile int httpCode;

	private boolean log;

	public NetRequester(String URL, boolean makeLogFiles, BlockingQueue<ApiResponse> recv, Thread parent, long delayMilli, int delayNano) {
		super(recv, parent, delayMilli, delayNano);
		validateURL(URL);
		this.URL = URL;
		lastGeneration = 0;
		httpCode = 200;
		log = makeLogFiles;
	}

	public NetRequester(String URL, boolean makeLogFiles, BlockingQueue<ApiResponse> recv, Thread parent, long delayMilli) {
		super(recv, parent, delayMilli);
		validateURL(URL);
		this.URL = URL;
		lastGeneration = 0;
		httpCode = 200;
		log = makeLogFiles;
	}

	public NetRequester(String URL, boolean makeLogFiles, BlockingQueue<ApiResponse> recv, Thread parent, long timeout, TimeUnit unit) {
		super(recv, parent, timeout, unit);
		validateURL(URL);
		this.URL = URL;
		lastGeneration = 0;
		httpCode = 200;
		log = makeLogFiles;
	}

	/**
	 * Throws {@code IllegalArgumentException} if the provided url is not valid
	 * @param url The URL to validat
	 * @throws IllegalArgumentException if {@code url} is not a valid URI
	 */
	private static void validateURL(String url) {
		try {
			new URI(url);
		} 
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Provided URL was invalid", e);
		}
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
						if (log) {
							logJSON(newData,gen);
						}
						send(newData, gen);
						lastGeneration = gen;
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
