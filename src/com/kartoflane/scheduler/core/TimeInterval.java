package com.kartoflane.scheduler.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A class for simple time intervals in 24-hour system.
 * 
 * Does not support wrapping intervals, where the end time
 * is less than start time.
 * 
 * @author kartoFlane
 *
 */
public class TimeInterval implements Comparable<TimeInterval> {

	private static final String hourRegex = "(\\d{1,2}):(\\d{2})";
	private static final Pattern timePtrn = Pattern.compile(hourRegex + "\\s*?-\\s*?" + hourRegex);

	private int startH;
	private int startM;
	private int endH;
	private int endM;

	public TimeInterval(int sH, int sM, int eH, int eM) {
		set(sH, sM, eH, eM);
	}

	public TimeInterval(String range) {
		set(range);
	}

	public TimeInterval(TimeInterval ti) {
		set(ti);
	}

	public int[] length() {
		int dH = endH - startH;
		int dM = endM - startM;
		if (dM < 0) {
			dM = 60 + dM;
			--dH;
		}
		return new int[] { dH, dM };
	}

	public TimeInterval set(int sH, int sM, int eH, int eM) {
		if (sH >= 0 && sM >= 0 && eH >= 0 && eM >= 0) {
			startH = sH;
			startM = sM;
			endH = eH;
			endM = eM;
			wrap();
			return this;
		}
		else {
			throw new IllegalArgumentException("Arguments must be non-negative integers.");
		}
	}

	public TimeInterval set(String range) {
		Matcher m = timePtrn.matcher(range);

		if (m.find()) {
			startH = Integer.valueOf(m.group(1));
			startM = Integer.valueOf(m.group(2));
			endH = Integer.valueOf(m.group(3));
			endM = Integer.valueOf(m.group(4));
			wrap();
			return this;
		}
		else {
			throw new IllegalArgumentException("Time interval is wrongly formatted:" + range);
		}
	}

	public TimeInterval set(TimeInterval ti) {
		if (ti != null) {
			startH = ti.startH;
			startM = ti.startM;
			endH = ti.endH;
			endM = ti.endM;
			wrap();
			return this;
		}
		else {
			throw new IllegalArgumentException("Argument must not be null.");
		}
	}

	public TimeInterval setLength(int dH, int dM) {
		if (dH >= 0 && dM >= 0) {
			endH = startH + dH;
			endM = startM + dM;
			wrap();
			return this;
		}
		else {
			throw new IllegalArgumentException("Arguments must be non-negative integers.");
		}
	}

	public TimeInterval setLength(TimeInterval ti) {
		int[] l = ti.length();
		return setLength(l[0], l[1]);
	}

	public TimeInterval advance(int sH, int sM, int eH, int eM) {
		return set(startH + sH, startM + sM, endH + eH, endM + eM);
	}

	public TimeInterval advance(int dH, int dM) {
		return set(startH + dH, startM + dM, endH + dH, endM + dM);
	}

	public TimeInterval advance(TimeInterval ti) {
		int[] length = ti.length();
		return set(startH + length[0], startM + length[1], endH + length[0], endM + length[1]);
	}

	private void wrap() {
		startH += startM / 60;
		startH %= 24;
		startM %= 60;
		endH += endM / 60;
		endH %= 24;
		endM %= 60;
	}

	public int getStartHour() {
		return startH;
	}

	public int getEndHour() {
		return endH;
	}

	public int getStartMinute() {
		return startM;
	}

	public int getEndMinute() {
		return endM;
	}

	public boolean intersects(TimeInterval o) {
		float s = startH + startM * 0.01f;
		float e = endH + endM * 0.01f;
		float os = o.startH + o.startM * 0.01f;
		float oe = o.endH + o.endM * 0.01f;
		return !borders(o) && ((e >= os && e <= oe) || (s >= os && s <= oe));
	}

	public boolean borders(TimeInterval o) {
		float s = startH + startM * 0.01f;
		float e = endH + endM * 0.01f;
		float os = o.startH + o.startM * 0.01f;
		float oe = o.endH + o.endM * 0.01f;
		return (s <= os && e == os) || (os <= s && oe == s);
	}

	@Override
	public String toString() {
		return String.format("%02d:%02d - %02d:%02d", startH, startM, endH, endM);
	}

	@Override
	public int compareTo(TimeInterval o) {
		int result = startH - o.startH;
		if (result == 0) {
			result = startM - o.startM;
			if (result == 0) {
				result = endH - o.endH;
				if (result == 0)
					result = endM - o.endM;
			}
		}

		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endH;
		result = prime * result + endM;
		result = prime * result + startH;
		result = prime * result + startM;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TimeInterval))
			return false;
		TimeInterval other = (TimeInterval) obj;
		if (endH != other.endH)
			return false;
		if (endM != other.endM)
			return false;
		if (startH != other.startH)
			return false;
		if (startM != other.startM)
			return false;
		return true;
	}
}
