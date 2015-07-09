package com.kartoflane.scheduler.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.kartoflane.scheduler.catalog.Catalog;
import com.kartoflane.scheduler.catalog.CatalogIO;
import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.locale.LocaleStringKeys;
import com.kartoflane.scheduler.util.UIUtils;


@SuppressWarnings("serial")
public class ScraperFrame extends JDialog
		implements ActionListener {

	private static final int defaultFrameWidth = 450;
	private static final int defaultFrameHeight = 400;

	private final LocaleManager localem;
	private final Catalog catalog;

	private final JTextArea textArea;
	private final JRadioButton btnECL;
	private final JRadioButton btnAKZ;
	private final JButton btnScrape;
	private final JButton btnInfo;

	public ScraperFrame(JFrame parent, LocaleManager localeManager, Catalog cat) {
		super(parent, "Scraper");

		localem = localeManager;
		catalog = cat;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);

		setSize(defaultFrameWidth, defaultFrameHeight);
		setMinimumSize(new Dimension(defaultFrameWidth / 2, defaultFrameHeight / 2));
		// Make the frame appear at the center of the screen
		setLocationRelativeTo(null);

		/*
		 * ======================================================================================
		 */

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, 0.0 };

		setLayout(gridBagLayout);

		JScrollPane spText = new JScrollPane();

		GridBagConstraints gbc_spText = new GridBagConstraints();
		gbc_spText.gridwidth = 2;
		gbc_spText.insets = new Insets(0, 0, 5, 0);
		gbc_spText.fill = GridBagConstraints.BOTH;
		gbc_spText.gridx = 0;
		gbc_spText.gridy = 0;
		add(spText, gbc_spText);

		textArea = new JTextArea();
		spText.setViewportView(textArea);

		JPanel panelSource = new JPanel();
		panelSource.setBorder(BorderFactory.createTitledBorder(localem.getString(LocaleStringKeys.SCRAPER_SOURCE)));
		panelSource.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		GridBagConstraints gbc_panelSource = new GridBagConstraints();
		gbc_panelSource.gridwidth = 2;
		gbc_panelSource.insets = new Insets(0, 0, 5, 0);
		gbc_panelSource.fill = GridBagConstraints.BOTH;
		gbc_panelSource.gridx = 0;
		gbc_panelSource.gridy = 1;
		add(panelSource, gbc_panelSource);

		btnECL = new JRadioButton("Edukacja.CL");
		btnECL.setSelected(true);
		panelSource.add(btnECL);

		btnAKZ = new JRadioButton("AKZ");
		btnAKZ.setEnabled(false); // TODO remove once AKZ scraping works reliably
		panelSource.add(btnAKZ);

		ButtonGroup groupSource = new ButtonGroup();
		groupSource.add(btnECL);
		groupSource.add(btnAKZ);

		btnScrape = new JButton("Scrape");
		btnScrape.addActionListener(this);

		GridBagConstraints gbc_btnScrape = new GridBagConstraints();
		gbc_btnScrape.anchor = GridBagConstraints.WEST;
		gbc_btnScrape.insets = new Insets(0, 5, 5, 0);
		gbc_btnScrape.gridx = 0;
		gbc_btnScrape.gridy = 2;
		add(btnScrape, gbc_btnScrape);

		btnInfo = new JButton("Info");
		btnInfo.addActionListener(this);

		GridBagConstraints gbc_btnInfo = new GridBagConstraints();
		gbc_btnInfo.insets = new Insets(0, 0, 5, 5);
		gbc_btnInfo.anchor = GridBagConstraints.EAST;
		gbc_btnInfo.gridx = 1;
		gbc_btnInfo.gridy = 2;
		add(btnInfo, gbc_btnInfo);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == btnScrape && btnScrape.isEnabled()) {
			String text = textArea.getText();

			if (btnECL.isSelected()) {
				CatalogIO.scrapeECL(catalog, text, localem);
			}
			else if (btnAKZ.isSelected()) {
				CatalogIO.scrapeAKZ(catalog, text, localem);
			}
		}
		else if (source == btnInfo && btnInfo.isEnabled()) {
			String message = localem.getString(LocaleStringKeys.SCRAPER_INFO);
			message = UIUtils.toHTML(UIUtils.unescapeNewline(message));
			UIUtils.showInfoDialog(message);
		}
	}
}
