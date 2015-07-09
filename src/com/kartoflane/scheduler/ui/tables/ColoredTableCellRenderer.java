package com.kartoflane.scheduler.ui.tables;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * This renderer draws the cell with a colored background, if it is not selected.
 * 
 * Due to the way renderers are used by JTables (as a rubber-stamp), there is no way to have
 * the renderer only draw the cell specified by the 'row' and 'col' parameters in the
 * getTableCellRendererComponent method. As such, this renderer should be applied on a cell-by-cell
 * basis to cells that should be drawn with the selected background.
 *
 */
@SuppressWarnings("serial")
public class ColoredTableCellRenderer extends DefaultTableCellRenderer {

	private final Color backgroundColor;

	public ColoredTableCellRenderer(Color c) {
		backgroundColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}

	public ColoredTableCellRenderer(int red, int green, int blue, int alpha) {
		backgroundColor = new Color(red, green, blue, alpha);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

		if (!isSelected) {
			c.setBackground(backgroundColor);
		}

		return c;
	}
}
