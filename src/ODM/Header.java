package ODM;
import java.time.*;

public final class Header {
	public final String version;
	public final String comment;
	public final String classification;
	public final ZonedDateTime creationDate;
	public final String originator;
	public final String messageID;

	public Header(String version, String comment, String classification, ZonedDateTime creationDate, String originator, String messageID) {
		this.version = version;
		this.comment = comment;
		this.classification = classification;
		this.creationDate = creationDate;
		this.originator = originator;
		this.messageID = messageID;
	}

	public String toString() {
		String result = "Header(version=";
		result += version;
		if (comment != null) {
			result += ", comment=\"";
			result += comment;
			result += "\"";
		}
		if (classification != null) {
		result += ", classification=\"";
		result += classification;
		result += "\"";
		}
		result += ", creationDate=";
		result += creationDate.toString();
		result += ", originator=\"";
		result += originator;
		result += "\"";
		if (messageID != null) {
			result += ", messageID=\"";
			result += messageID;
			result += "\"";
		}
		result += ")";
		return result;
	}
}
