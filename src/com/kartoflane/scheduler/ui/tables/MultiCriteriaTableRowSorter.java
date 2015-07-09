package com.kartoflane.scheduler.ui.tables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * Sometimes, a row in a table represents a single, cohesive object that had to be
 * artificially scattered throughout the columns in order to be represented in
 * the table; however, many of this object's properties may be relevant to its
 * sorting order.
 * 
 * RowSorters don't normally support referencing values from other columns in
 * case that entries in one column are equal, hence this workaround.
 * 
 * What makes this solution work is the fact that RowSorters use the data from
 * the view, instead of directly from the model (model's internal layout of rows
 * is separate from the view's)
 * 
 * Obviously, this sorter won't work well for columns that have no equal values.
 * 
 * Usage:
 * 
 * int[] colsToSortBy = new int[] { ... };
 * sorter.setSortingParams(clickedCol, colsToSortBy);
 * 
 * 
 * @author kartoFlane
 */
public class MultiCriteriaTableRowSorter<T extends TableModel> extends TableRowSorter<T> {

	private final Map<Integer, Map<Integer, SortOrder>> colOrderMap = new HashMap<Integer, Map<Integer, SortOrder>>();
	private final Map<Integer, Comparator<?>> colComparatorMap = new HashMap<Integer, Comparator<?>>();
	private SortOrder defaultSortOrder = SortOrder.ASCENDING;

	public MultiCriteriaTableRowSorter(T model) {
		super(model);
	}

	/**
	 * The default sorting direction for columns that have not specified
	 * a preferred direction.
	 */
	public void setDefaultSortOrder(SortOrder order) {
		if (order == null) {
			throw new IllegalArgumentException("Argument must not be null.");
		}
		checkSortOrder(order);

		defaultSortOrder = order;
	}

	public SortOrder getDefaultSortOrder() {
		return defaultSortOrder;
	}

	public void setComparator(int column, Comparator<?> comp) {
		colComparatorMap.put(column, comp);
	}

	@Override
	public Comparator<?> getComparator(int column) {
		Comparator<?> comp = colComparatorMap.get(column);

		if (comp != null) {
			return comp;
		}
		else {
			return super.getComparator(column);
		}
	}

	/**
	 * 
	 * @param col
	 *            the primary column, the one that the user clicks
	 * @param secondarySortCols
	 *            the other columns to sort by, listed in order from
	 *            most significant to least significant
	 */
	public void setSortingParams(int col, int[] secondarySortCols) {
		Map<Integer, SortOrder> priorityMap = new LinkedHashMap<Integer, SortOrder>();
		for (int c : secondarySortCols) {
			priorityMap.put(c, null);
		}
		colOrderMap.put(col, priorityMap);
	}

	/**
	 * 
	 * @param col
	 *            the primary column, the one that the user clicks
	 * @param secondarySortCols
	 *            the other columns to sort by, listed in order from
	 *            most significant to least significant
	 * @param order
	 *            array of the respective columns' preferred sorting direction
	 */
	public void setSortingParams(int col, int[] secondarySortCols, SortOrder[] order) {
		Map<Integer, SortOrder> priorityMap = new LinkedHashMap<Integer, SortOrder>();
		for (int i = 0; i < secondarySortCols.length; ++i) {
			checkSortOrder(order[i]);
			priorityMap.put(secondarySortCols[i], order[i]);
		}
		colOrderMap.put(col, priorityMap);
	}

	@Override
	public void toggleSortOrder(int column) {
		super.toggleSortOrder(column);

		List<SortKey> newKeys = new ArrayList<SortKey>();

		// Primary sorting parameter
		SortOrder order = getSortKeys().get(0).getSortOrder();
		newKeys.add(new SortKey(column, order));

		// Secondary sorting parameters -- used when objects are equal according
		// to the previous parameter
		Map<Integer, SortOrder> param = colOrderMap.get(column);
		if (param != null) {
			for (Map.Entry<Integer, SortOrder> entry : param.entrySet()) {
				int col = entry.getKey();
				if (col != column) {
					order = entry.getValue();
					newKeys.add(new SortKey(col, order == null ? defaultSortOrder : order));
				}
			}
		}

		setSortKeys(newKeys);
	}

	private static void checkSortOrder(SortOrder order) {
		if (order == null)
			return;
		switch (order) {
			case ASCENDING:
			case DESCENDING:
				break;
			default:
				throw new IllegalArgumentException("" + order);
		}
	}
}
