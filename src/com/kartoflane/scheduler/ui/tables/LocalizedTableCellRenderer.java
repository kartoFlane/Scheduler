package com.kartoflane.scheduler.ui.tables;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.kartoflane.scheduler.core.CourseTypes;
import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.core.Weeks;
import com.kartoflane.scheduler.locale.LocaleManager;


@SuppressWarnings("serial")
public class LocalizedTableCellRenderer extends DefaultTableCellRenderer {

	private final LocaleManager localem;

	public LocalizedTableCellRenderer(LocaleManager localeManager) {
		localem = localeManager;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

		if (value instanceof Days) {
			Days day = (Days) value;
			setValue(day.toString(localem));
		}
		else if (value instanceof CourseTypes) {
			CourseTypes type = (CourseTypes) value;
			setValue(type.toShort(localem));
		}
		else if (value instanceof Weeks) {
			Weeks week = (Weeks) value;
			setValue(week.toShort(localem));
		}

		return c;
	}
}
