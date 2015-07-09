package com.kartoflane.scheduler.locale;

import java.lang.reflect.Field;
import java.util.Locale;


public class LocaleStringKeys {

	public static final String SCHEDULE = "SCHEDULE";
	public static final String NEW_SCHEDULE = "NEW_SCHEDULE";
	public static final String LOAD_SCHEDULE = "LOAD_SCHEDULE";
	public static final String SAVE_SCHEDULE = "SAVE_SCHEDULE";

	public static final String CATALOG = "CATALOG";
	public static final String NEW_CATALOG = "NEW_CATALOG";
	public static final String LOAD_CATALOG = "LOAD_CATALOG";
	public static final String SAVE_CATALOG = "SAVE_CATALOG";
	public static final String GEN_CATALOG = "GEN_CATALOG";

	public static final String MSG_OVERLAP = "MSG_OVERLAP";
	public static final String MSG_ENROLLED_CLASS = "MSG_ENROLLED_CLASS";
	public static final String MSG_ENROLLED_GROUP = "MSG_ENROLLED_GROUP";
	public static final String MSG_NEED_CATALOG = "MSG_NEED_CATALOG";

	public static final String MSG_SCHEDULE_LOAD_FAIL_PARSE = "MSG_SCHEDULE_LOAD_FAIL_PARSE";
	public static final String MSG_SCHEDULE_LOAD_FAIL_READ = "MSG_SCHEDULE_LOAD_FAIL_READ";
	public static final String MSG_SCHEDULE_LOAD_FAIL_OVERLAP = "MSG_SCHEDULE_LOAD_FAIL_OVERLAP";
	public static final String MSG_SCHEDULE_LOAD_FAIL_ENROLLED_CLASS = "MSG_SCHEDULE_LOAD_FAIL_ENROLLED_CLASS";
	public static final String MSG_SCHEDULE_LOAD_FAIL_ENROLLED_GROUP = "MSG_SCHEDULE_LOAD_FAIL_ENROLLED_GROUP";
	public static final String MSG_SCHEDULE_LOAD_FAIL = "MSG_SCHEDULE_LOAD_FAIL";

	public static final String MSG_SCHEDULE_SAVE_FAIL_JSON = "MSG_SCHEDULE_SAVE_FAIL_JSON";
	public static final String MSG_SCHEDULE_SAVE_FAIL = "MSG_SCHEDULE_SAVE_FAIL";

	public static final String MSG_CATALOG_LOAD_FAIL_PARSE = "MSG_CATALOG_LOAD_FAIL_PARSE";
	public static final String MSG_CATALOG_LOAD_FAIL_READ = "MSG_CATALOG_LOAD_FAIL_READ";

	public static final String MSG_CATALOG_SAVE_FAIL_JSON = "MSG_CATALOG_SAVE_FAIL_JSON";
	public static final String MSG_CATALOG_SAVE_FAIL = "MSG_CATALOG_SAVE_FAIL";

	public static final String COL_CLASS_DAY = "COL_CLASS_DAY";
	public static final String COL_CLASS_TIME = "COL_CLASS_TIME";
	public static final String COL_CLASS_WEEK = "COL_CLASS_WEEK";
	public static final String COL_CLASS_TYPE = "COL_CLASS_TYPE";
	public static final String COL_CLASS_INSTRUCTOR = "COL_CLASS_INSTRUCTOR";
	public static final String COL_CLASS_LOCATION = "COL_CLASS_LOCATION";
	public static final String COL_GROUP_CODE = "COL_GROUP_CODE";
	public static final String COL_COURSE_NAME = "COL_COURSE_NAME";

	public static final String COL_SCHEDULE_TIME = "COL_SCHEDULE_TIME";

	public static final String DAY_MONDAY = "DAY_MONDAY";
	public static final String DAY_TUESDAY = "DAY_TUESDAY";
	public static final String DAY_WEDNESDAY = "DAY_WEDNESDAY";
	public static final String DAY_THURSDAY = "DAY_THURSDAY";
	public static final String DAY_FRIDAY = "DAY_FRIDAY";

	public static final String WEEK_EACH = "WEEK_EACH";
	public static final String WEEK_EVEN = "WEEK_EVEN";
	public static final String WEEK_ODD = "WEEK_ODD";

	public static final String WEEK_EACH_SHORT = "WEEK_EACH";
	public static final String WEEK_EVEN_SHORT = "WEEK_EVEN";
	public static final String WEEK_ODD_SHORT = "WEEK_ODD";

	public static final String TYPE_LECTURE = "TYPE_LECTURE";
	public static final String TYPE_LABORATORY = "TYPE_LABORATORY";
	public static final String TYPE_PRACTICE = "TYPE_PRACTICE";
	public static final String TYPE_SEMINAR = "TYPE_SEMINAR";
	public static final String TYPE_PROJECT = "TYPE_PROJECT";
	public static final String TYPE_OTHER = "TYPE_OTHER";

	public static final String TYPE_LECTURE_SHORT = "TYPE_LECTURE_SHORT";
	public static final String TYPE_LABORATORY_SHORT = "TYPE_LABORATORY_SHORT";
	public static final String TYPE_PRACTICE_SHORT = "TYPE_PRACTICE_SHORT";
	public static final String TYPE_SEMINAR_SHORT = "TYPE_SEMINAR_SHORT";
	public static final String TYPE_PROJECT_SHORT = "TYPE_PROJECT_SHORT";
	public static final String TYPE_OTHER_SHORT = "TYPE_OTHER_SHORT";

	public static final String SCRAPER_SOURCE = "SCRAPER_SOURCE";
	public static final String SCRAPER_INFO = "SCRAPER_INFO";

	private LocaleStringKeys() {
		// Static class -- disallow instantiation.
	}

	public static void debugAssertCompleteness(LocaleManager localem, Locale locale) {
		Field[] fields = LocaleStringKeys.class.getFields();

		for (Field f : fields) {
			try {
				localem.getString(locale, (String) f.get(null));
			}
			catch (IllegalArgumentException e) {
				System.err.println(e.getMessage());
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
