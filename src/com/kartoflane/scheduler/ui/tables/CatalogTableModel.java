package com.kartoflane.scheduler.ui.tables;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.kartoflane.scheduler.catalog.Catalog;
import com.kartoflane.scheduler.catalog.ClassData;
import com.kartoflane.scheduler.catalog.ClassGroup;
import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.core.TimeInterval;
import com.kartoflane.scheduler.core.Weeks;
import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.locale.LocaleStringKeys;


@SuppressWarnings("serial")
public class CatalogTableModel extends DefaultTableModel {

	// @formatter:off
	// Column indices to be used with methods that take column index as argument.
	public static final int COLUMN_DAY =		0;
	public static final int COLUMN_TIME =		1;
	public static final int COLUMN_WEEK =		2;
	public static final int COLUMN_GROUP =		3;
	public static final int COLUMN_TYPE =		4;
	public static final int COLUMN_COURSE =		5;
	public static final int COLUMN_INSTR =		6;
	public static final int COLUMN_LOC =		7;
	// @formatter:on

	private static String[] columnNames = new String[8];
	private static Class<?>[] columnTypes = new Class<?>[] { Days.class, TimeInterval.class, Weeks.class,
			String.class, String.class, String.class, String.class, String.class };

	private final LocaleManager localem;

	private Catalog currentCatalog;
	// Rows keep fragmented data in the form of an Object array;
	// we need easy access to ClassData elements, so store them in a list
	private List<ClassGroup> rows = new ArrayList<ClassGroup>();

	public CatalogTableModel(LocaleManager localem) {
		super(columnNames, 0);

		this.localem = localem;

		columnNames[0] = localem.getString(LocaleStringKeys.COL_CLASS_DAY);
		columnNames[1] = localem.getString(LocaleStringKeys.COL_CLASS_TIME);
		columnNames[2] = localem.getString(LocaleStringKeys.COL_CLASS_WEEK);
		columnNames[3] = localem.getString(LocaleStringKeys.COL_GROUP_CODE);
		columnNames[4] = localem.getString(LocaleStringKeys.COL_CLASS_TYPE);
		columnNames[5] = localem.getString(LocaleStringKeys.COL_COURSE_NAME);
		columnNames[6] = localem.getString(LocaleStringKeys.COL_CLASS_INSTRUCTOR);
		columnNames[7] = localem.getString(LocaleStringKeys.COL_CLASS_LOCATION);

		setColumnIdentifiers(columnNames);
	}

	protected void setCurrentCatalog(Catalog cat) {
		currentCatalog = cat;
	}

	protected Catalog getCurrentCatalog() {
		return currentCatalog;
	}

	protected void addRow(ClassGroup cg) {
		rows.add(cg);
		addRow(cg.toRowData(localem));
	}

	protected void addRow(ClassData cd) {
		addRow(cd.toRowData(localem));
	}

	protected ClassGroup getRow(int row) {
		if (rows.size() <= row) {
			return null;
		}
		else {
			return rows.get(row);
		}
	}

	/**
	 * Removes all rows from the table.
	 */
	protected void clear() {
		rows.clear();
		for (int i = getRowCount() - 1; i >= 0; --i) {
			removeRow(i);
		}
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return columnTypes[column];
	}
}
