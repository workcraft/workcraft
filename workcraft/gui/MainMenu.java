package org.workcraft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainMenu extends JMenuBar {
	private static final long serialVersionUID = 1L;

	private String[] lafCaptions = new String[] {
			"Java default",
			"Substance: Moderate",
			"Substance: Mist Silver",
			"Substance: Raven",
			"Substance: Business",
			"Substance: Creme"
		};
	private String[] lafClasses = new String[] {
			"javax.swing.plaf.metal.MetalLookAndFeel",
			"org.jvnet.substance.skin.SubstanceModerateLookAndFeel",
			"org.jvnet.substance.skin.SubstanceMistSilverLookAndFeel",
			"org.jvnet.substance.skin.SubstanceRavenLookAndFeel",
			"org.jvnet.substance.skin.SubstanceBusinessLookAndFeel",
			"org.jvnet.substance.skin.SubstanceCremeCoffeeLookAndFeel"
		};

	MainMenu(final MainWindow frame) {
		// File
		JMenu mnFile = new JMenu();
		mnFile.setText("File");

		JMenuItem miExit = new JMenuItem();
		miExit.setText("Exit");
		miExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.framework.shutdown();
			}
		});

		JMenuItem miShutdownGUI = new JMenuItem();
		miShutdownGUI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.framework.shutdownGUI();
			}
		});
		miShutdownGUI.setText("Shutdown GUI");

		JMenuItem miOpenDocument = new JMenuItem();
		miOpenDocument.setText("Open document...");


		mnFile.add(miOpenDocument);
		mnFile.addSeparator();

		mnFile.addSeparator();
		mnFile.add(miShutdownGUI);
		mnFile.add(miExit);

		// Preferences
		JMenu mnPreferences = new JMenu();
		mnPreferences.setText("Preferences");

		JMenu mnLAF = new JMenu();
		mnLAF.setText("Look and Feel");

		for(int i=0; i<lafClasses.length; i++) {
			JMenuItem miLAFItem = new JMenuItem();
			miLAFItem.setText(lafCaptions[i]);
			final String lafClass = lafClasses[i];
			miLAFItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.setLAF(lafClass);
				}
			});
			mnLAF.add(miLAFItem);
		}

		JMenuItem miReconfigure = new JMenuItem("Reconfigure plugins");
		miReconfigure.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.framework.getPluginManager().reconfigure();
			}
		});

		mnPreferences.add(mnLAF);
		mnPreferences.addSeparator();
		mnPreferences.add(miReconfigure);

		add(mnFile);
		add(mnPreferences);
	}

}
