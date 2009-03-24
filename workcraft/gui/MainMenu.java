package org.workcraft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.Exporter;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.OperationCancelledException;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionCheckBoxMenuItem;
import org.workcraft.gui.actions.ScriptedActionMenuItem;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.layout.Layout;
import org.workcraft.plugins.modelchecking.ModelChecker;

@SuppressWarnings("serial")
public class MainMenu extends JMenuBar {
	class LayoutAction extends ScriptedAction {
		String layoutClassName;
		String layoutText;

		public LayoutAction(PluginInfo layoutPlugin) {
			layoutClassName = layoutPlugin.getClassName();
			layoutText = layoutPlugin.getDisplayName();
		}

		public String getScript() {
			return "mainWindow.doLayout(\""+layoutClassName+"\")";

		}
		public String getText() {
			return layoutText;
		}
	}
	class ToggleWindowAction extends ScriptedAction {
		private int windowID;
		private String windowTitle;

		public ToggleWindowAction(DockableWindow window) {
			windowID = window.getID();
			windowTitle = window.getTitle();
		}
		public String getScript() {
			return "mainWindow.toggleDockableWindow("+windowID+");";
		}
		public String getText() {
			return windowTitle;
		}
	}
	class ExportAction extends ScriptedAction {
		private String exporterClassName;
		private String displayName;

		public ExportAction(Exporter exporter) {
			exporterClassName = exporter.getClass().getName();
			displayName = exporter.getDescription();
		}

		public String getScript() {
			return "mainWindow.exportTo(\""+exporterClassName+"\");";
		}

		public String getText() {
			return displayName;
		}

	}

	class ModelCheckAction extends ScriptedAction {
		private String modelCheckerClassName;
		private String displayName;

		public ModelCheckAction(ModelChecker checker) {
			modelCheckerClassName = checker.getClass().getName();
			displayName = checker.getDisplayName();
		}

		public String getScript() {
			return "mainWindow.runModelChecker(\""+modelCheckerClassName+"\");";
		}

		public String getText() {
			return displayName;
		}
	}

	private JMenu mnFile, mnEdit, mnView, mnTools = null, mnModelChecking = null, mnSettings, mnHelp, mnWindows;
	private JMenu mnExport;
	private JMenu mnLayout = null;

	private MainWindow mainWindow;
	private HashMap <Integer, ScriptedActionCheckBoxMenuItem> windowItems = new HashMap<Integer, ScriptedActionCheckBoxMenuItem>();

