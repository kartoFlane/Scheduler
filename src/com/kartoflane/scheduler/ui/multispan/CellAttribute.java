package com.kartoflane.scheduler.ui.multispan;

import java.awt.Dimension;


/**
 * @author Nobuo Tamemasa
 * @version 1.0 11/22/98
 */
public interface CellAttribute {

	public void addColumn();

	public void addRow();

	public void insertRow(int row);

	public Dimension getSize();

	public void setSize(Dimension size);

}