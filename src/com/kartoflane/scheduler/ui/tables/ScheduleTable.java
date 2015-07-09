package com.kartoflane.scheduler.ui.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.kartoflane.scheduler.catalog.ClassData;
import com.kartoflane.scheduler.catalog.Schedule;
import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.core.TimeInterval;
import com.kartoflane.scheduler.core.Weeks;
import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.ui.colgroup.ColumnGroup;
import com.kartoflane.scheduler.ui.colgroup.GroupableTableHeader;
import com.kartoflane.scheduler.ui.multispan.CellSpan;
import com.kartoflane.scheduler.ui.multispan.MultiSpanCellTable;


@SuppressWarnings("serial")
public class ScheduleTable extends MultiSpanCellTable {

	/** How many minutes are represented by a single row. Must be > 0 */
	private static final int minutesPerRow = 5;
	/** Height of a single minute row, in pixels. Must be >= 1. */
	private static final int rowHeight = 3;

	private static final ColoredTableCellRenderer allowedCellRenderer = new ColoredTableCellRenderer(128, 255, 128, 255);
	private static final ColoredTableCellRenderer forbiddenCellRenderer = new ColoredTableCellRenderer(255, 128, 128, 255);
	private static final ColoredTableCellRenderer selectedCellRenderer = new ColoredTableCellRenderer(164, 164, 255, 255);
	/**
	 * Due to some weird inner working of Swing, text in cells that have the default background
	 * gets drawn in a slightly fuzzied way.
	 * 
	 * To fix this, change the background color to anything other than the default white.
	 */
	private static final ColoredTableCellRenderer defaultCellRenderer = new ColoredTableCellRenderer(254, 254, 254, 255);

	private final LocaleManager localem;
	private final Map<TableCellRenderer, List<Cell>> rendererCellMap = new HashMap<TableCellRenderer, List<Cell>>();

	private TimeInterval dayInterval = null;

	public ScheduleTable(LocaleManager localem) {
		super(new ScheduleTableModel(localem));

		this.localem = localem;

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumnModel cm = getColumnModel();
		cm.setColumnMargin(0);
		setRowMargin(0);
		setRowHeight(rowHeight);

		allowedCellRenderer.setHorizontalAlignment(JLabel.CENTER);
		forbiddenCellRenderer.setHorizontalAlignment(JLabel.CENTER);
		selectedCellRenderer.setHorizontalAlignment(JLabel.CENTER);
		defaultCellRenderer.setHorizontalAlignment(JLabel.CENTER);

		rendererCellMap.put(allowedCellRenderer, new ArrayList<Cell>());
		rendererCellMap.put(forbiddenCellRenderer, new ArrayList<Cell>());
		rendererCellMap.put(selectedCellRenderer, new ArrayList<Cell>());

		setDefaultRenderer(String.class, defaultCellRenderer);

		GroupableTableHeader header = new GroupableTableHeader(cm);
		header.setResizingAllowed(false);
		setTableHeader(header);

		int columnIndex = 1;
		for (Days day : Days.values()) {
			ColumnGroup cg = new ColumnGroup(day.toString(localem));
			cg.add(cm.getColumn(columnIndex));
			cg.add(cm.getColumn(columnIndex + 1));
			header.addColumnGroup(cg);

			columnIndex += 2;
		}

		setTimeView(TimeView.ACADEMIC);
		setEnabled(false);
	}

	@Override
	public void setModel(TableModel model) {
		if (model instanceof ScheduleTableModel) {
			super.setModel(model);
		}
		else {
			throw new IllegalArgumentException("Model must be instance of " +
					ScheduleTableModel.class.getSimpleName());
		}
	}

	public ScheduleTableModel getModel() {
		return (ScheduleTableModel) super.getModel();
	}

	/*
	 * =======================
	 * NOTE: Time view control
	 * =======================
	 */