	private String[] lafCaptions = new String[] {
			"Java default",
			"Windows",
			"Substance: Moderate",
			"Substance: Mist Silver",
			"Substance: Raven",
			"Substance: Business",
			"Substance: Creme"
	};
	private String[] lafClasses = new String[] {
			"javax.swing.plaf.metal.MetalLookAndFeel",
			"com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
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

		ScriptedActionMenuItem miNewWorkspace = new ScriptedActionMenuItem(WorkspaceWindow.Actions.NEW_WORKSPACE_AS_ACTION);
		miNewWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miOpenWorkspace = new ScriptedActionMenuItem(WorkspaceWindow.Actions.OPEN_WORKSPACE_ACTION);
		miOpenWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miAddFiles = new ScriptedActionMenuItem(WorkspaceWindow.Actions.ADD_FILES_TO_WORKSPACE_ACTION);
		miAddFiles.addScriptedActionListener(mainWindow.getDefaultActionListener());


		ScriptedActionMenuItem miSaveWorkspace = new ScriptedActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_ACTION);
		miSaveWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miSaveWorkspaceAs = new ScriptedActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_AS_ACTION);
		miSaveWorkspaceAs.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miImport = new ScriptedActionMenuItem(MainWindow.Actions.IMPORT_ACTION);
		miImport.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnExport = new JMenu("Export");
		mnExport.setEnabled(false);

		mnFile.add(miNewModel);
		mnFile.add(miOpenModel);
		mnFile.add(miSaveWork);
		mnFile.add(miSaveWorkAs);
		mnFile.addSeparator();
		mnFile.add(miImport);
		mnFile.add(mnExport);

		mnFile.addSeparator();
		mnFile.add(miNewWorkspace);
		mnFile.add(miOpenWorkspace);
		mnFile.add(miAddFiles);
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
					try {
						mainWindow.setLAF(lafClass);
					} catch (OperationCancelledException e1) {
					}
				}
			});
			mnLAF.add(miLAFItem);
		}

		mnWindows = new JMenu();
		mnWindows.setText("Windows");

		/*	ScriptedActionMenuItem miSaveLayout = new ScriptedActionMenuItem(MainWindow.Actions.SAVE_UI_LAYOUT);
		miSaveLayout.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnView.add(miSaveLayout);

		ScriptedActionMenuItem miLoadLayout = new ScriptedActionMenuItem(MainWindow.Actions.LOAD_UI_LAYOUT);
		miLoadLayout.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnView.add(miLoadLayout);*/

		mnView.add(mnWindows);
		mnView.add(mnLAF);

		// Edit
		mnEdit = new JMenu();
		mnEdit.setText("Edit");

		// Settings
		mnSettings = new JMenu();
		mnSettings.setText("Utility");

		ScriptedActionMenuItem miCustomButtons = new ScriptedActionMenuItem(MainWindow.Actions.EDIT_CUSTOM_BUTTONS_ACTION);
		miCustomButtons.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miReconfigure = new ScriptedActionMenuItem(MainWindow.Actions.RECONFIGURE_PLUGINS_ACTION);
		miReconfigure.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ScriptedActionMenuItem miProperties = new ScriptedActionMenuItem(MainWindow.Actions.EDIT_SETTINGS_ACTION);
		miProperties.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnSettings.add(miProperties);
		mnSettings.add(miCustomButtons);
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

	private void addLayout (PluginInfo layoutPlugin) {
		if (mnLayout == null)
			mnLayout = new JMenu("Layout");

		ScriptedActionMenuItem miLayoutMenuItem = new ScriptedActionMenuItem(new LayoutAction(layoutPlugin));
		miLayoutMenuItem.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnLayout.add(miLayoutMenuItem);
	}

	private void addModelChecker (ModelChecker checker) {
		if (mnModelChecking == null)
			mnModelChecking = new JMenu("Model checking");

		ScriptedActionMenuItem miModelCheckMenuItem = new ScriptedActionMenuItem(new ModelCheckAction(checker));
		miModelCheckMenuItem.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnModelChecking.add(miModelCheckMenuItem);
	}

	private void addExporter (Exporter exporter) {
		ScriptedActionMenuItem miExport = new ScriptedActionMenuItem(new ExportAction(exporter));
		miExport.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnExport.add(miExport);
		mnExport.setEnabled(true);
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
					addLayout(info);
			}
		} catch (PluginInstantiationException e) {
			System.err.println ("Could not instantiate layout plugin class: " + e.getMessage() + " (skipped)");
		}

		if (mnLayout != null)
			mnTools.add(mnLayout);


		PluginInfo[] modelCheckerPluginInfo = framework.getPluginManager().getPluginsByInterface(ModelChecker.class.getName());

		try {
			for (PluginInfo info : modelCheckerPluginInfo) {
				ModelChecker modelChecker = (ModelChecker)framework.getPluginManager().getSingleton(info);

				if (modelChecker.isApplicableTo(model))
					addModelChecker(modelChecker);
			}
		} catch (PluginInstantiationException e) {
			System.err.println ("Could not instantiate layout plugin class: " + e.getMessage() + " (skipped)");
		}

		if (mnModelChecking != null)
			mnTools.add(mnModelChecking);


		if (mnTools.getMenuComponentCount() > 0)
			add(mnTools, getComponentIndex(mnSettings));
		else
			mnTools = null;

		mnExport.removeAll();
		mnExport.setEnabled(false);

		PluginInfo[] exportPluginInfo = framework.getPluginManager().getPluginsByInterface(Exporter.class.getName());

		try {
			for (PluginInfo info : exportPluginInfo) {
				Exporter exporter = (Exporter)framework.getPluginManager().getSingleton(info);

				if (exporter.isApplicableTo(model))
					addExporter(exporter);
			}
		}  catch (PluginInstantiationException e) {
			System.err.println ("Could not instantiate export plugin class: " + e.getMessage() + " (skipped)");
		}


		doLayout();
	}

	final public void registerUtilityWindow(DockableWindow window) {
		ScriptedActionCheckBoxMenuItem miWindowItem = new ScriptedActionCheckBoxMenuItem(new ToggleWindowAction(window));
		miWindowItem.addScriptedActionListener(mainWindow.getDefaultActionListener());
		miWindowItem.setSelected(!window.isClosed());
		windowItems.put (window.getID(), miWindowItem);
		mnWindows.add(miWindowItem);
	}

	final public void utilityWindowClosed (int ID) {
		ScriptedActionCheckBoxMenuItem mi = windowItems.get(ID);
		if (mi!=null)
			mi.setSelected(false);
	}

	final public void utilityWindowDisplayed (int ID) {
		ScriptedActionCheckBoxMenuItem mi = windowItems.get(ID);
		if (mi!=null)
			mi.setSelected(true);
	}
}

