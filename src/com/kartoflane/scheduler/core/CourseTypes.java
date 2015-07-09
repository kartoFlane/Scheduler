package com.kartoflane.scheduler.core;

import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.locale.LocaleStringKeys;


public enum CourseTypes {
	LABORATORY,
	LECTURE,
	PRACTICE,
	PROJECT,
	SEMINAR,
	OTHER;

	public static CourseTypes eval(String s) throws IllegalArgumentException {
		CourseTypes result = null;

		// switch on Strings only available since Java 7
		if (LECTURE.matches(s)) {
			result = LECTURE;
		}
		else if (LABORATORY.matches(s)) {
			result = LABORATORY;
		}
		else if (PRACTICE.matches(s)) {
			result = PRACTICE;
		}
		else if (SEMINAR.matches(s)) {
			result = SEMINAR;
		}
		else if (PROJECT.matches(s)) {
			result = PROJECT;
		}
		else if (OTHER.matches(s)) {
			result = OTHER;
		}
		else {
			throw new IllegalArgumentException(s);
		}

		return result;
	}

	public String toString(LocaleManager localem) {
		switch (this) {
			case LECTURE:
				return localem.getString(LocaleStringKeys.TYPE_LECTURE);
			case LABORATORY:
				return localem.getString(LocaleStringKeys.TYPE_LABORATORY);
			case PRACTICE:
				return localem.getString(LocaleStringKeys.TYPE_PRACTICE);
			case SEMINAR:
				return localem.getString(LocaleStringKeys.TYPE_SEMINAR);
			case PROJECT:
				return localem.getString(LocaleStringKeys.TYPE_PROJECT);
			case OTHER:
				return localem.getString(LocaleStringKeys.TYPE_OTHER);
			default:
				return null;
		}
	}

	public String toShort(LocaleManager localem) {
		switch (this) {
			case LECTURE:
				return localem.getString(LocaleStringKeys.TYPE_LECTURE_SHORT);
			case LABORATORY:
				return localem.getString(LocaleStringKeys.TYPE_LABORATORY_SHORT);
			case PRACTICE:
				return localem.getString(LocaleStringKeys.TYPE_PRACTICE_SHORT);
			case SEMINAR:
				return localem.getString(LocaleStringKeys.TYPE_SEMINAR_SHORT);
			case PROJECT:
				return localem.getString(LocaleStringKeys.TYPE_PROJECT_SHORT);
			case OTHER:
				return localem.getString(LocaleStringKeys.TYPE_OTHER_SHORT);
			default:
				return null;
		}
	}

	private boolean matches(String s) {
		boolean result = name().equalsIgnoreCase(s);

		if (!result) {
			switch (this) {
				case LECTURE:
					return s.matches("(?i)w|wyklad|wyk³ad");
				case LABORATORY:
					return s.matches("(?i)l|lab|laboratorium");
				case PRACTICE:
					return s.matches("(?i)c|æ|cwiczenia|æwiczenia");
				case SEMINAR:
					return s.matches("(?i)s|seminarium|konwersatorium");
				case PROJECT:
					return s.matches("(?i)p|projekt");
				case OTHER:
					return true; // TODO
				default:
					break;
			}
		}

		return result;
	}
}