	/**
	 * Initializes the time column with values ranging from 7:30 to 20:35,
	 * with varying span and spacing, to represent the time slots for classes
	 * in an academic day.
	 */
	private void initCellsAcademic() {
		dayInterval = new TimeInterval("07:30 - 20:35");
		TimeInterval stamp = new TimeInterval(dayInterval);

		int hourRowSpan = rowSpan(new TimeInterval("09:15 - 11:00"));
		int breakRowSpan = rowSpan(new TimeInterval("09:00 - 09:15"));
		int[] columns = { 0 };
		int row = 0;

		getModel().setRowCount(rowSpan(dayInterval));

		// 7:30 - 9:00 (1h 30min)
		stamp.setLength(1, 30);

		combineCells(createRowRange(row, rowSpan(stamp)), columns);
		setValueAt(stamp.toString(), row, 0);
		stamp.advance(stamp);
		row += rowSpan(stamp);

		// 15:00 - 15:15 (15min)
		stamp.setLength(0, 15);

		combineCells(createRowRange(row, breakRowSpan), columns);
		setValueAt(stamp.toString(), row, 0);
		stamp.advance(stamp);
		row += breakRowSpan;

		for (int i = 0; i < 3; ++i) {
			// class (1h 45min)
			stamp.setLength(1, 45);

			combineCells(createRowRange(row, hourRowSpan), columns);
			setValueAt(stamp.toString(), row, 0);
			stamp.advance(stamp);
			row += hourRowSpan;

			// break (15min)
			stamp.setLength(0, 15);

			combineCells(createRowRange(row, breakRowSpan), columns);
			setValueAt(stamp.toString(), row, 0);
			stamp.advance(stamp);
			row += breakRowSpan;
		}

		for (int i = 0; i < 2; ++i) {
			// class (1h 40min)
			stamp.setLength(1, 40);

			combineCells(createRowRange(row, rowSpan(stamp)), columns);
			setValueAt(stamp.toString(), row, 0);
			stamp.advance(stamp);
			row += rowSpan(stamp);

			// break (10min)
			stamp.setLength(0, 10);

			combineCells(createRowRange(row, rowSpan(stamp)), columns);
			setValueAt(stamp.toString(), row, 0);
			stamp.advance(stamp);
			row += rowSpan(stamp);
		}

		// 18:55 - 20:35 (1h 40min)
		stamp.setLength(1, 40);

		combineCells(createRowRange(row, rowSpan(stamp)), columns);
		setValueAt(stamp.toString(), row, 0);
		stamp.advance(stamp);
		row += rowSpan(stamp);
	}

	/**
	 * Initializes the time column with values ranging from 7:00 to 21:00,
	 * with even 1-hour spans and intervals.
	 */
	private void initCellsStandard() {
		dayInterval = new TimeInterval("07:00 - 21:00");
		TimeInterval stamp = new TimeInterval(dayInterval);

		int dayRowSpan = rowSpan(dayInterval);
		int hourRowSpan = rowSpan(new TimeInterval("00:00 - 01:00"));
		int[] columns = { 0 };
		int row = 0;

		getModel().setRowCount(dayRowSpan);

		int hoursInDay = dayRowSpan / hourRowSpan;
		stamp.setLength(1, 0);
		for (int i = 0; i < hoursInDay; ++i) {
			combineCells(createRowRange(row, hourRowSpan), columns);
			setValueAt(stamp.toString(), row, 0);
			stamp.advance(stamp);
			row += hourRowSpan;
		}
	}

	public void setTimeView(TimeView view) {
		int cols = getModel().getColumnCount();
		int rows = getModel().getRowCount();

		CellSpan cellAtt = (CellSpan) getModel().getCellAttribute();
		TableModel model = getModel();
		for (int col = 0; col < cols; ++col) {
			for (int row = 0; row < rows; ++row) {
				model.setValueAt(null, row, col);
				cellAtt.split(row, col);
			}
		}

		switch (view) {
			case ACADEMIC:
				initCellsAcademic();
				break;
			case STANDARD:
				initCellsStandard();
				break;
			default:
				throw new IllegalArgumentException("" + view);
		}
	}

	public static enum TimeView {
		STANDARD, ACADEMIC;
	}

	/*
	 * ==============================
	 * NOTE: Current schedule control
	 * ==============================
	 */

