package com.kartoflane.scheduler.ui.tables;

import com.kartoflane.scheduler.catalog.ClassData;
import com.kartoflane.scheduler.catalog.Schedule;
import com.kartoflane.scheduler.core.TimeInterval;
import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.locale.LocaleStringKeys;
import com.kartoflane.scheduler.ui.multispan.AttributiveCellTableModel;


@SuppressWarnings("serial")
public class ScheduleTableModel extends AttributiveCellTableModel {

	private static String[] columnNames = new String[11];
	private static Class<?>[] columnTypes = new Class[] { String.class, String.class, String.class,
			String.class, String.class, String.class, String.class, String.class, String.class,
			String.class, String.class };

	private ClassData highlightedCell;
	private Schedule currentSchedule;

	public ScheduleTableModel(LocaleManager localem) {
		super(columnNames, 0);

		columnNames[0] = localem.getString(LocaleStringKeys.COL_SCHEDULE_TIME);
		columnNames[1] = localem.getString(LocaleStringKeys.WEEK_ODD);
		columnNames[2] = localem.getString(LocaleStringKeys.WEEK_EVEN);
		columnNames[3] = localem.getString(LocaleStringKeys.WEEK_ODD);
		columnNames[4] = localem.getString(LocaleStringKeys.WEEK_EVEN);
		columnNames[5] = localem.getString(LocaleStringKeys.WEEK_ODD);
		columnNames[6] = localem.getString(LocaleStringKeys.WEEK_EVEN);
		columnNames[7] = localem.getString(LocaleStringKeys.WEEK_ODD);
		columnNames[8] = localem.getString(LocaleStringKeys.WEEK_EVEN);
		columnNames[9] = localem.getString(LocaleStringKeys.WEEK_ODD);
		columnNames[10] = localem.getString(LocaleStringKeys.WEEK_EVEN);

		setColumnIdentifiers(columnNames);
	}

	public void setCurrentSchedule(Schedule schedule) {
		currentSchedule = schedule;
	}

	public Schedule getCurrentSchedule() {
		return currentSchedule;
	}

	public void setHighlightedCell(ClassData cd) {
		highlightedCell = cd;
	}

	public ClassData getHighlightedCell() {
		return highlightedCell;
	}

	public int getTimeRow(TimeInterval ti) {
		for (int row = 0; row < getRowCount(); row++) {
			if (getValueAt(row, 0).equals(ti)) {
				return row;
			}
		}
		return -1;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public boolean isCellModifiable(int row, int column) {
		return column > 0;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return columnTypes[column];
	}
}
