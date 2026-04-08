package JSON;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class JsonDocument {
	protected static final Pattern DELIM = Pattern.compile("\\p{javaWhitespace}+|\\p{javaWhitespace}*(?=[\\[\\]{}:,])|(?<=[\\\\[\\\\]{}:,])\\p{javaWhitespace}*");

	public final JsonType root;

	public JsonDocument(JsonType root) {
		this.root = root;
	}

	/**
	 * Reads the JSON documet contained in the provided String
	 * @param doc
	 * @return
	 */
	public static JsonDocument read(String doc) {
		Scanner r = new Scanner(doc);
		r.useDelimiter(DELIM);
		JsonType root = JsonType.parse(r);
		r.close();
		return new JsonDocument(root);
	}

	public static JsonDocument read(File f) {
		Scanner r;
		try {
			r = new Scanner(f);
		}
		catch (IOException e) {
			System.err.println(e);
			return null;
		}
		r.useDelimiter(DELIM);
		JsonType root = JsonType.parse(r);
		r.close();
		return new JsonDocument(root);
	}

	/**
	 * Reads the provided document and parses and returns the top level JSON object.
	 * @param doc
	 * @return
	 */
	public static JsonObject readObject(String doc) {
		Scanner r = new Scanner(doc);
		r.useDelimiter(DELIM);
		JsonObject root = JsonObject.parse(r);
		r.close();
		return root;
	}

	public JsonType getRoot() {
		return root;
	}
	
	public String toString() {
		return "Document("+root.toString() + ")";
	}

	public void write(File f) throws FileNotFoundException {
		PrintStream out = new PrintStream(f);
		write(out);
		out.close();
	}

	public void write(PrintStream out) {
		out.print(root.toString());
	}
}