	public void setCurrentSchedule(Schedule schedule) {
		clearSchedule();
		getModel().setCurrentSchedule(schedule);
		setEnabled(schedule != null);
	}

	public Schedule getCurrentSchedule() {
		return getModel().getCurrentSchedule();
	}

	/**
	 * (Re)loads the current schedule set for this table.
	 * 
	 * To change the current schedule, use {@link #setCurrentSchedule(Schedule)}
	 */
	public void loadSchedule() {
		clearSchedule();

		Schedule schedule = getModel().getCurrentSchedule();

		if (schedule == null) {
			return;
		}

		TableModel model = getModel();
		for (Days day : Days.values()) {
			for (Weeks week : Weeks.values()) {
				Set<ClassData> classes = schedule.listClasses(day, week);
				int col = toColumn(day, week);

				for (ClassData cd : classes) {
					TimeInterval time = cd.getTime();

					int row = toRow(time);
					int[] rows = createRowRange(row, rowSpan(time));
					int[] cols;

					if (week == Weeks.EACH) {
						cols = new int[] { col, col + 1 };
					}
					else {
						cols = new int[] { col };
					}

					combineCells(rows, cols);
					model.setValueAt(cd.toCellData(localem), row, col);
				}
			}
		}
	}

	protected void clearSchedule() {
		int cols = getModel().getColumnCount();
		int rows = getModel().getRowCount();

		CellSpan cellAtt = (CellSpan) getModel().getCellAttribute();
		TableModel model = getModel();
		for (int col = 1; col < cols; ++col) {
			for (int row = 0; row < rows; ++row) {
				model.setValueAt(null, row, col);
				cellAtt.split(row, col);
			}
		}
	}

	/*
	 * ==========================================
	 * NOTE: Day to col & time to row conversions
	 * ==========================================
	 */

	private static int toColumn(Days day, Weeks week) {
		int result = 1;
		result += day.ordinal() * 2;
		if (week == Weeks.EVEN) {
			++result;
		}
		return result;
	}

	private int toRow(TimeInterval ti) {
		TimeInterval temp = new TimeInterval(dayInterval.getStartHour(), dayInterval.getStartMinute(),
				ti.getStartHour(), ti.getStartMinute());
		return rowSpan(temp);
	}

	private static int rowSpan(TimeInterval ti) {
		int[] length = ti.length();
		int totalMinutes = length[0] * 60 + length[1];
		return totalMinutes / minutesPerRow;
	}

	private static int[] createRowRange(int start, int length) {
		int[] result = new int[length];
		for (int i = 0; i < length; ++i)
			result[i] = start + i;
		return result;
	}

	/*
	 * ================================
	 * NOTE: Current selection querying
	 * ================================
	 */

	public Days getSelectedDay() {
		int col = getSelectedColumn();
		if (col > 0) {
			--col;
			return Days.values()[col / 2];
		}

		return null;
	}

	public Weeks getSelectedWeek() {
		int col = getSelectedColumn();
		if (col > 0) {
			--col;
			return Weeks.values()[1 + col % 2];
		}

		return null;
	}

	public TimeInterval getSelectedTime() {
		int col = getSelectedColumn();
		int row = getSelectedRow();

		if (row < 0) {
			return null;
		}

		CellSpan cellAtt = (CellSpan) getModel().getCellAttribute();
		int[] span = cellAtt.getSpan(row, col);

		if (col == 0) {
			if (span[CellSpan.ROW] < 0) {
				row += span[CellSpan.ROW];
			}
			return new TimeInterval(getModel().getValueAt(row, 0).toString());
		}
		else {
			TimeInterval result = new TimeInterval(0, minutesPerRow * row,
					0, minutesPerRow * (row + span[CellSpan.ROW]));
			result.advance(dayInterval.getStartHour(), dayInterval.getStartMinute());
			return result;
		}
	}

	public ClassData getSelectedClassData() {
		Days day = getSelectedDay();
		Weeks week = getSelectedWeek();
		TimeInterval time = getSelectedTime();

		if (day == null || week == null || time == null) {
			return null;
		}
		else {
			return getCurrentSchedule().getClass(day, week, time);
		}
	}

