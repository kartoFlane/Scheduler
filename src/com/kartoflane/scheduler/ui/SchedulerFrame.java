package com.kartoflane.scheduler.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.kartoflane.scheduler.Scheduler;
import com.kartoflane.scheduler.catalog.Catalog;
import com.kartoflane.scheduler.catalog.CatalogIO;
import com.kartoflane.scheduler.catalog.ClassData;
import com.kartoflane.scheduler.catalog.ClassGroup;
import com.kartoflane.scheduler.catalog.Schedule;
import com.kartoflane.scheduler.catalog.ScheduleIO;
import com.kartoflane.scheduler.core.ClassAlreadyEnrolledException;
import com.kartoflane.scheduler.core.ClassOverlapException;
import com.kartoflane.scheduler.core.CourseAlreadyEnrolledException;
import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.core.GroupFilter;
import com.kartoflane.scheduler.core.IPredicate;
import com.kartoflane.scheduler.core.TimeInterval;
import com.kartoflane.scheduler.core.Weeks;
import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.locale.LocaleStringKeys;
import com.kartoflane.scheduler.ui.tables.CatalogTable;
import com.kartoflane.scheduler.ui.tables.ScheduleTable;
import com.kartoflane.scheduler.ui.tables.ScheduleTable.Highlights;
import com.kartoflane.scheduler.util.UIUtils;


