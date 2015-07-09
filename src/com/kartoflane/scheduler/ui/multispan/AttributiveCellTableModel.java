package com.kartoflane.scheduler.ui.multispan;

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;


/**
 * <pre>
 * //   setDataVector was throwing a recursive exception,
 * // by commenting a line out, the source now only
 * // causes a singular NPE.
 * </pre>
 * 
 * @author Nobuo Tamemasa, Andrew Thompson, Tomasz 'kartoFlane' Bachminski
 * @version 1.0 11/22/98, 1.1 2004/03/05
 */
@SuppressWarnings("serial")
public class AttributiveCellTableModel extends DefaultTableModel {

	protected CellAttribute cellAtt;

	public AttributiveCellTableModel() {
		this((Vector) null, 0);
	}

	public AttributiveCellTableModel(int numRows, int numColumns) {
		Vector names = new Vector(numColumns);
		names.setSize(numColumns);
		setColumnIdentifiers(names);
		dataVector = new Vector();
		setNumRows(numRows);
		cellAtt = new DefaultCellAttribute(numRows, numColumns);
	}

	public AttributiveCellTableModel(Vector columnNames, int numRows) {
		setColumnIdentifiers(columnNames);
		dataVector = new Vector();
		setNumRows(numRows);
		cellAtt = new DefaultCellAttribute(numRows, columnNames.size());
	}

	public AttributiveCellTableModel(Object[] columnNames, int numRows) {
		this(convertToVector(columnNames), numRows);
	}

	public AttributiveCellTableModel(Vector data, Vector columnNames) {
		setDataVector(data, columnNames);
	}

	public AttributiveCellTableModel(Object[][] data, Object[] columnNames) {
		setDataVector(data, columnNames);
	}

	/**
	 * Previously changing the row count of the table at runtime would cause it to bug out,
	 * due to the CellAttribute not being updated.
	 * 
	 * @author kartoFlane
	 */
	public void setNumRows(int rowCount) {
		super.setNumRows(rowCount);
		cellAtt.setSize(new Dimension(columnIdentifiers.size(), rowCount));
	}

	public void setDataVector(Vector newData, Vector columnNames) {
		if (newData == null)
			throw new IllegalArgumentException("setDataVector() - Null parameter");

		super.setDataVector(newData, columnNames);

		dataVector = new Vector(0);

		dataVector = newData;

		// this was throwing a recursive exception,
		// by commentin it out, the source now only
		// causes a singular exception.
		// setColumnIdentifiers(columnNames);
		cellAtt = new DefaultCellAttribute(dataVector.size(),
				// columnNames.size());
				columnIdentifiers.size());

		newRowsAdded(new TableModelEvent(this, 0, getRowCount() - 1,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	public void setColumnIdentifiers(Vector columnIdentifiers) {
		super.setDataVector(dataVector, columnIdentifiers);
	}

	public void addColumn(Object columnName, Vector columnData) {
		if (columnName == null)
			throw new IllegalArgumentException("addColumn() - null parameter");
		columnIdentifiers.addElement(columnName);
		int index = 0;
		Enumeration enumeration = dataVector.elements();
		while (enumeration.hasMoreElements()) {
			Object value;
			if ((columnData != null) && (index < columnData.size()))
				value = columnData.elementAt(index);
			else
				value = null;
			((Vector) enumeration.nextElement()).addElement(value);
			index++;
		}

		//
		cellAtt.addColumn();

		fireTableStructureChanged();
	}

	public void addRow(Vector rowData) {
		Vector newData = null;
		if (rowData == null) {
			newData = new Vector(getColumnCount());
		}
		else {
			rowData.setSize(getColumnCount());
		}
		dataVector.addElement(newData);

		//
		cellAtt.addRow();

		newRowsAdded(new TableModelEvent(this, getRowCount() - 1, getRowCount() - 1,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	public void insertRow(int row, Vector rowData) {
		if (rowData == null) {
			rowData = new Vector(getColumnCount());
		}
		else {
			rowData.setSize(getColumnCount());
		}

		dataVector.insertElementAt(rowData, row);

		//
		cellAtt.insertRow(row);

		newRowsAdded(new TableModelEvent(this, row, row,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	public CellAttribute getCellAttribute() {
		return cellAtt;
	}

	public void setCellAttribute(CellAttribute newCellAtt) {
		int numColumns = getColumnCount();
		int numRows = getRowCount();
		if ((newCellAtt.getSize().width != numColumns) ||
				(newCellAtt.getSize().height != numRows)) {
			newCellAtt.setSize(new Dimension(numRows, numColumns));
		}
		cellAtt = newCellAtt;
		fireTableDataChanged();
	}
}