	/*
	 * ===========================================
	 * NOTE: Cell combination/splitting delegation
	 * ===========================================
	 */

	public void combineCells(int[] rows, int[] cols) {
		CellSpan cellAtt = (CellSpan) getModel().getCellAttribute();
		cellAtt.combine(rows, cols);
	}

	public void splitCell(int row, int col) {
		CellSpan cellAtt = (CellSpan) getModel().getCellAttribute();
		cellAtt.split(row, col);
	}

	public int[] getAggregatingCell(int row, int col) {
		CellSpan cellAtt = (CellSpan) getModel().getCellAttribute();
		int[] span = cellAtt.getSpan(row, col);

		if (span[CellSpan.ROW] < 0) {
			row += span[CellSpan.ROW];
		}
		if (span[CellSpan.COLUMN] < 0) {
			col += span[CellSpan.COLUMN];
		}

		return new int[] { row, col };
	}

	/*
	 * =====================================
	 * NOTE: Cell highlighting functionality
	 * =====================================
	 */

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (isEnabled()) {
			for (TableCellRenderer key : rendererCellMap.keySet()) {
				for (Cell c : rendererCellMap.get(key)) {
					if (c.row == row && c.col == column) {
						return key;
					}
				}
			}
		}
		return super.getCellRenderer(row, column);
	}

	private TableCellRenderer getRenderer(Highlights h) {
		switch (h) {
			case ALLOWED:
				return allowedCellRenderer;
			case FORBIDDEN:
				return forbiddenCellRenderer;
			case SELECTED:
				return selectedCellRenderer;
			default:
				throw new IllegalArgumentException("" + h);
		}
	}

	public void clearHighlight() {
		for (List<?> l : rendererCellMap.values()) {
			l.clear();
		}
	}

	public void addHighlightedCells(Highlights h, int[] rows, int[] cols) {
		if (h == null) {
			throw new IllegalArgumentException("Highlight must not be null!");
		}

		List<Cell> cellList = rendererCellMap.get(getRenderer(h));

		for (int i = 0; i < rows.length; ++i) {
			for (int j = 0; j < cols.length; ++j) {
				cellList.add(new Cell(rows[i], cols[j]));
			}
		}
	}

	public void addHighlightedClass(Highlights h, ClassData cd) {
		TimeInterval time = cd.getTime();

		int row = toRow(time);
		int col = toColumn(cd.day, cd.week);

		CellSpan cellAtt = (CellSpan) getModel().getCellAttribute();
		if (cd.week == Weeks.EVEN && !cellAtt.isVisible(row, col)) {
			--col;
		}

		int[] cols;

		if (cd.week == Weeks.EACH) {
			cols = new int[] { col, col + 1 };
		}
		else {
			cols = new int[] { col };
		}

		addHighlightedCells(h, createRowRange(row, rowSpan(time)), cols);
	}

	public Integer[] getHighlightedRows() {
		Set<Integer> highlightedRows = new HashSet<Integer>();

		for (List<Cell> list : rendererCellMap.values()) {
			for (Cell c : list) {
				highlightedRows.add(c.row);
			}
		}

		return highlightedRows.toArray(new Integer[0]);
	}

	public Integer[] getHighlightedCols() {
		Set<Integer> highlightedCols = new HashSet<Integer>();

		for (List<Cell> list : rendererCellMap.values()) {
			for (Cell c : list) {
				highlightedCols.add(c.col);
			}
		}

		return highlightedCols.toArray(new Integer[0]);
	}

	private static class Cell {
		public final int row;
		public final int col;

		public Cell(int r, int c) {
			row = r;
			col = c;
		}
	}

	public static enum Highlights {
		ALLOWED, FORBIDDEN, SELECTED;
	}

	/*
	 * ===================
	 * NOTE: Other methods
	 * ===================
	 */

	public void changeSelection(int rowIndex, int colIndex, boolean toggle, boolean extend) {
		// Enable clicking the selected cell again to deslect it
		super.changeSelection(rowIndex, colIndex, !extend, extend);
	}
}
