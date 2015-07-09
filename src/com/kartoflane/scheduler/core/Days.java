package com.kartoflane.scheduler.core;

import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.locale.LocaleStringKeys;


public enum Days {
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY;

	public static Days eval(String s) throws IllegalArgumentException {
		Days result = null;

		// switch on Strings only available since Java 7
		if (MONDAY.matches(s)) {
			result = MONDAY;
		}
		else if (TUESDAY.matches(s)) {
			result = TUESDAY;
		}
		else if (WEDNESDAY.matches(s)) {
			result = WEDNESDAY;
		}
		else if (THURSDAY.matches(s)) {
			result = THURSDAY;
		}
		else if (FRIDAY.matches(s)) {
			result = FRIDAY;
		}
		else {
			throw new IllegalArgumentException(s);
		}

		return result;
	}

	public String toString(LocaleManager localem) {
		switch (this) {
			case MONDAY:
				return localem.getString(LocaleStringKeys.DAY_MONDAY);
			case TUESDAY:
				return localem.getString(LocaleStringKeys.DAY_TUESDAY);
			case WEDNESDAY:
				return localem.getString(LocaleStringKeys.DAY_WEDNESDAY);
			case THURSDAY:
				return localem.getString(LocaleStringKeys.DAY_THURSDAY);
			case FRIDAY:
				return localem.getString(LocaleStringKeys.DAY_FRIDAY);
			default:
				return null;
		}
	}

	private boolean matches(String s) {
		boolean result = name().equalsIgnoreCase(s);

		if (!result) {
			switch (this) {
				case MONDAY:
					return s.matches("(?i)pn|poniedzialek|poniedzia³ek|pon");
				case TUESDAY:
					return s.matches("(?i)wt|wtorek");
				case WEDNESDAY:
					return s.matches("(?i)sr|œr|sroda|œroda");
				case THURSDAY:
					return s.matches("(?i)cz|czw|czwartek");
				case FRIDAY:
					return s.matches("(?i)pt|piatek");
				default:
					break;
			}
		}

		return result;
	}
}
