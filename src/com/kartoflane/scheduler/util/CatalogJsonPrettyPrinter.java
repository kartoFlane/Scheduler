package com.kartoflane.scheduler.util;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

@SuppressWarnings("serial")
public class CatalogJsonPrettyPrinter extends DefaultPrettyPrinter {

	public CatalogJsonPrettyPrinter() {
		Indenter i = new LfTabIndenter();
		indentArraysWith(i);
		indentObjectsWith(i);
	}

	/**
	 * Default linefeed-based indenter uses system-specific linefeeds and
	 * tabs for indentation per level.
	 */
	public static class LfTabIndenter extends NopIndenter {
		private final static String SYS_LF;
		static {
			String lf = null;
			try {
				lf = System.getProperty("line.separator");
			} catch (Throwable t) {
			} // access exception?
			SYS_LF = (lf == null) ? "\n" : lf;
		}

		final static int TAB_COUNT = 32;
		final static char[] TABS = new char[TAB_COUNT];
		static {
			Arrays.fill(TABS, '\t');
		}

		public static final Lf2SpacesIndenter instance = new Lf2SpacesIndenter();

		/**
		 * Linefeed used; default value is the platform-specific linefeed.
		 */
		protected final String _lf;

		public LfTabIndenter() {
			this(SYS_LF);
		}

		/**
		 * @since 2.3
		 */
		public LfTabIndenter(String lf) {
			_lf = lf;
		}

		/**
		 * "Mutant factory" method that will return an instance that uses
		 * specified String as linefeed.
		 * 
		 * @since 2.3
		 */
		public LfTabIndenter withLinefeed(String lf) {
			if (lf.equals(_lf)) {
				return this;
			}
			return new LfTabIndenter(lf);
		}

		@Override
		public boolean isInline() {
			return false;
		}

		@Override
		public void writeIndentation(JsonGenerator jg, int level)
				throws IOException, JsonGenerationException {
			jg.writeRaw(_lf);
			if (level > 0) { // should we err on negative values (as there's some flaw?)
				while (level > TAB_COUNT) { // should never happen but...
					jg.writeRaw(TABS, 0, TAB_COUNT);
					level -= TABS.length;
				}
				jg.writeRaw(TABS, 0, level);
			}
		}
	}
}
