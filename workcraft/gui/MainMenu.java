package org.workcraft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.workcraft.gui.actions.ScriptedActionMenuItem;

public class MainMenu extends JMenuBar {
	private static final long serialVersionUID = 1L;

	JMenu mnFile, mnEdit, mnView, mnTools, mnSettings, mnHelp;
	JMenuItem miShowPropertyEditor;

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
		mnFile = new JMenu();
		mnFile.setText("File");

		ScriptedActionMenuItem miNewModel = new ScriptedActionMenuItem(MainWindow.Actions.CREATE_WORK_ACTION);
		miNewModel.setMnemonic(KeyEvent.VK_N);
		miNewModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		miNewModel.addScriptedActionListener(frame.getDefaultActionListener());

		ScriptedActionMenuItem miExit = new ScriptedActionMenuItem(MainWindow.Actions.EXIT_ACTION);
		miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		miExit.addScriptedActionListener(frame.getDefaultActionListener());

		ScriptedActionMenuItem miShutdownGUI = new ScriptedActionMenuItem(MainWindow.Actions.SHUTDOWN_GUI_ACTION);
		miShutdownGUI.addScriptedActionListener(frame.getDefaultActionListener());

		ScriptedActionMenuItem miOpenModel = new ScriptedActionMenuItem(MainWindow.Actions.OPEN_WORK_ACTION);
		miOpenModel.setMnemonic(KeyEvent.VK_O);
		miOpenModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		miOpenModel.addScriptedActionListener(frame.getDefaultActionListener());

/*		JMenuItem miSaveWorkspace = new JMenuItem();
		miSaveWorkspace.setText("Save workspace");
		miSaveWorkspace.setMnemonic(KeyEvent.VK_O);
		miSaveWorkspace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		miSaveWorkspace.addActionListener(frame.getDefaultActionListener());
		miSaveWorkspace.setActionCommand("gui.saveWorkspace()");*/

		mnFile.add(miNewModel);
		mnFile.add(miOpenModel);

		mnFile.addSeparator();
		mnFile.add(miShutdownGUI);
		mnFile.add(miExit);


		// View
		mnView = new JMenu();
		mnView.setText ("View");

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

		JMenu mnWindows = new JMenu();
		mnWindows.setText("Windows");

		JMenuItem miShowPropertyEditor = new JCheckBoxMenuItem();
		miShowPropertyEditor.setText("Property editor");
		//miSaveWorkspace.setActionCommand("gui.togglePropertyEditorVisible()");

		mnWindows.add(miShowPropertyEditor);

		mnView.add(mnWindows);
		mnView.addSeparator();
		mnView.add(mnLAF);

		// Edit
		mnEdit = new JMenu();
		mnEdit.setText("Edit");

		// Tools
		mnTools = new JMenu();
		mnTools.setText("Tools");

		// Settings
		mnSettings = new JMenu();
		mnSettings.setText("Settings");

		ScriptedActionMenuItem miReconfigure = new ScriptedActionMenuItem(MainWindow.Actions.RECONFIGURE_PLUGINS_ACTION);
		miReconfigure.addScriptedActionListener(frame.getDefaultActionListener());

		mnSettings.add(miReconfigure);

		// Help
		mnHelp = new JMenu();
		mnHelp.setText("Help");

		add(mnFile);
		add(mnEdit);
		add(mnView);
		add(mnTools);
		add(mnSettings);
		add(mnHelp);
	}
}
