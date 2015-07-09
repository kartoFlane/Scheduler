package com.kartoflane.scheduler.locale;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LocaleManager {

	private static final String localeCommentRegex = "\\s*?//.*?";
	private static final String localeEntryRegex = "(\\w*?)\\s*?:\\s*?\"(.*?)\"";

	private static final Pattern localeEntryPtrn = Pattern.compile(localeEntryRegex + "(" + localeCommentRegex + ")?$");
	private static final Pattern localeFileNamePtrn = Pattern.compile("([a-z]{2})_([A-Z]{2})\\.txt");

	private final Map<Locale, Map<String, String>> localeMap;
	private Locale defaultLocale = Locale.getDefault();

	public LocaleManager() {
		localeMap = new HashMap<Locale, Map<String, String>>();
	}

	public LocaleManager(File localesFolder) throws FileNotFoundException {
		this();

		for (File localeFile : localesFolder.listFiles()) {
			String name = localeFile.getName();

			Matcher matcher = localeFileNamePtrn.matcher(name);
			if (!matcher.find()) {
				continue;
			}

			String language = matcher.group(1);
			String country = matcher.group(2);
			Locale locale = new Locale(language, country);

			loadLocaleFile(this, locale, localeFile);
		}
	}

	public void setDefaultLocale(Locale locale) {
		if (locale == null) {
			throw new IllegalArgumentException("Default locale must not be null.");
		}
		defaultLocale = locale;
	}

	public Locale getDefaultLocale() {
		return defaultLocale;
	}

	/*
	 * ===============================================================================
	 */

	/**
	 * Associates the specified localized string with the specified key in the specified locale.
	 * 
	 * @param locale
	 *            the locale in which to perform the association
	 * @param key
	 *            the key with which the string will be associated
	 * @param s
	 *            the localized string
	 */
	public void addString(Locale locale, String key, String s) {
		if (locale == null) {
			throw new IllegalArgumentException("Locale must not be null.");
		}

		if (key == null || s == null) {
			throw new IllegalArgumentException("Argument must not be null.");
		}

		Map<String, String> stringMap = localeMap.get(locale);
		if (stringMap == null) {
			stringMap = new HashMap<String, String>();
			localeMap.put(locale, stringMap);
		}

		stringMap.put(key, s);
	}

	/**
	 * Returns the localized string for the specified key from the specified locale.
	 */
	public String getString(Locale locale, String key) {
		if (locale == null) {
			return null;
		}
		if (!localeMap.get(locale).containsKey(key)) {
			throw new IllegalArgumentException(String.format("No entry for key '%s' in locale '%s'.", key, locale));
		}
		return localeMap.get(locale).get(key);
	}

	/**
	 * Associates the specified localized string with the specified key in current default locale.
	 * 
	 * @param key
	 *            the key with which the string will be associated
	 * @param s
	 *            the localized string
	 */
	public void addString(String key, String s) {
		addString(defaultLocale, key, s);
	}

	/**
	 * Returns the localized string for the specified key from current default locale.
	 */
	public String getString(String key) {
		return getString(defaultLocale, key);
	}

	/**
	 * Lists all currently loaded locales.
	 */
	public Locale[] listLocales() {
		return localeMap.keySet().toArray(new Locale[0]);
	}

	/*
	 * =====================================================================================
	 * Static loading methods
	 */

	private static void loadLocaleFile(LocaleManager lm, Locale l, File f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);

		try {
			int lineCount = 0;
			while (sc.hasNextLine()) {
				++lineCount;
				String line = sc.nextLine();

				if (line.matches("\\s*?") || line.matches(localeCommentRegex)) {
					// Ignore if it's an empty or comment line
					continue;
				}

				Matcher matcher = localeEntryPtrn.matcher(line);
				if (!matcher.find()) {
					throw new IllegalArgumentException(String.format("Wrong locale file format at line %s in file %s%n", lineCount, f.getName()));
				}

				lm.addString(l, matcher.group(1), matcher.group(2));
			}
		}
		finally {
			if (sc != null) {
				sc.close();
			}
		}
	}
}
