/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionCheckBoxMenuItem;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.util.ListMap;
import org.workcraft.util.Pair;
import org.workcraft.util.Tools;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class MainMenu extends JMenuBar {
	class ToolAction extends Action {
		Tool tool;
		String text;

		public ToolAction(Pair<String, Tool> tool) {
			this.tool = tool.getSecond();
			this.text = tool.getFirst();
		}

		public String getText() {
			return text;
		}

		@Override
		public void run() {
			final Framework framework = Framework.getInstance();
			framework.getMainWindow().runTool(tool);
		}
	}

	class ToggleWindowAction extends Action {
		private DockableWindow window;
		private String windowTitle;

		public ToggleWindowAction(DockableWindow window) {
			this.window = window;
			windowTitle = window.getTitle();
		}
		@Override
		public String getText() {
			return windowTitle;
		}
		@Override
		public void run() {
			final Framework framework = Framework.getInstance();
			framework.getMainWindow().toggleDockableWindow(window);
		}
	}

	class ExportAction extends Action {
		private final Exporter exporter;

		public ExportAction(Exporter exporter) {
			this.exporter = exporter;
		}

		@Override
		public void run() {
			try {
				final Framework framework = Framework.getInstance();
				framework.getMainWindow().export(exporter);
			} catch (OperationCancelledException e) {
			}
		}

		public String getText() {
			return exporter.getDescription();
		}
	}

	final private MainWindow mainWindow;
	final private JMenu mnExport = new JMenu("Export");
	final private JMenu mnRecent = new JMenu("Open recent");
	final private JMenu mnWindows = new JMenu("Windows");
	final private JMenu mnTools = new JMenu("Tools");
	final private HashMap <Integer, ActionCheckBoxMenuItem> windowItems = new HashMap<Integer, ActionCheckBoxMenuItem>();

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
		addFileMenu(mainWindow);
		addEditMenu(mainWindow);
		addViewMenu(mainWindow);
		add(mnTools);
		addHelpMenu(mainWindow);
	}

	private void addFileMenu(final MainWindow mainWindow) {
		JMenu mnFile = new JMenu("File");

		ActionMenuItem miNewModel = new ActionMenuItem(MainWindowActions.CREATE_WORK_ACTION);
		miNewModel.setMnemonic(KeyEvent.VK_N);
		miNewModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		miNewModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miOpenModel = new ActionMenuItem(MainWindowActions.OPEN_WORK_ACTION);
		miOpenModel.setMnemonic(KeyEvent.VK_O);
		miOpenModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		miOpenModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miMergeModel = new ActionMenuItem(MainWindowActions.MERGE_WORK_ACTION);
		miMergeModel.setMnemonic(KeyEvent.VK_M);
		miMergeModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miSaveWork = new ActionMenuItem(MainWindowActions.SAVE_WORK_ACTION);
		miSaveWork.setMnemonic(KeyEvent.VK_S);
		miSaveWork.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		miSaveWork.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miSaveWorkAs = new ActionMenuItem(MainWindowActions.SAVE_WORK_AS_ACTION);
		miSaveWorkAs.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miCloseAll = new ActionMenuItem(MainWindowActions.CLOSE_ALL_EDITORS_ACTION);
		miCloseAll.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miCloseActive = new ActionMenuItem(MainWindowActions.CLOSE_ACTIVE_EDITOR_ACTION);
		miCloseActive.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miImport = new ActionMenuItem(MainWindowActions.IMPORT_ACTION);
		miImport.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miNewWorkspace = new ActionMenuItem(WorkspaceWindow.Actions.NEW_WORKSPACE_AS_ACTION);
		miNewWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miOpenWorkspace = new ActionMenuItem(WorkspaceWindow.Actions.OPEN_WORKSPACE_ACTION);
		miOpenWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miAddFiles = new ActionMenuItem(WorkspaceWindow.Actions.ADD_FILES_TO_WORKSPACE_ACTION);
		miAddFiles.addScriptedActionListener(mainWindow.getDefaultActionListener());


		ActionMenuItem miSaveWorkspace = new ActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_ACTION);
		miSaveWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miSaveWorkspaceAs = new ActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_AS_ACTION);
		miSaveWorkspaceAs.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miReconfigure = new ActionMenuItem(MainWindowActions.RECONFIGURE_PLUGINS_ACTION);
		miReconfigure.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miShutdownGUI = new ActionMenuItem(MainWindowActions.SHUTDOWN_GUI_ACTION);
		miShutdownGUI.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miExit = new ActionMenuItem(MainWindowActions.EXIT_ACTION);
		miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		miExit.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnFile.add(miNewModel);
		mnFile.add(miOpenModel);
		mnFile.add(mnRecent);
		mnFile.add(miMergeModel);

		mnFile.addSeparator();
		mnFile.add(miSaveWork);
		mnFile.add(miSaveWorkAs);
		mnFile.add(miCloseActive);
		mnFile.add(miCloseAll);

		mnFile.addSeparator();
		mnFile.add(miImport);
		mnFile.add(mnExport);

		// FIXME: Workspace functionality is not working yet.
