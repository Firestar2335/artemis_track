package ODM;
import java.util.*;
import java.time.*;

public class OrbitalEphemerisMessage extends OrbitalDataMessage {
	private final Segment[] segments;

	public OrbitalEphemerisMessage(Header h, OEMMetadata m, Collection<? extends Segment> segments) {
		super(h,m);
		this.segments = new Segment[segments.size()];
		Iterator<? extends Segment> iter = segments.iterator();
		for (int i = 0; i < this.segments.length; i++) {
			this.segments[i] = iter.next();
		}
	}
	

	public static OrbitalEphemerisMessage fromScanner(Scanner s, Header h) {
		OEMMetadata firstMeta = null;
		List<Segment> sections = new ArrayList<>();
		OEMMetadata meta;
		EphemerisBlock eph;
		CovarianceBlock covar;

		while(s.hasNext("META_START")) {
			meta = new OEMMetadata(s);
			if (firstMeta == null) {
				firstMeta = meta;
			}
			eph = EphemerisBlock.fromScanner(s, meta.timeSystem);
			if (s.hasNext("COVARIANCE_START")) {
				covar = CovarianceBlock.fromString(collectCovar(s),meta.timeSystem, meta.refFrame);
			}
			else {
				covar = null;
			}
			sections.add(new Segment(meta, eph, covar));
		}
		return new OrbitalEphemerisMessage(h,firstMeta, sections);
	}

	/**
	 * Collects lines until "COVARIANCE_STOP" is reached, and includes that line
	 * @param s
	 * @return
	 */
	private static String collectCovar(Scanner s) {
		String str = "";
		while (!s.hasNext("COVARIANCE_START")) {
			str += s.next() + "\n";
		}
		return str + s.next();
	}

	public Segment[] getSegments() {
		return Arrays.copyOf(segments, segments.length);
	}

	public MessageType messageType() {
		return MessageType.OEM;
	}

	public String toString() {
		return "OrbitalEphemerisMessage(segments = "+Arrays.toString(segments)+")";
	}

	public StateVector getStateVector(Instant t) {
		for (Segment segment : segments) {
			Instant start = segment.metadata.useableStartTime == null ? segment.metadata.startTime : segment.metadata.useableStartTime;
			Instant stop = segment.metadata.useableStopTime == null ? segment.metadata.stopTime : segment.metadata.useableStopTime;
			if (t.isAfter(start) && t.isBefore(stop)) {
				return segment.interpolate(t);
			}
		}
		throw new IllegalArgumentException("Time provided was not found in data");
	}
}
