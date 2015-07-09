package com.kartoflane.scheduler.ui.multispan;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableCellRenderer;


/**
 * @author Nobuo Tamemasa, Tomasz 'kartoFlane' Bachminski
 * @version 1.0 11/26/98
 */
public class MultiSpanCellTableUI extends BasicTableUI {

	private static Color disabledColor = new Color(0, 0, 0, 40);

	public void paint(Graphics g, JComponent c) {
		Rectangle oldClipBounds = g.getClipBounds();
		Rectangle clipBounds = new Rectangle(oldClipBounds);
		int tableWidth = table.getColumnModel().getTotalColumnWidth();
		clipBounds.width = Math.min(clipBounds.width, tableWidth);
		g.setClip(clipBounds);

		int firstIndex = table.rowAtPoint(new Point(0, clipBounds.y));
		int lastIndex = table.getRowCount() - 1;

		Rectangle rowRect = new Rectangle(0, 0,
				tableWidth, table.getRowHeight() + table.getRowMargin());
		rowRect.y = firstIndex * rowRect.height;

		for (int index = firstIndex; index <= lastIndex; index++) {
			if (rowRect.intersects(clipBounds)) {
				// System.out.println(); // debug
				// System.out.print("" + index +": "); // row
				paintRow(g, index);
			}
			rowRect.y += rowRect.height;
		}

		// @author kartoFlane -- added grey overlay for when the table is disabled
		if (!table.isEnabled()) {
			int tableHeight = table.getHeight();
			Color cl = g.getColor();
			g.setColor(disabledColor);
			g.fillRect(0, 0, tableWidth, tableHeight);
			g.setColor(cl);
		}

		g.setClip(oldClipBounds);
	}

	private void paintRow(Graphics g, int row) {
		Rectangle rect = g.getClipBounds();
		boolean drawn = false;

		AttributiveCellTableModel tableModel = (AttributiveCellTableModel) table.getModel();
		CellSpan cellAtt = (CellSpan) tableModel.getCellAttribute();
		int numColumns = table.getColumnCount();

		for (int column = 0; column < numColumns; column++) {
			Rectangle cellRect = table.getCellRect(row, column, true);
			int cellRow, cellColumn;
			if (cellAtt.isVisible(row, column)) {
				cellRow = row;
				cellColumn = column;
				// System.out.print("   "+column+" "); // debug
			}
			else {
				cellRow = row + cellAtt.getSpan(row, column)[CellSpan.ROW];
				cellColumn = column + cellAtt.getSpan(row, column)[CellSpan.COLUMN];
				// System.out.print("  ("+column+")"); // debug
			}
			if (cellRect.intersects(rect)) {
				drawn = true;
				paintCell(g, cellRect, cellRow, cellColumn);
			}
			else if (drawn) {
				break;
			}
		}
	}

	private void paintCell(Graphics g, Rectangle cellRect, int row, int column) {
		int spacingHeight = table.getRowMargin();
		int spacingWidth = table.getColumnModel().getColumnMargin();

		cellRect.setBounds(cellRect.x + spacingWidth / 2, cellRect.y + spacingHeight / 2,
				cellRect.width - spacingWidth, cellRect.height - spacingHeight);

		if (table.isEditing() && table.getEditingRow() == row &&
				table.getEditingColumn() == column) {
			Component component = table.getEditorComponent();
			component.setBounds(cellRect);
			component.validate();
		}
		else {
			TableCellRenderer renderer = table.getCellRenderer(row, column);
			Component component = table.prepareRenderer(renderer, row, column);

			if (component.getParent() == null) {
				rendererPane.add(component);
			}
			rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y,
					cellRect.width, cellRect.height, true);
		}

		// @author kartoFlane -- draw the border after everything else so that the cell can
		// have a custom background color. Also test for showGrid()
		if (table.getShowHorizontalLines() && table.getShowVerticalLines()) {
			Color c = g.getColor();
			g.setColor(table.getGridColor());
			g.drawRect(cellRect.x, cellRect.y, cellRect.width, cellRect.height);
			g.setColor(c);
		}
	}
}