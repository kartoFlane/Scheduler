package com.kartoflane.scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.locale.LocaleStringKeys;
import com.kartoflane.scheduler.ui.SchedulerFrame;
import com.kartoflane.scheduler.util.UIUtils;


public class Scheduler {

	private static final Logger log = LogManager.getLogger(Scheduler.class);

	public static final String APP_NAME = "Scheduler";

	private Scheduler() {
		// Static class -- disallow instantiation.
	}

	/**
	 * TODO:
	 * - catalog generation -- catalog merging, scraper frame, checkbox to add to current if possible (or new if none)
	 * 
	 * - when selecting a row in catalog table, scroll the schedule table to make
	 * the corresponding slot visible
	 * - in schedule table: control + up/down arrow to move 10 rows at a time, when
	 * not in column 0?
	 * 
	 */

	public static void main(String[] args) {
		// Ensure all popups are triggered from the event dispatch thread.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				guiInit();
			}
		});
	}

	public static void guiInit() {
		// Loggers are configured via log4j2.xml file

		try {
			// Swing is ugly. Use native L&F if possible.
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			// Due to some internal working, default sounds are not played for non-Swing LAFs.
			// Details: stackoverflow.com/questions/12128231/12156617#12156617
			Object[] cueList = (Object[]) UIManager.get("AuditoryCues.cueList");
			UIManager.put("AuditoryCues.playList", cueList);
		}
		catch (Exception e) {
			log.error("Error occurred while setting system look and feel:", e);
		}

		// Check if the program can access its own folder
		if (new File(".").exists() == false) {
			// If we can't access the current working directory, then we won't be able to access
			// the locales folder either.
			String msg = "Program was unable to access the current working directory.\n\n" +
					"Make sure that you're not trying to run the program from inside of a zip archive.\n" +
					"Also, instead of double-clicking on scheduler.jar, try to launch scheduler.bat, or\n" +
					"run the following command in the command line, in the program's directory:\n" +
					"java -jar scheduler.jar";
			log.error("Failed to access current directory.");
			UIUtils.showErrorDialog(msg);

			System.exit(0);
		}

		LocaleManager localeManager = null;
		try {
			localeManager = new LocaleManager(new File("locales"));

			// TODO: Debug code, remove
			for (Locale locale : localeManager.listLocales()) {
				LocaleStringKeys.debugAssertCompleteness(localeManager, locale);
			}
		}
		catch (FileNotFoundException ex) {
			log.error("Error occurred while creating locale manager: ", ex);
		}

		try {
			SchedulerFrame frame = new SchedulerFrame(localeManager);
			frame.setVisible(true);

			File defaultCatalog = new File("catalog.json");
			if (defaultCatalog.exists()) {
				frame.loadCatalog(defaultCatalog);
			}

			frame.prepareWorkspace();
		}
		catch (Exception e) {
			log.error("Error occured while creating main frame: ", e);
		}
	}
}
