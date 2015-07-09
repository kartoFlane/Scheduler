package com.kartoflane.scheduler.ui.tables;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.kartoflane.scheduler.catalog.Catalog;
import com.kartoflane.scheduler.catalog.ClassData;
import com.kartoflane.scheduler.catalog.ClassGroup;
import com.kartoflane.scheduler.core.CourseTypes;
import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.core.IPredicate;
import com.kartoflane.scheduler.core.Weeks;
import com.kartoflane.scheduler.locale.LocaleManager;


@SuppressWarnings("serial")
public class CatalogTable extends JTable {

	private final LocaleManager localem;
	private IPredicate<ClassGroup> currentFilter;

	public CatalogTable(LocaleManager localem) {
		super(new CatalogTableModel(localem));

		this.localem = localem;

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		LocalizedTableCellRenderer localeRenderer = new LocalizedTableCellRenderer(localem);
		setDefaultRenderer(Days.class, localeRenderer);
		setDefaultRenderer(CourseTypes.class, localeRenderer);
		setDefaultRenderer(Weeks.class, localeRenderer);

		MultiCriteriaTableRowSorter<CatalogTableModel> sorter =
				new MultiCriteriaTableRowSorter<CatalogTableModel>(getModel());

		sorter.setSortingParams(CatalogTableModel.COLUMN_DAY, new int[] {
				// Sorting order, from most significant to least significant
				CatalogTableModel.COLUMN_TIME,
				CatalogTableModel.COLUMN_WEEK,
				CatalogTableModel.COLUMN_TYPE
		});
		sorter.setSortingParams(CatalogTableModel.COLUMN_WEEK, new int[] {
				CatalogTableModel.COLUMN_DAY,
				CatalogTableModel.COLUMN_TIME,
				CatalogTableModel.COLUMN_TYPE
		});
		sorter.setSortingParams(CatalogTableModel.COLUMN_TIME, new int[] {
				CatalogTableModel.COLUMN_DAY,
				CatalogTableModel.COLUMN_WEEK,
				CatalogTableModel.COLUMN_TYPE
		});
		sorter.setSortingParams(CatalogTableModel.COLUMN_COURSE, new int[] {
				CatalogTableModel.COLUMN_TYPE,
				CatalogTableModel.COLUMN_DAY,
				CatalogTableModel.COLUMN_TIME,
				CatalogTableModel.COLUMN_WEEK
		});

		Comparator<Object> comparator = new Comparator<Object>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public int compare(Object o1, Object o2) {
				if (o1.getClass().equals(o2.getClass())) {
					return ((Comparable) o1).compareTo(o2);
				}
				else if (o1 instanceof String) {
					return 1;
				}
				else if (o2 instanceof String) {
					return -1;
				}
				else {
					throw new ClassCastException(String.format("Comparing %s and %s", o1, o2));
				}
			}
		};

		sorter.setComparator(CatalogTableModel.COLUMN_DAY, comparator);
		sorter.setComparator(CatalogTableModel.COLUMN_TIME, comparator);
		sorter.setComparator(CatalogTableModel.COLUMN_WEEK, comparator);

		setRowSorter(sorter);

		TableColumnModel cm = getColumnModel();
		cm.getColumn(0).setMinWidth(100);
		cm.getColumn(0).setMaxWidth(100);
		cm.getColumn(1).setMinWidth(80);
		cm.getColumn(1).setMaxWidth(80);
		cm.getColumn(2).setMinWidth(70);
		cm.getColumn(2).setMaxWidth(70);
		cm.getColumn(3).setMinWidth(60);
		cm.getColumn(3).setMaxWidth(60);
		cm.getColumn(4).setMinWidth(30);
		cm.getColumn(4).setMaxWidth(30);
	}

	@Override
	public void setModel(TableModel model) {
		if (model instanceof CatalogTableModel) {
			super.setModel(model);
		}
		else {
			throw new IllegalArgumentException("Model must be instance of " +
					CatalogTableModel.class.getSimpleName());
		}
	}

	public CatalogTableModel getModel() {
		return (CatalogTableModel) super.getModel();
	}

	/*
	 * =============================
	 * NOTE: Current catalog control
	 * =============================
	 */

	public void setCurrentCatalog(Catalog cat) {
		getModel().setCurrentCatalog(cat);
	}

	public Catalog getCurrentCatalog() {
		return getModel().getCurrentCatalog();
	}

	public IPredicate<ClassGroup> getFilter() {
		return currentFilter;
	}

	/**
	 * Sets the filter for classes that should be shown in the table.
	 * Can be null to load all classes.
	 */
	public void setFilter(IPredicate<ClassGroup> filter) {
		currentFilter = filter;
	}

	/**
	 * Loads all classes from the catalog that match the filter specified
	 * by {@link #setFilter(IPredicate)} method.
	 */
	public void loadCatalog() {
		CatalogTableModel model = getModel();
		Catalog cat = model.getCurrentCatalog();

		if (cat == null) {
			return;
		}

		model.clear();

		for (ClassGroup cg : cat.getClassGroups()) {
			if (currentFilter == null || currentFilter.accept(cg)) {
				model.addRow(cg);
			}
		}
	}

	public void viewClassDetail(ClassData cd) {
		CatalogTableModel model = getModel();
		clear();
		model.addRow(cd);
	}

	/*
	 * ===================
	 * NOTE: Other methods
	 * ===================
	 */

	public ClassGroup getClassGroup(int modelRow) {
		return getModel().getRow(modelRow);
	}

	/**
	 * Removes all rows from the table.
	 */
	protected void clear() {
		setFilter(new IPredicate<ClassGroup>() {
			public boolean accept(ClassGroup cg) {
				return false;
			}
		});
		loadCatalog();
	}

	public void changeSelection(int rowIndex, int colIndex, boolean toggle, boolean extend) {
		// Enable clicking the selected cell again to deslect it
		super.changeSelection(rowIndex, colIndex, !extend, extend);
	}

	/**
	 * Implement table cell tooltips.
	 */
	public String getToolTipText(MouseEvent e) {
		String tooltip = null;
		Point p = e.getPoint();
		int row = rowAtPoint(p);
		int col = columnAtPoint(p);

		Object value = getValueAt(row, col);
		if (value != null) {
			String text = value.toString();

			if (text.equals(ClassGroup.MULTIPLE_VALUES)) {
				row = getRowSorter().convertRowIndexToModel(row);
				ClassGroup group = getClassGroup(row);

				StringBuilder buf = new StringBuilder();
				buf.append("<html>");
				for (ClassData cd : group.getClasses()) {
					buf.append(cd.getValueByColumn(localem, col));
					buf.append("<br/>");
				}
				buf.append("</html>");

				tooltip = buf.toString();
			}
		}

		return tooltip;
	}
}