//		mnFile.addSeparator();
//		mnFile.add(miNewWorkspace);
//		mnFile.add(miOpenWorkspace);
//		mnFile.add(miAddFiles);
//		mnFile.add(miSaveWorkspace);
//		mnFile.add(miSaveWorkspaceAs);

		mnFile.addSeparator();
		mnFile.add(miReconfigure);
		mnFile.add(miShutdownGUI);
		mnFile.addSeparator();
		mnFile.add(miExit);

		add(mnFile);
	}

	private void addExportSeparator(String text) {
		mnExport.add(new JLabel(text));
		mnExport.addSeparator();
	}

	private void addExporter(Exporter exporter) {
		String text = exporter.getDescription();
		ActionMenuItem miExport = new ActionMenuItem(new ExportAction(exporter), text);

		miExport.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnExport.add(miExport);
		mnExport.setEnabled(true);
	}

	private void addEditMenu(final MainWindow mainWindow) {
		JMenu mnEdit = new JMenu("Edit");

		ActionMenuItem miUndo = new ActionMenuItem(MainWindowActions.EDIT_UNDO_ACTION);
		miUndo.setMnemonic(KeyEvent.VK_U);
		miUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		miUndo.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miRedo = new ActionMenuItem(MainWindowActions.EDIT_REDO_ACTION);
		miRedo.setMnemonic(KeyEvent.VK_R);
		miRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		miRedo.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miCut = new ActionMenuItem(MainWindowActions.EDIT_CUT_ACTION);
		miCut.setMnemonic(KeyEvent.VK_T);
		miCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		miCut.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miCopy = new ActionMenuItem(MainWindowActions.EDIT_COPY_ACTION);
		miCopy.setMnemonic(KeyEvent.VK_C);
		miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		miCopy.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miPaste = new ActionMenuItem(MainWindowActions.EDIT_PASTE_ACTION);
		miPaste.setMnemonic(KeyEvent.VK_P);
		miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		miPaste.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miDelete = new ActionMenuItem(MainWindowActions.EDIT_DELETE_ACTION);
		miDelete.setMnemonic(KeyEvent.VK_D);
		miDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		miDelete.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miSelectAll = new ActionMenuItem(MainWindowActions.EDIT_SELECT_ALL_ACTION);
		miSelectAll.setMnemonic(KeyEvent.VK_A);
		miSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		miSelectAll.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miSelectInverse = new ActionMenuItem(MainWindowActions.EDIT_SELECT_INVERSE_ACTION);
		miSelectInverse.setMnemonic(KeyEvent.VK_V);
		miSelectInverse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		miSelectInverse.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miSelectNone = new ActionMenuItem(MainWindowActions.EDIT_SELECT_NONE_ACTION);
		miSelectNone.setMnemonic(KeyEvent.VK_E);
		miSelectNone.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miProperties = new ActionMenuItem(MainWindowActions.EDIT_SETTINGS_ACTION);
		miProperties.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnEdit.add(miUndo);
		mnEdit.add(miRedo);
		mnEdit.addSeparator();
		mnEdit.add(miCut);
		mnEdit.add(miCopy);
		mnEdit.add(miPaste);
		mnEdit.add(miDelete);
		mnEdit.addSeparator();
		mnEdit.add(miSelectAll);
		mnEdit.add(miSelectInverse);
		mnEdit.add(miSelectNone);
		mnEdit.addSeparator();
		mnEdit.add(miProperties);

		add(mnEdit);
	}

	private void addViewMenu(final MainWindow mainWindow) {
		JMenu mnView = new JMenu("View");

		ActionMenuItem miZoomIn = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_IN);
		miZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
		miZoomIn.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miZoomOut = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_OUT);
		miZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
		miZoomOut.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miZoomDefault = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_DEFAULT);
		miZoomDefault.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, ActionEvent.CTRL_MASK));
		miZoomDefault.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miZoomFit = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_FIT);
		miZoomFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		miZoomFit.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miPanLeft = new ActionMenuItem(MainWindowActions.VIEW_PAN_LEFT);
		miPanLeft.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.CTRL_MASK));
		miPanLeft.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miPanUp = new ActionMenuItem(MainWindowActions.VIEW_PAN_UP);
		miPanUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.CTRL_MASK));
		miPanUp.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miPanRight = new ActionMenuItem(MainWindowActions.VIEW_PAN_RIGHT);
		miPanRight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.CTRL_MASK));
		miPanRight.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miPanDown = new ActionMenuItem(MainWindowActions.VIEW_PAN_DOWN);
		miPanDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.CTRL_MASK));
		miPanDown.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miPanCenter = new ActionMenuItem(MainWindowActions.VIEW_PAN_CENTER);
		miPanCenter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		miPanCenter.addScriptedActionListener(mainWindow.getDefaultActionListener());

		JMenu mnLAF = new JMenu("Look and Feel");
		for (int i = 0; i < lafClasses.length; i++) {
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

		/*	ScriptedActionMenuItem miSaveLayout = new ScriptedActionMenuItem(MainWindow.Actions.SAVE_UI_LAYOUT);
		miSaveLayout.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnView.add(miSaveLayout);

		ScriptedActionMenuItem miLoadLayout = new ScriptedActionMenuItem(MainWindow.Actions.LOAD_UI_LAYOUT);
		miLoadLayout.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnView.add(miLoadLayout);*/

		ActionMenuItem miResetLayout = new ActionMenuItem(MainWindowActions.RESET_GUI_ACTION);
		miResetLayout.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnView.add(miZoomIn);
		mnView.add(miZoomOut);
		mnView.add(miZoomDefault);
		mnView.add(miZoomFit);
		mnView.addSeparator();
		mnView.add(miPanCenter);
		mnView.add(miPanLeft);
		mnView.add(miPanUp);
		mnView.add(miPanRight);
		mnView.add(miPanDown);
		mnView.addSeparator();
		mnView.add(mnWindows);
		mnView.add(mnLAF);
		mnView.add(miResetLayout);


		add(mnView);
	}

	private void addHelpMenu(final MainWindow mainWindow) {
		JMenu mnHelp = new JMenu();
		mnHelp.setText("Help");

		ActionMenuItem miOverview = new ActionMenuItem(MainWindowActions.HELP_OVERVIEW_ACTION);
		miOverview.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miContents = new ActionMenuItem(MainWindowActions.HELP_CONTENTS_ACTION);
		miContents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		miContents.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miTutorials = new ActionMenuItem(MainWindowActions.HELP_TUTORIALS_ACTION);
		miTutorials.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miBugreport = new ActionMenuItem(MainWindowActions.HELP_BUGREPORT_ACTION);
		miBugreport.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miQuestion = new ActionMenuItem(MainWindowActions.HELP_QUESTION_ACTION);
		miQuestion.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miAbout = new ActionMenuItem(MainWindowActions.HELP_ABOUT_ACTION);
		miAbout.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnHelp.add(miOverview);
		mnHelp.add(miContents);
		mnHelp.add(miTutorials);
		mnHelp.addSeparator();
		mnHelp.add(miBugreport);
		mnHelp.add(miQuestion);
		mnHelp.addSeparator();
		mnHelp.add(miAbout);

		add(mnHelp);
	}

	final public void setMenuForWorkspaceEntry(final WorkspaceEntry we) {
		we.updateActionState();
		setToolsMenu(we);
		setExportMenu(we);
	}

	private void setExportMenu(final WorkspaceEntry we) {
		mnExport.removeAll();
		mnExport.setEnabled(false);

		VisualModel model = we.getModelEntry().getVisualModel();
		final Framework framework = Framework.getInstance();
		PluginManager pluginManager = framework.getPluginManager();
		Collection<PluginInfo<? extends Exporter>> plugins = pluginManager.getPlugins(Exporter.class);

		boolean haveVisual = false;
		for (PluginInfo<? extends Exporter> info : plugins) {
			Exporter exporter = info.getSingleton();
			if (exporter.getCompatibility(model) > Exporter.NOT_COMPATIBLE) {
				if (!haveVisual) {
					addExportSeparator("Visual");
				}
				addExporter(exporter);
				haveVisual = true;
			}
		}

		boolean haveNonVisual = false;
		for (PluginInfo<? extends Exporter> info : plugins) {
			Exporter exporter = info.getSingleton();
			if (exporter.getCompatibility(model.getMathModel()) > Exporter.NOT_COMPATIBLE) {
				if (!haveNonVisual) {
					addExportSeparator("Non-visual");
				}
				addExporter(exporter);
				haveNonVisual = true;
			}
		}
	}

	private void setToolsMenu(final WorkspaceEntry we) {
		mnTools.setVisible(true);
		mnTools.removeAll();

		ListMap<String, Pair<String, Tool>> tools = Tools.getTools(we);
		List<String> sections = Tools.getSections(tools);

		for (String section : sections) {
			JMenu target = mnTools;
			if (!section.isEmpty()) {
				JMenu sectionMenu = new JMenu (section);
				mnTools.add(sectionMenu);
				target = sectionMenu;
			}
			for (Pair<String, Tool> tool : Tools.getSectionTools(section, tools)) {
				ActionMenuItem miTool = new ActionMenuItem(new ToolAction(tool));
				miTool.addScriptedActionListener(mainWindow.getDefaultActionListener());
				target.add(miTool);
			}
		}
	}

	final public void registerUtilityWindow(DockableWindow window) {
		ActionCheckBoxMenuItem miWindowItem = new ActionCheckBoxMenuItem(new ToggleWindowAction(window));
		miWindowItem.addScriptedActionListener(mainWindow.getDefaultActionListener());
		miWindowItem.setSelected(!window.isClosed());
		windowItems.put (window.getID(), miWindowItem);
		mnWindows.add(miWindowItem);
	}

	final public void setRecentMenu(ArrayList<String> entries) {
		mnRecent.removeAll();
		mnRecent.setEnabled(false);
		int index = 0;
		Collections.reverse(entries);
		for (final String entry: entries) {
			if (entry != null) {
				JMenuItem miFile = new JMenuItem();
				if (index > 9) {
					miFile.setText(entry);
				} else {
					miFile.setText(index + ". " + entry);
					miFile.setMnemonic(index + '0');
					index++;
				}
				miFile.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mainWindow.openWork(new File(entry));
					}
				});
				mnRecent.add(miFile);
				mnRecent.setEnabled(true);
			}
		}
		mnRecent.addSeparator();
		JMenuItem miClear = new JMenuItem("Clear the list");
		miClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainWindow.clearRecentFiles();
			}
		});
		mnRecent.add(miClear);
	}

	final public void utilityWindowClosed (int ID) {
		ActionCheckBoxMenuItem mi = windowItems.get(ID);
		if (mi!=null)
			mi.setSelected(false);
	}

	final public void utilityWindowDisplayed (int ID) {
		ActionCheckBoxMenuItem mi = windowItems.get(ID);
		if (mi!=null)
			mi.setSelected(true);
	}

	public void reset() {
		mnTools.setVisible(false);
		mnTools.removeAll();
	}

	public JMenu getRecentMenu() {
		return mnRecent;
	}

	public JMenu getExportMenu() {
		return mnExport;
	}

	public JMenu getToolsMenu() {
		return mnTools;
	}

}