@SuppressWarnings("serial")
public class SchedulerFrame extends JFrame
		implements ActionListener, ListSelectionListener {

	private static final Logger log = LogManager.getLogger(SchedulerFrame.class);

	private static final int defaultFrameWidth = 900;
	private static final int defaultFrameHeight = 600;

	private static final String shiftFocus = "com.kartoflane.scheduler:SHIFT_FOCUS";
	private static final String tableDeselect = "com.kartoflane.scheduler:DESELECT";
	private static final String classRemove = "com.kartoflane.scheduler:CLASS_REMOVE";
	private static final String classAdd = "com.kartoflane.scheduler:CLASS_ADD";

	private final LocaleManager localem;

	private final CatalogTable tbCatalog;
	private final ScheduleTable tbSchedule;

	private final JMenuItem mntmNewSchedule;
	private final JMenuItem mntmLoadSchedule;
	private final JMenuItem mntmSaveSchedule;

	private final JMenuItem mntmNewCatalog;
	private final JMenuItem mntmLoadCatalog;
	private final JMenuItem mntmCreateCatalog;
	private final JMenuItem mntmSaveCatalog;

	public SchedulerFrame(LocaleManager localeManager) {
		super(Scheduler.APP_NAME);

		localem = localeManager;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setSize(defaultFrameWidth, defaultFrameHeight);
		setMinimumSize(new Dimension(defaultFrameWidth, defaultFrameHeight / 2));
		// Make the frame appear at the center of the screen
		setLocationRelativeTo(null);

		/*
		 * ======================================================================================
		 * Menu bar
		 */

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnSchedule = new JMenu(localem.getString(LocaleStringKeys.SCHEDULE));
		menuBar.add(mnSchedule);

		mntmNewSchedule = new JMenuItem(localem.getString(LocaleStringKeys.NEW_SCHEDULE));
		mntmNewSchedule.addActionListener(this);
		mntmNewSchedule.setEnabled(false);

		mntmLoadSchedule = new JMenuItem(localem.getString(LocaleStringKeys.LOAD_SCHEDULE));
		mntmLoadSchedule.addActionListener(this);
		mntmLoadSchedule.setEnabled(false);

		mntmSaveSchedule = new JMenuItem(localem.getString(LocaleStringKeys.SAVE_SCHEDULE));
		mntmSaveSchedule.addActionListener(this);
		mntmSaveSchedule.setEnabled(false);

		mnSchedule.add(mntmNewSchedule);
		mnSchedule.add(mntmLoadSchedule);
		mnSchedule.addSeparator();
		mnSchedule.add(mntmSaveSchedule);

		JMenu mnCatalog = new JMenu(localem.getString(LocaleStringKeys.CATALOG));
		menuBar.add(mnCatalog);

		mntmNewCatalog = new JMenuItem(localem.getString(LocaleStringKeys.NEW_CATALOG));
		mntmNewCatalog.addActionListener(this);

		mntmLoadCatalog = new JMenuItem(localem.getString(LocaleStringKeys.LOAD_CATALOG));
		mntmLoadCatalog.addActionListener(this);

		mntmSaveCatalog = new JMenuItem(localem.getString(LocaleStringKeys.SAVE_CATALOG));
		mntmSaveCatalog.addActionListener(this);
		mntmSaveCatalog.setEnabled(false);

		mntmCreateCatalog = new JMenuItem(localem.getString(LocaleStringKeys.GEN_CATALOG));
		mntmCreateCatalog.addActionListener(this);
		mntmCreateCatalog.setEnabled(false);

		mnCatalog.add(mntmNewCatalog);
		mnCatalog.add(mntmLoadCatalog);
		mnCatalog.addSeparator();
		mnCatalog.add(mntmSaveCatalog);
		mnCatalog.addSeparator();
		mnCatalog.add(mntmCreateCatalog);

		/*
		 * ======================================================================================
		 * Split pane & tables
		 */

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setContinuousLayout(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);

		/*
		 * ======================================================================================
		 * Schedule table
		 */

		JScrollPane spSchedule = new JScrollPane(tbSchedule = new ScheduleTable(localem));
		splitPane.setLeftComponent(spSchedule);

		tbSchedule.getSelectionModel().addListSelectionListener(this);
		tbSchedule.getColumnModel().getSelectionModel().addListSelectionListener(this);

		tbSchedule.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (!tbCatalog.isEnabled() || !tbSchedule.isEnabled()) {
					return;
				}

				if (e.getClickCount() == 2) {
					if (tbSchedule.getSelectedColumn() < 0 || tbSchedule.getSelectedRow() < 0) {
						// The table has toggle selection enabled; clicking the same cell twice deselects it.
						// Reselect the cell.
						selectCell(tbSchedule, e.getPoint());
					}

					if (tbSchedule.getSelectedColumn() > 0) {
						Days day = tbSchedule.getSelectedDay();
						Weeks week = tbSchedule.getSelectedWeek();
						TimeInterval ti = tbSchedule.getSelectedTime();

						Schedule schedule = tbSchedule.getCurrentSchedule();
						ClassData cd = schedule.getClass(day, week, ti);

						if (cd != null && schedule.remove(cd.group)) {
							tbSchedule.loadSchedule();
							updateCatalogFilter();
						}
					}
				}
			}
		});

		tbSchedule.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), tableDeselect);
		tbSchedule.getActionMap().put(tableDeselect, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// Deselect when esc pressed
				tbSchedule.clearSelection();
			}
		});

		tbSchedule.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), classRemove);
		tbSchedule.getActionMap().put(classRemove, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// Remove the selected group, if possible
				if (!tbCatalog.isEnabled() || !tbSchedule.isEnabled() || tbSchedule.getSelectedColumn() < 1) {
					return;
				}

				Days day = tbSchedule.getSelectedDay();
				Weeks week = tbSchedule.getSelectedWeek();
				TimeInterval ti = tbSchedule.getSelectedTime();

				Schedule schedule = tbSchedule.getCurrentSchedule();
				ClassData cd = schedule.getClass(day, week, ti);

				if (cd != null && schedule.remove(cd.group)) {
					tbSchedule.loadSchedule();
					updateCatalogFilter();
				}
			}
		});

		tbSchedule.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
				put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), shiftFocus);
		tbSchedule.getActionMap().put(shiftFocus, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// Switch focus to the other table
				tbCatalog.requestFocusInWindow();
			}
		});

		/*
		 * ======================================================================================
		 * Catalog table
		 */

		JScrollPane spCatalog = new JScrollPane(tbCatalog = new CatalogTable(localem));
		splitPane.setRightComponent(spCatalog);

		tbCatalog.getSelectionModel().addListSelectionListener(this);

		tbCatalog.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (!tbCatalog.isEnabled() || !tbSchedule.isEnabled()) {
					return;
				}

				if (e.getClickCount() == 2) {
					if (tbCatalog.getSelectedColumn() == -1 || tbCatalog.getSelectedRow() == -1) {
						// The table has toggle selection enabled; clicking the same cell twice deselects it.
						// Reselect the cell.
						selectCell(tbCatalog, e.getPoint());
					}

					try {
						int row = tbCatalog.getSelectedRow();
						row = tbCatalog.getRowSorter().convertRowIndexToModel(row);
						ClassGroup cg = tbCatalog.getClassGroup(row);

						if (cg != null && tbSchedule.getCurrentSchedule().add(cg)) {
							tbSchedule.loadSchedule();
							updateCatalogFilter();
						}
					}
					catch (ClassOverlapException ex) {
						ClassData cd = ex.getClassData();
						log.trace(String.format("Class slot already taken: %s %s, %s", cd.day, cd.week, cd.getTime()));
						UIUtils.showWarnDialog(localem.getString(LocaleStringKeys.MSG_OVERLAP));
					}
					catch (ClassAlreadyEnrolledException ex) {
						log.trace(String.format("Already enrolled in this class: %s", ex.getClassData()));
						UIUtils.showWarnDialog(localem.getString(LocaleStringKeys.MSG_ENROLLED_CLASS));
					}
					catch (CourseAlreadyEnrolledException ex) {
						log.trace(String.format("Already enrolled in this course: %s", ex.getClassGroup()));
						UIUtils.showWarnDialog(localem.getString(LocaleStringKeys.MSG_ENROLLED_GROUP));
					}
				}
			}
		});

		tbCatalog.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), tableDeselect);
		tbCatalog.getActionMap().put(tableDeselect, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// Deselect when esc pressed
				tbCatalog.clearSelection();
			}
		});

		tbCatalog.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), classAdd);
		tbCatalog.getActionMap().put(classAdd, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// Add the selected group, if possible
				int row = tbCatalog.getSelectedRow();
				if (!tbCatalog.isEnabled() || !tbSchedule.isEnabled() || row < 0) {
					return;
				}

				try {
					row = tbCatalog.getRowSorter().convertRowIndexToModel(row);
					ClassGroup cg = tbCatalog.getClassGroup(row);

					if (cg != null && tbSchedule.getCurrentSchedule().add(cg)) {
						tbSchedule.loadSchedule();
						updateCatalogFilter();
					}
				}
				catch (ClassOverlapException ex) {
					ClassData cd = ex.getClassData();
					log.trace(String.format("Class slot already taken: %s %s, %s", cd.day, cd.week, cd.getTime()));
					UIUtils.showWarnDialog(localem.getString(LocaleStringKeys.MSG_OVERLAP));
				}
				catch (ClassAlreadyEnrolledException ex) {
					log.trace(String.format("Already enrolled in this class: %s", ex.getClassData()));
					UIUtils.showWarnDialog(localem.getString(LocaleStringKeys.MSG_ENROLLED_CLASS));
				}
				catch (CourseAlreadyEnrolledException ex) {
					log.trace(String.format("Already enrolled in this course: %s", ex.getClassGroup()));
					UIUtils.showWarnDialog(localem.getString(LocaleStringKeys.MSG_ENROLLED_GROUP));
				}
			}
		});

		tbCatalog.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
				put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), shiftFocus);
		tbCatalog.getActionMap().put(shiftFocus, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// Switch focus to the other table
				tbSchedule.requestFocusInWindow();
			}
		});
	}

	/** Initialization method. Call after the frame is created. */
	public void prepareWorkspace() {
		if (tbCatalog.getCurrentCatalog() == null) {
			tbCatalog.setFilter(null);
		}
		else {
			tbCatalog.setFilter(new GroupFilter(
					tbSchedule.getCurrentSchedule(),
					tbSchedule.getSelectedDay(),
					tbSchedule.getSelectedWeek(),
					tbSchedule.getSelectedTime()));

			tbSchedule.setCurrentSchedule(new Schedule());

			mntmNewSchedule.setEnabled(true);
			mntmSaveSchedule.setEnabled(true);
			mntmLoadSchedule.setEnabled(true);
			mntmCreateCatalog.setEnabled(true);
			mntmSaveCatalog.setEnabled(true);
		}
		tbCatalog.getRowSorter().toggleSortOrder(0);
	}

	private void selectCell(JTable table, Point p) {
		int col = table.columnAtPoint(p);
		int row = table.rowAtPoint(p);
		table.addColumnSelectionInterval(col, col);
		table.addRowSelectionInterval(row, row);
	}

	/*
	 * ==================
	 * NOTE: File control
	 * ==================
	 */

	/**
	 * Add extension to a file that doesn't have an extension.<br>
	 * This method is useful to automatically add an extension in the savefileDialog control.
	 * 
	 * @param file
	 *            file to check
	 * @param ext
	 *            extension to add
	 * @return file with extension (e.g. 'test.doc')
	 */
	private String addFileExtIfNecessary(String file, String ext) {
		if (file.lastIndexOf('.') == -1)
			file += ext;

		return file;
	}

	public boolean loadSchedule(Catalog catalog, File f) {
		try {
			tbSchedule.setCurrentSchedule(ScheduleIO.read(catalog, f));
			tbSchedule.loadSchedule();
			return true;
		}
		catch (JsonProcessingException e) {
			log.error("Error occurred while parsing schedule file " + f.getPath(), e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_SCHEDULE_LOAD_FAIL_PARSE));
		}
		catch (IOException e) {
			log.error("Error occurred while reading schedule file " + f.getPath(), e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_SCHEDULE_LOAD_FAIL_READ));
		}
		catch (ClassOverlapException e) {
			log.error(String.format("Failed to load schedule file %s; contains overlapping classes: %s", f.getPath(), e.getClassData()));
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_SCHEDULE_LOAD_FAIL_OVERLAP));
		}
		catch (ClassAlreadyEnrolledException e) {
			log.error(String.format("Failed to load schedule file %s; contains duplicate classes: %s", f.getPath(), e.getClassData()));
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_SCHEDULE_LOAD_FAIL_ENROLLED_CLASS));
		}
		catch (CourseAlreadyEnrolledException e) {
			log.error(String.format("Failed to load schedule file %s; contains duplicate courses: %s", f.getPath(), e.getClassGroup()));
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_SCHEDULE_LOAD_FAIL_ENROLLED_GROUP));
		}
		catch (IllegalArgumentException e) {
			log.error(String.format("An error occurred while loading schedule file %s: %s", f.getPath(), e.getMessage()));
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_SCHEDULE_LOAD_FAIL));
		}

		return false;
	}

	public void saveSchedule(Schedule schedule, File f) {
		try {
			ScheduleIO.write(schedule, f);
		}
		catch (JsonGenerationException e) {
			log.error("Error occurred while generating JSON code for schedule:", e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_SCHEDULE_SAVE_FAIL_JSON));
		}
		catch (JsonMappingException e) {
			log.error("Error occurred while generating JSON code for schedule:", e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_SCHEDULE_SAVE_FAIL_JSON));
		}
		catch (IOException e) {
			log.error("Error occured while writing schedule file:", e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_SCHEDULE_SAVE_FAIL));
		}
	}

	public boolean loadCatalog(File f) {
		try {
			tbCatalog.setCurrentCatalog(CatalogIO.read(f));
			tbCatalog.loadCatalog();
			return true;
		}
		catch (JsonProcessingException e) {
			log.error("Error occurred while parsing catalog file " + f.getPath(), e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_CATALOG_LOAD_FAIL_PARSE));
		}
		catch (IOException e) {
			log.error("Error occurred while reading catalog file " + f.getPath(), e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_CATALOG_LOAD_FAIL_READ));
		}

		return false;
	}

	public void saveCatalog(Catalog cat, File f) {
		try {
			CatalogIO.write(tbCatalog.getCurrentCatalog(), f);
		}
		catch (JsonGenerationException e) {
			log.error("Error occurred while generating JSON code for catalog:", e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_CATALOG_SAVE_FAIL_JSON));
		}
		catch (JsonMappingException e) {
			log.error("Error occurred while generating JSON code for catalog:", e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_CATALOG_SAVE_FAIL_JSON));
		}
		catch (IOException e) {
			log.error("Error occured while writing catalog file:", e);
			UIUtils.showErrorDialog(localem.getString(LocaleStringKeys.MSG_CATALOG_SAVE_FAIL));
		}
	}

	/*
	 * ======================
	 * NOTE: Listener methods
	 * ======================
	 */

	private void updateCatalogFilter() {
		IPredicate<ClassGroup> prevFilter = tbCatalog.getFilter();
		IPredicate<ClassGroup> newFilter = new GroupFilter(
				tbSchedule.getCurrentSchedule(),
				tbSchedule.getSelectedDay(),
				tbSchedule.getSelectedWeek(),
				tbSchedule.getSelectedTime());

		if (!newFilter.equals(prevFilter)) {
			ClassData cd = tbSchedule.getSelectedClassData();

			if (cd == null) {
				tbCatalog.setFilter(newFilter);
				tbCatalog.loadCatalog();
			}
			else {
				tbCatalog.viewClassDetail(cd);
			}
		}
		else {
			tbCatalog.loadCatalog();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == mntmNewSchedule) {
			if (!mntmNewSchedule.isEnabled())
				return;

			tbSchedule.setCurrentSchedule(new Schedule());

			mntmSaveSchedule.setEnabled(true);
		}
		else if (source == mntmLoadSchedule) {
			if (!mntmLoadSchedule.isEnabled())
				return;

			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));

			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				Catalog catalog = tbCatalog.getCurrentCatalog();

				if (catalog == null) {
					UIUtils.showWarnDialog(localem.getString(LocaleStringKeys.MSG_NEED_CATALOG));
				}
				else {
					boolean result = loadSchedule(catalog, file);
					mntmSaveSchedule.setEnabled(result);
				}
			}
		}
		else if (source == mntmSaveSchedule) {
			if (!mntmSaveSchedule.isEnabled())
				return;

			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = new File(addFileExtIfNecessary(chooser.getSelectedFile().getAbsolutePath(), ".json"));
				saveSchedule(tbSchedule.getCurrentSchedule(), file);
			}
		}
		else if (source == mntmNewCatalog) {
			if (!mntmNewCatalog.isEnabled())
				return;

			tbCatalog.setCurrentCatalog(new Catalog());
			updateCatalogFilter();

			mntmNewSchedule.setEnabled(true);
			mntmLoadSchedule.setEnabled(true);
			mntmCreateCatalog.setEnabled(true);
			mntmSaveCatalog.setEnabled(true);
		}
		else if (source == mntmCreateCatalog) {
			if (!mntmCreateCatalog.isEnabled())
				return;

			ScraperFrame scraper = new ScraperFrame(this, localem, tbCatalog.getCurrentCatalog());
			scraper.setVisible(true);
		}
		else if (source == mntmLoadCatalog) {
			if (!mntmLoadCatalog.isEnabled())
				return;

			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
			boolean result = false;

			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				result = loadCatalog(file);
			}

			mntmNewSchedule.setEnabled(result);
			mntmLoadSchedule.setEnabled(result);
			mntmCreateCatalog.setEnabled(result);
			mntmSaveCatalog.setEnabled(result);
		}
		else if (source == mntmSaveCatalog) {
			if (!mntmSaveCatalog.isEnabled())
				return;

			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = new File(addFileExtIfNecessary(chooser.getSelectedFile().getAbsolutePath(), ".json"));
				saveCatalog(tbCatalog.getCurrentCatalog(), file);
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		Object source = e.getSource();

		if (tbSchedule.isEnabled() && source == tbCatalog.getSelectionModel()) {
			int row = tbCatalog.getSelectedRow();

			tbSchedule.clearHighlight();
			if (row >= 0) {
				row = tbCatalog.getRowSorter().convertRowIndexToModel(tbCatalog.getSelectedRow());
				ClassGroup cg = tbCatalog.getClassGroup(row);

				if (cg != null) {
					tbSchedule.clearHighlight();

					for (ClassData cd : cg.getClasses()) {
						Highlights h = Highlights.ALLOWED;
						ClassData other = tbSchedule.getCurrentSchedule().getClass(cd.day, cd.week, cd.getTime());

						if (other == cd) {
							h = Highlights.SELECTED;
						}
						else if (other != null ||
								!tbSchedule.getCurrentSchedule().isFree(cd.day, cd.week, cd.getTime())) {
							h = Highlights.FORBIDDEN;
						}

						tbSchedule.addHighlightedClass(h, cd);
					}
				}
			}

			tbSchedule.repaint();
		}
		else if (source == tbSchedule.getSelectionModel() ||
				source == tbSchedule.getColumnModel().getSelectionModel()) {
			// FIXME: two events get sent on selection & deselection
			// ignore one of them or split into two separate if-blocks?

			updateCatalogFilter();
		}
	}
}
