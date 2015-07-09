package com.kartoflane.scheduler.util;

import javax.swing.JOptionPane;


public class UIUtils {

	private UIUtils() {
		// Static class -- disallow instantiation.
	}

	public static void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showWarnDialog(String message) {
		JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
	}

	public static void showInfoDialog(String message) {
		JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	// @formatter:off
	/**
	 * Turns the message into HTML code, for use with JLabels, tooltips and such,
	 * to achieve multiline text.
	 * 
	 * <br/><br/>
	 * 
	 * {@literal
	 * Replaces '\n' with <br/>, '>' and '<' with their HTML equivalents, and wraps
	 * the message in <head></head> tags.
	 * }
	 */
	 // @formatter:on
	public static String toHTML(String msg) {
		msg = msg.replaceAll("<", "&lt;");
		msg = msg.replaceAll(">", "&gt;");
		msg = msg.replaceAll("\n", "<br/>");
		return "<html>" + msg + "</html>";
	}

	public static String unescapeNewline(String msg) {
		return msg.replace("\\n", "\n");
	}
}
