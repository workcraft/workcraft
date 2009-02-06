package org.workcraft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionMenuItem;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.layout.Layout;

@SuppressWarnings("serial")
public class MainMenu extends JMenuBar {
	class LayoutAction extends ScriptedAction {
		String layoutClassName;
		String layoutText;

		public LayoutAction(Layout layout) {
			layoutClassName = layout.getClass().getName();
			layoutText = layout.getDisplayName();
		}

		public String getScript() {
			return "layout = framework.getPluginManager().getSingletonByName(\""+layoutClassName+"\");\n" +
					"layout.doLayout(visualModel);";
		}
		public String getText() {
			return layoutText;
		}
	}

	JMenu mnFile, mnEdit, mnView, mnTools = null, mnSettings, mnHelp;
	JMenu mnLayout = null;
	JMenuItem miShowPropertyEditor;

	MainWindow mainWindow;

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

	MainMenu(final MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		// File
		mnFile = new JMenu();
		mnFile.setText("File");

		ScriptedActionMenuItem miNewModel = new ScriptedActionMenuItem(MainWindow.Actions.CREATE_WORK_ACTION);
		miNewModel.setMnemonic(KeyEvent.VK_N);
		miNewModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		miNewModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miExit = new ScriptedActionMenuItem(MainWindow.Actions.EXIT_ACTION);
		miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		miExit.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miShutdownGUI = new ScriptedActionMenuItem(MainWindow.Actions.SHUTDOWN_GUI_ACTION);
		miShutdownGUI.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miOpenModel = new ScriptedActionMenuItem(MainWindow.Actions.OPEN_WORK_ACTION);
		miOpenModel.setMnemonic(KeyEvent.VK_O);
		miOpenModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		miOpenModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miSaveWork = new ScriptedActionMenuItem(MainWindow.Actions.SAVE_WORK_ACTION);
		miSaveWork.setMnemonic(KeyEvent.VK_S);
		miSaveWork.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		miSaveWork.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miSaveWorkAs = new ScriptedActionMenuItem(MainWindow.Actions.SAVE_WORK_AS_ACTION);
		miSaveWorkAs.addScriptedActionListener(mainWindow.getDefaultActionListener());


		ScriptedActionMenuItem miSaveWorkspace = new ScriptedActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_ACTION);
		miSaveWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miSaveWorkspaceAs = new ScriptedActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_AS_ACTION);
		miSaveWorkspaceAs.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnFile.add(miNewModel);
		mnFile.add(miOpenModel);
		mnFile.add(miSaveWork);
		mnFile.add(miSaveWorkAs);


		mnFile.addSeparator();
		mnFile.add(miSaveWorkspace);
		mnFile.add(miSaveWorkspaceAs);
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
					mainWindow.setLAF(lafClass);
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


		// Settings
		mnSettings = new JMenu();
		mnSettings.setText("Settings");

		ScriptedActionMenuItem miReconfigure = new ScriptedActionMenuItem(MainWindow.Actions.RECONFIGURE_PLUGINS_ACTION);
		miReconfigure.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnSettings.add(miReconfigure);

		// Help
		mnHelp = new JMenu();
		mnHelp.setText("Help");

		add(mnFile);
		add(mnEdit);
		add(mnView);
		add(mnSettings);
		add(mnHelp);
	}

	private void addLayout (Layout layout) {
		if (mnLayout == null)
			mnLayout = new JMenu("Layout");

		ScriptedActionMenuItem miLayoutMenuItem = new ScriptedActionMenuItem(new LayoutAction(layout));
		miLayoutMenuItem.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnLayout.add(miLayoutMenuItem);
	}

	final public void setMenuForModel(VisualModel model) {
		if (mnTools != null)
			remove(mnTools);

		mnTools = null;
		mnLayout = null;

		mnTools = new JMenu();
		mnTools.setText("Tools");

		Framework framework = mainWindow.getFramework();

		PluginInfo[] layoutPluginInfo = framework.getPluginManager().getPluginsByInterface(Layout.class.getName());

		try {
			for (PluginInfo info : layoutPluginInfo) {
				Layout layout = (Layout)framework.getPluginManager().getSingleton(info);

				if (layout.isApplicableTo(model))
					addLayout(layout);
			}
		} catch (PluginInstantiationException e) {
			System.err.println ("Could not instantiate layout plugin class: " + e.getMessage() + " (skipped)");
		}

		if (mnLayout != null)
			mnTools.add(mnLayout);


		if (mnTools.getMenuComponentCount() > 0)
			add(mnTools, getComponentIndex(mnSettings));
		else
			mnTools = null;

		doLayout();
	}
}

