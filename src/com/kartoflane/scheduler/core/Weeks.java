package com.kartoflane.scheduler.core;

import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.locale.LocaleStringKeys;


public enum Weeks {
	EACH,
	ODD,
	EVEN;

	public static Weeks eval(String s) throws IllegalArgumentException {
		Weeks result = null;

		// switch on Strings only available since Java 7
		if (EACH.matches(s)) {
			result = EACH;
		}
		else if (EVEN.matches(s)) {
			result = EVEN;
		}
		else if (ODD.matches(s)) {
			result = ODD;
		}
		else {
			throw new IllegalArgumentException(s);
		}

		return result;
	}

	public String toString(LocaleManager localem) {
		switch (this) {
			case EACH:
				return localem.getString(LocaleStringKeys.WEEK_EACH);
			case EVEN:
				return localem.getString(LocaleStringKeys.WEEK_EVEN);
			case ODD:
				return localem.getString(LocaleStringKeys.WEEK_ODD);
			default:
				return null;
		}
	}

	public String toShort(LocaleManager localem) {
		switch (this) {
			case EACH:
				return localem.getString(LocaleStringKeys.WEEK_EACH_SHORT);
			case EVEN:
				return localem.getString(LocaleStringKeys.WEEK_EVEN_SHORT);
			case ODD:
				return localem.getString(LocaleStringKeys.WEEK_ODD_SHORT);
			default:
				return null;
		}
	}

	public boolean collides(Weeks week) {
		return this == EACH || week == EACH || this == week;
	}

	private boolean matches(String s) {
		boolean result = name().equalsIgnoreCase(s);

		if (!result) {
			switch (this) {
				case EACH:
					return s == null || s.equals("");
				case EVEN:
					return s.matches("(?i)tp");
				case ODD:
					return s.matches("(?i)tn");
				default:
					break;
			}
		}

		return result;
	}
}
