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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.defaults.StandardBorderManager;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.drag.preview.AlphaPreview;
import org.flexdock.docking.state.PersistenceException;
import org.flexdock.perspective.Perspective;
import org.flexdock.perspective.PerspectiveManager;
import org.flexdock.perspective.persist.PerspectiveModel;
import org.flexdock.perspective.persist.xml.XMLPersister;
import org.flexdock.plaf.common.border.ShadowBorder;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceConstants.TabContentPaneBorderKind;
import org.workcraft.Framework;
import org.workcraft.ModelFactory;
import org.workcraft.PluginInfo;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.LayoutFailedException;
import org.workcraft.exceptions.ModelCheckingFailedException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditorDialog;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.layout.Layout;
import org.workcraft.plugins.modelchecking.ModelChecker;
import org.workcraft.util.Export;
import org.workcraft.util.Import;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
	public static class Actions {
		public static final ScriptedAction CREATE_WORK_ACTION = new ScriptedAction() {
			public String getScript() {
				return tryOperation("mainWindow.createWork();");
			}
			public String getText() {
				return "Create work...";
			};
		};
		public static final ScriptedAction OPEN_WORK_ACTION = new ScriptedAction() {
			public String getScript() {
				return tryOperation("mainWindow.openWork();");
			}
			public String getText() {
				return "Open work...";
			};
		};
		public static final ScriptedAction SAVE_WORK_ACTION = new ScriptedAction() {
			public String getScript() {
				return tryOperation("mainWindow.saveWork();");
			}
			public String getText() {
				return "Save work";
			};
		};
		public static final ScriptedAction SAVE_WORK_AS_ACTION = new ScriptedAction() {
			public String getScript() {
				return tryOperation("mainWindow.saveWorkAs();");
			}
			public String getText() {
				return "Save work as...";
			};
		};
		public static final ScriptedAction EXIT_ACTION = new ScriptedAction() {
			public String getScript() {
				return tryOperation("framework.shutdown();");
			}
			public String getText() {
				return "Exit";
			};
		};
		public static final ScriptedAction SHUTDOWN_GUI_ACTION = new ScriptedAction() {
			public String getScript() {
				return tryOperation("framework.shutdownGUI();");
			}
			public String getText() {
				return "Switch to console mode";
			};
		};
		public static final ScriptedAction RECONFIGURE_PLUGINS_ACTION = new ScriptedAction() {
			public String getScript() {
				return "framework.getPluginManager().reconfigure();";
			}
			public String getText() {
				return "Reconfigure plugins";
			};
		};
		public static final ScriptedAction RESET_LAYOUT_ACTION = new ScriptedAction() {
			public String getScript() {
				return tryOperation ("mainWindow.resetDockingLayout();");
			}
			public String getText() {
				return "Reset GUI layout";
			};
		};

		public static final ScriptedAction IMPORT_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.importFrom()";
			}
			public String getText() {
				return "Import...";
			}
		};

		public static final ScriptedAction EDIT_CUSTOM_BUTTONS_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.editCustomButtons()";
			}
			public String getText() {
				return "Edit custom buttons...";
			}
		};

		public static final ScriptedAction EDIT_SETTINGS_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.editSettings()";
			}
			public String getText() {
				return "Settings...";
			}
		};
	}

	private final ScriptedActionListener defaultActionListener = new ScriptedActionListener() {
		public void actionPerformed(ScriptedAction e) {
			/*if (e.getScript() == null)
				System.out.println ("Scripted action \"" + e.getText()+"\": null action");
			else
				System.out.println ("Scripted action \"" + e.getText()+"\":\n"+e.getScript());

			if (e.getUndoScript() == null)
				System.out.println ("Action cannot be undone.");
			else {
				System.out.println ("Undo script:\n" +e.getUndoScript());
				if (e.getRedoScript() == null)
					System.out.println ("Action cannot be redone.");
				else
					System.out.println ("Redo script:\n"+e.getRedoScript());
			}*/

			if (e.getScript() != null)
				framework.execJavaScript(e.getScript());
		}
	};

	private Framework framework;

	private WorkspaceWindow workspaceWindow;
	private OutputWindow outputWindow;
	private ErrorWindow errorWindow;
	private JavaScriptWindow jsWindow;
	private PropertyEditorWindow propertyEditorWindow;
	private ToolboxWindow toolboxWindow;

	private JPanel content;

	private DefaultDockingPort rootDockingPort;
	private DockableWindow outputDockable;
	private DockableWindow documentPlaceholder;

	private LinkedHashSet<DockableWindow> editorWindows = new LinkedHashSet<DockableWindow>();
	private LinkedList<DockableWindow> utilityWindows = new LinkedList<DockableWindow>();

	private GraphEditorPanel editorInFocus;

	private MainMenu mainMenu;

	private String lastSavePath = null;
	private String lastOpenPath = null;

	private int dockableIDCounter = 0;
	private HashMap<Integer, DockableWindow> IDToDockableWindowMap = new HashMap<Integer, DockableWindow>();

	protected void createWindows() {
		workspaceWindow  = new WorkspaceWindow(this);
		framework.getWorkspace().addListener(workspaceWindow);
		workspaceWindow.setVisible(true);
		propertyEditorWindow = new PropertyEditorWindow(framework);

		outputWindow = new OutputWindow(framework);
		errorWindow = new ErrorWindow(framework);
		jsWindow = new JavaScriptWindow(framework);

		toolboxWindow = new ToolboxWindow(framework);

		outputDockable = null;
		editorInFocus = null;
	}

	public MainWindow(final Framework framework) {
		super();
		this.framework = framework;

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				framework.shutdown();
			}
		});

	}

	public void setLAF(String laf) throws OperationCancelledException {
		if (JOptionPane.showConfirmDialog(this, "Changing Look and Feel requires GUI restart.\n\n" +
				"This will cause the visual editor windows to be closed.\n\nProceed?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		if (laf == null)
			laf = UIManager.getSystemLookAndFeelClassName();

		framework.setConfigVar("gui.lookandfeel", laf);
		framework.restartGUI();
	}

	public WorkspaceWindow getWorkspaceView() {
		return workspaceWindow;
	}

	private int getNextDockableID() {
		return dockableIDCounter ++;
	}

	private DockableWindow createDockableWindow(JComponent component, String name, Dockable neighbour, int options) {
		return createDockableWindow(component, name, neighbour, options, DockingConstants.CENTER_REGION, name);
	}

	private DockableWindow createDockableWindow(JComponent component, String name, int options, String relativeRegion, float split) {
		return createDockableWindow(component, name, null, options, relativeRegion, split, name);
	}

	private DockableWindow createDockableWindow(JComponent component, String name, Dockable neighbour, int options, String relativeRegion, float split) {
		return createDockableWindow(component, name, neighbour, options, relativeRegion, split, name);
	}

	private DockableWindow createDockableWindow(JComponent component, String name, Dockable neighbour, int options, String relativeRegion, String persistentID) {
		int ID = getNextDockableID();

		DockableWindowContentPanel panel = new DockableWindowContentPanel(this, ID, name, component, options);
		DockableWindow dockable = new DockableWindow(panel, persistentID);
		DockingManager.registerDockable(dockable);

		IDToDockableWindowMap.put(ID, dockable);

		if (neighbour != null)
			DockingManager.dock(dockable, neighbour, relativeRegion);
		else
			DockingManager.dock(dockable, rootDockingPort, relativeRegion);

		return dockable;
	}


	private DockableWindow createDockableWindow(JComponent component, String name, Dockable neighbour, int options, String relativeRegion, float split, String persistentID) {
		DockableWindow dockable = createDockableWindow (component, name, neighbour, options, relativeRegion, persistentID);
		DockingManager.setSplitProportion(dockable, split);
		return dockable;
	}

	public void createEditorWindow(WorkspaceEntry we) {
		if (we.getModel() == null) {
			JOptionPane.showMessageDialog(this, "The selected entry is not a Workcraft model, and cannot be edited.", "Cannot open editor", JOptionPane.ERROR_MESSAGE);
			return;
		}

		VisualModel visualModel = (we.getModel() instanceof VisualModel) ? (VisualModel) we.getModel() : null;

		if (visualModel == null)
			try {
				visualModel = ModelFactory.createVisualModel(we.getModel());
				we.setModel(visualModel);
			} catch (VisualModelInstantiationException e) {
				JOptionPane.showMessageDialog(this, "A visual model could not be created for the selected model.\nPlease refer to the Problems window for details.\n", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}

			GraphEditorPanel editor = new GraphEditorPanel(this, we);
			String dockableTitle = we.getTitle() + " - " + visualModel.getDisplayName();

			DockableWindow editorWindow;

			if (editorWindows.isEmpty()) {
				editorWindow = createDockableWindow (editor, dockableTitle, documentPlaceholder,
						DockableWindowContentPanel.CLOSE_BUTTON | DockableWindowContentPanel.MAXIMIZE_BUTTON, DockingConstants.CENTER_REGION, "Document"+we.getEntryID());

				DockingManager.close(documentPlaceholder);
				DockingManager.unregisterDockable(documentPlaceholder);
				utilityWindows.remove(documentPlaceholder);
			}
			else {
				DockableWindow firstEditorWindow = editorWindows.iterator().next();
				editorWindow = createDockableWindow (editor, dockableTitle, firstEditorWindow,
						DockableWindowContentPanel.CLOSE_BUTTON | DockableWindowContentPanel.MAXIMIZE_BUTTON, DockingConstants.CENTER_REGION, "Document"+we.getEntryID());
			}

			editorWindows.add(editorWindow);
			requestFocus(editor);
	}

	private void registerUtilityWindow(DockableWindow dockableWindow) {
		if (!rootDockingPort.getDockables().contains(dockableWindow)) {
			dockableWindow.setClosed(true);
			DockingManager.close(dockableWindow);
		}
		mainMenu.registerUtilityWindow(dockableWindow);
		utilityWindows.add(dockableWindow);
	}

	public void startup() {
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
		//SwingUtilities.updateComponentTreeUI(MainWindow.this);

		String laf = framework.getConfigVar("gui.lookandfeel");
		if (laf == null)
			laf = UIManager.getCrossPlatformLookAndFeelClassName();
		LAF.setLAF(laf);
		SwingUtilities.updateComponentTreeUI(this);

		content = new JPanel(new BorderLayout(0,0));
		setContentPane(content);

		PerspectiveManager pm = (PerspectiveManager)DockingManager.getLayoutManager();
		pm.add(new Perspective("defaultWorkspace", "defaultWorkspace"));
		pm.setCurrentPerspective("defaultWorkspace", true);

		rootDockingPort = new DefaultDockingPort("defaultDockingPort");
		content.add(rootDockingPort, BorderLayout.CENTER);

		boolean maximised = Boolean.parseBoolean(framework.getConfigVar("gui.main.maximised"));
		String w = framework.getConfigVar("gui.main.width");
		String h = framework.getConfigVar("gui.main.height");
		int width = (w==null)?800:Integer.parseInt(w);
		int height = (h==null)?600:Integer.parseInt(h);

		lastSavePath = framework.getConfigVar("gui.main.lastSavePath");
		lastOpenPath = framework.getConfigVar("gui.main.lastOpenPath");

		this.setSize(width, height);

		if (maximised)
			setExtendedState(MAXIMIZED_BOTH);

		mainMenu = new MainMenu(this);
		setJMenuBar(mainMenu);

		setTitle("Workcraft " + Framework.FRAMEWORK_VERSION_MAJOR+"."+Framework.FRAMEWORK_VERSION_MINOR);

		createWindows();

		outputWindow.captureStream();
		errorWindow.captureStream();

		rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()));

		outputDockable = createDockableWindow (outputWindow, "Output", DockableWindowContentPanel.CLOSE_BUTTON, DockingManager.SOUTH_REGION, 0.8f);
		DockableWindow problems = createDockableWindow (errorWindow, "Problems", outputDockable, DockableWindowContentPanel.CLOSE_BUTTON);
		DockableWindow javaScript =  createDockableWindow (jsWindow, "Javascript", outputDockable, DockableWindowContentPanel.CLOSE_BUTTON);

		DockableWindow wsvd = createDockableWindow (workspaceWindow, "Workspace", DockableWindowContentPanel.CLOSE_BUTTON, DockingManager.EAST_REGION, 0.8f);
		DockableWindow propertyEditor = createDockableWindow (propertyEditorWindow, "Property editor", wsvd,  DockableWindowContentPanel.CLOSE_BUTTON, DockingManager.NORTH_REGION, 0.5f);
		DockableWindow toolbox = createDockableWindow (toolboxWindow, "Editor tools", wsvd, DockableWindowContentPanel.CLOSE_BUTTON, DockingManager.NORTH_REGION, 0.5f);

		documentPlaceholder = createDockableWindow(new DocumentPlaceholder(), "", outputDockable, 0, DockingManager.NORTH_REGION, 0.8f, "DocumentPlaceholder");

		DockingManager.display(outputDockable);
		EffectsManager.setPreview(new AlphaPreview(Color.BLACK, Color.GRAY, 0.5f));


		workspaceWindow.startup();

		setVisible(true);

		loadDockingLayout();
		DockableWindow.updateHeaders(rootDockingPort);

		registerUtilityWindow (outputDockable);
		registerUtilityWindow (problems);
		registerUtilityWindow (javaScript);
		registerUtilityWindow (wsvd);
		registerUtilityWindow (propertyEditor);
		registerUtilityWindow (toolbox);
		utilityWindows.add(documentPlaceholder);
	}

	public ScriptedActionListener getDefaultActionListener() {
		return defaultActionListener;
	}

	public void maximizeDockableWindow(int ID) {
		DockableWindow dockableWindow = IDToDockableWindowMap.get(ID);

		if (dockableWindow != null) {
			DockingManager.toggleMaximized(dockableWindow);
			dockableWindow.setMaximized(!dockableWindow.isMaximized());

		} else {
			System.err.println ("maximizeDockableWindow: window with ID="+ID+" was not found.");
		}
	}

	public void closeDockableWindow(int ID) throws OperationCancelledException {
		DockableWindow dockableWindow = IDToDockableWindowMap.get(ID);

		if (dockableWindow != null) {
			if (dockableWindow.getContentPanel().getContent() instanceof GraphEditorPanel) {
				GraphEditorPanel editor = (GraphEditorPanel)dockableWindow.getContentPanel().getContent();
				// handle editor window close

				WorkspaceEntry we = editor.getWorkspaceEntry();

				if (editor.getWorkspaceEntry().isUnsaved()) {
					int result = JOptionPane.showConfirmDialog(this, "Model \""+we.getTitle() + "\" ("+we.getModel().getDisplayName()+") has unsaved changes.\nSave before closing?",
							"Confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

					if (result == JOptionPane.YES_OPTION) {
						save(we);
					}
					else if (result == JOptionPane.CANCEL_OPTION)
						throw new OperationCancelledException("Operation cancelled by user.");
				}

				if (we.isTemporary())
					framework.getWorkspace().remove(we);

				if (DockingManager.isMaximized(dockableWindow))
					DockingManager.toggleMaximized(dockableWindow);

				editorWindows.remove(dockableWindow);

				if (editorWindows.isEmpty()) {
					DockingManager.registerDockable(documentPlaceholder);
					DockingManager.dock(documentPlaceholder, dockableWindow, DockingConstants.CENTER_REGION);
					utilityWindows.add(documentPlaceholder);
				}

				DockingManager.close(dockableWindow);
				DockingManager.unregisterDockable(dockableWindow);
				dockableWindow.setClosed(true);

			} else {
				// handle utility window close
				mainMenu.utilityWindowClosed(ID);
				DockingManager.close(dockableWindow);
				dockableWindow.setClosed(true);
			}
		} else {
			System.err.println ("closeDockableWindow: window with ID="+ID+" was not found.");
		}
	}

	public void displayDockableWindow(int ID) {
		DockableWindow dockableWindow = IDToDockableWindowMap.get(ID);
		if (dockableWindow != null) {
			DockingManager.display(dockableWindow);
			dockableWindow.setClosed(false);
			mainMenu.utilityWindowDisplayed(ID);

		} else {
			System.err.println ("displayDockableWindow: window with ID="+ID+" was not found.");
		}
	}

	public void toggleDockableWindow(int ID) {
		DockableWindow dockableWindow = IDToDockableWindowMap.get(ID);

		if (dockableWindow.isClosed())
			displayDockableWindow(ID);
		else
			try {
				closeDockableWindow(ID);
			} catch (OperationCancelledException e) {
				e.printStackTrace();
			}
	}

	private void saveDockingLayout() {
		PerspectiveManager pm = (PerspectiveManager)DockingManager.getLayoutManager();
		pm.getCurrentPerspective().cacheLayoutState(rootDockingPort);
		pm.forceDockableUpdate();
		PerspectiveModel pmodel = new PerspectiveModel(pm.getDefaultPerspective().getPersistentId(), pm.getCurrentPerspectiveName(), pm.getPerspectives());
		XMLPersister pers = new XMLPersister();
		try {
			File file = new File("config/uilayout.xml");
			File parentDir = file.getParentFile();
			if (parentDir != null)
				if (!parentDir.exists())
					parentDir.mkdirs();

			FileOutputStream os = new FileOutputStream(file);
			pers.store(os, pmodel);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}

	private void loadDockingLayout() {
		PerspectiveManager pm = (PerspectiveManager)DockingManager.getLayoutManager();
		XMLPersister pers = new XMLPersister();
		try {
			File f = new File ("./config/uilayout.xml");
			if (!f.exists())
				return;

			FileInputStream is = new FileInputStream(f);

			PerspectiveModel pmodel = pers.load(is);


			pm.remove("defaultWorkspace");
			pm.setCurrentPerspective("defaultWorkspace");

			for (Perspective p : pmodel.getPerspectives())
				pm.add(p, false);

			pm.reload(rootDockingPort);

			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() throws OperationCancelledException {
		closeEditorWindows();


		if (framework.getWorkspace().isChanged() && ! framework.getWorkspace().isTemporary()) {
			int result = JOptionPane.showConfirmDialog(this, "Current workspace has unsaved changes.\nSave before closing?",
					"Confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				workspaceWindow.saveWorkspace();
			}
			else if (result == JOptionPane.CANCEL_OPTION)
				throw new OperationCancelledException("Operation cancelled by user.");
		}

		saveDockingLayout();

		content.remove(rootDockingPort);

		framework.setConfigVar("gui.main.maximised", Boolean.toString((getExtendedState() & JFrame.MAXIMIZED_BOTH)!=0) );
		framework.setConfigVar("gui.main.width", Integer.toString(getWidth()));
		framework.setConfigVar("gui.main.height", Integer.toString(getHeight()));

		if (lastSavePath != null)
			framework.setConfigVar("gui.main.lastSavePath", lastSavePath);
		if (lastOpenPath != null)
			framework.setConfigVar("gui.main.lastOpenPath", lastOpenPath);

		outputWindow.releaseStream();
		errorWindow.releaseStream();

		workspaceWindow.shutdown();

		setVisible(false);
	}

	/*private void unregisterUtilityWindows() {
		for (DockableWindow w : utilityWindows) {
			DockingManager.close(w);
			DockingManager.unregisterDockable(w);
		}
		utilityWindows.clear();
	}*/

	public void createWork() throws OperationCancelledException {
		CreateWorkDialog dialog = new CreateWorkDialog(MainWindow.this);
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1) {
			PluginInfo info = dialog.getSelectedModel();
			try {
				Model mathModel = (Model)framework.getPluginManager().getInstance(info);

				if (!dialog.getModelTitle().isEmpty())
					mathModel.setTitle(dialog.getModelTitle());

				if (dialog.createVisualSelected()) {
					VisualModel visualModel = ModelFactory.createVisualModel(mathModel);
					WorkspaceEntry we = framework.getWorkspace().add(visualModel, false);
					if (dialog.openInEditorSelected())
						createEditorWindow (we);
					//rootDockingPort.dock(new GraphEditorPane(visualModel), CENTER_REGION);
					//addView(new GraphEditorPane(visualModel), mathModel.getTitle() + " - " + mathModel.getDisplayName(), DockingManager.NORTH_REGION, 0.8f);
				} else
					framework.getWorkspace().add(mathModel, false);
			} catch (PluginInstantiationException e) {
				e.printStackTrace();
				//throw new RuntimeException(e);
			} catch (VisualModelInstantiationException e) {
				e.printStackTrace(); //throw new RuntimeException(e);
			}
		} else
			throw new OperationCancelledException("Create operation cancelled by user.");
	}

	public void requestFocus (GraphEditorPanel sender) {
		sender.requestFocusInWindow();

		if (editorInFocus == sender)
			return;

		editorInFocus = sender;

		toolboxWindow.setToolsForModel(editorInFocus.getModel());
		mainMenu.setMenuForModel(editorInFocus.getModel());

		framework.deleteJavaScriptProperty("visualModel", framework.getJavaScriptGlobalScope());
		framework.setJavaScriptProperty("visualModel", sender.getModel(), framework.getJavaScriptGlobalScope(), true);

		framework.deleteJavaScriptProperty("model", framework.getJavaScriptGlobalScope());
		framework.setJavaScriptProperty("model", sender.getModel().getMathModel(), framework.getJavaScriptGlobalScope(), true);
	}

	public ToolboxWindow getToolboxWindow() {
		return toolboxWindow;
	}

	private void printCause (Throwable e) {
		e.printStackTrace();
		System.err.println ("-------------" + e);
		if (e.getCause() != null)
			printCause(e.getCause());
	}

	public void openWork() throws OperationCancelledException {
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);

		if (lastOpenPath != null)
			fc.setCurrentDirectory(new File(lastOpenPath));

		fc.setFileFilter(FileFilters.DOCUMENT_FILES);
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Open work file(s)");

		if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
			for (File f : fc.getSelectedFiles()) {
				try {
					WorkspaceEntry we = framework.getWorkspace().add(f.getPath(), true);
					if (we.getModel() instanceof VisualModel)
						createEditorWindow(we);
				} catch (DeserialisationException e) {
					JOptionPane.showMessageDialog(this, "A problem was encountered while trying to load \"" + f.getPath()
							+"\".\nPlease see Problems window for details.", "Load failed", JOptionPane.ERROR_MESSAGE);
					printCause(e);

				}
			}
			lastOpenPath = fc.getCurrentDirectory().getPath();
		} else
			throw new OperationCancelledException("Open operation cancelled by user.");
	}

	public void saveWork() throws OperationCancelledException {
		if (editorInFocus != null)
			save(editorInFocus.getWorkspaceEntry());
		else
			System.out.println ("No editor in focus");
	}

	public void saveWorkAs() throws OperationCancelledException {
		if (editorInFocus != null)
			saveAs(editorInFocus.getWorkspaceEntry());
		else
			System.err.println ("No editor in focus");
	}

	public void save(WorkspaceEntry we) throws OperationCancelledException {
		if (we.getFile() == null) {
			saveAs(we);
		}
		try {
			framework.save(we.getModel(), we.getFile().getPath());
		} catch (SerialisationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Model export failed", JOptionPane.ERROR_MESSAGE);
		}
		we.setUnsaved(false);
		lastSavePath = we.getFile().getParent();
	}

	private static String removeSpecialFileNameCharacters(String fileName)
	{
		return fileName
		.replace('\\', '_')
		.replace('/', '_')
		.replace(':', '_')
		.replace('"', '_')
		.replace('<', '_')
		.replace('>', '_')
		.replace('|', '_');
	}

	public void saveAs(WorkspaceEntry we) throws OperationCancelledException {
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.SAVE_DIALOG);

		String title = we.getModel().getTitle();
		title = removeSpecialFileNameCharacters(title);

		fc.setSelectedFile(new File(title));
		fc.setFileFilter(FileFilters.DOCUMENT_FILES);

		if (lastSavePath != null)
			fc.setCurrentDirectory(new File(lastSavePath));

		String path;

		while (true) {
			if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
				path = fc.getSelectedFile().getPath();

				if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION))
					path += FileFilters.DOCUMENT_EXTENSION;

				File f = new File(path);

				if (!f.exists())
					break;
				else
					if (JOptionPane.showConfirmDialog(this, "The file \"" + f.getName()+"\" already exists. Do you want to overwrite it?", "Confirm",
							JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						break;
			} else
				throw new OperationCancelledException("Save operation cancelled by user.");
		}

		try {
			framework.save(we.getModel(), path);
			we.setFile(new File(path));
			we.setUnsaved(false);
			lastSavePath = fc.getCurrentDirectory().getPath();
		} catch (SerialisationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Model export failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void importFrom() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);

		if (lastOpenPath != null)
			fc.setCurrentDirectory(new File(lastOpenPath));


		PluginInfo[] importerInfo = framework.getPluginManager().getPluginsImplementing(Importer.class.getName());
		Importer[] importers = new Importer[importerInfo.length];

		int cnt = 0;

		for (PluginInfo info : importerInfo) {
			try {
				Importer importer =  (Importer)framework.getPluginManager().getSingleton(info);
				importers[cnt++] = importer;
			} catch (PluginInstantiationException e) {
				e.printStackTrace();
				importers[cnt++] = null;
			}
		}

		fc.setAcceptAllFileFilterUsed(false);

		for (Importer importer : importers) {
			fc.addChoosableFileFilter(new ImporterFileFilter(importer));
		}

		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Import model(s)");

		if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
			for (File f : fc.getSelectedFiles()) {
				for (Importer importer : importers) {
					if (importer.accept(f)) {
						Model model;
						try {
							model = Import.importFromFile(importer, f);
							WorkspaceEntry we = framework.getWorkspace().add(model, false);
							if (we.getModel() instanceof VisualModel)
								createEditorWindow(we);
							break;
						} catch (IOException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(this, e.getMessage(), "I/O error", JOptionPane.ERROR_MESSAGE);
						} catch (DeserialisationException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(this, e.getMessage(), "Import error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			lastOpenPath = fc.getCurrentDirectory().getPath();
		}
	}

	public void runModelChecker(String modelCheckerClassName) {
		try {
			ModelChecker checker = (ModelChecker)framework.getPluginManager().getSingletonByName(modelCheckerClassName);
			checker.run(editorInFocus.getModel());
		} catch (ModelCheckingFailedException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Model checking failed", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (PluginInstantiationException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Model checking failed", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}


	public void doLayout (String layoutClassName) {
		try {
			Layout layout = (Layout)framework.getPluginManager().getSingletonByName(layoutClassName);
			layout.doLayout(editorInFocus.getModel());
		} catch (LayoutFailedException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Layout failed", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (PluginInstantiationException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Layout failed", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public void exportTo(String exporterClassName) throws OperationCancelledException {
		Exporter exporter;

		try {
			exporter = (Exporter)framework.getPluginManager().getSingletonByName(exporterClassName);
		} catch (PluginInstantiationException e1) {
			JOptionPane.showMessageDialog(this, e1.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
			return;
		}

		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setDialogTitle("Export as " + exporter.getDescription());
		fc.setAcceptAllFileFilterUsed(false);

		String title = editorInFocus.getModel().getTitle();
		title = removeSpecialFileNameCharacters(title);

		fc.setSelectedFile(new File(title));
		fc.setFileFilter(new ExporterFileFilter(exporter));

		if (lastSavePath != null)
			fc.setCurrentDirectory(new File(lastSavePath));

		String path;

		while (true) {
			if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
				path = fc.getSelectedFile().getPath();
				if (!path.endsWith(exporter.getExtenstion()))
					path += exporter.getExtenstion();

				File f = new File(path);

				if (!f.exists())
					break;
				else
					if (JOptionPane.showConfirmDialog(this, "The file \"" + f.getName()+"\" already exists. Do you want to overwrite it?", "Confirm",
							JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						break;
			} else
				throw new OperationCancelledException("Save operation cancelled by user.");
		}

		try {
			Export.exportToFile(exporter, editorInFocus.getModel(), path);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ModelValidationException e) {
			e.printStackTrace();
		} catch (SerialisationException e) {
			e.printStackTrace();
		}

		lastSavePath = fc.getCurrentDirectory().getPath();
	}



	public GraphEditorPanel getCurrentEditor() {
		return editorInFocus;
	}

	public void repaintCurrentEditor() {
		if (editorInFocus != null)
			editorInFocus.repaint();
	}

	public void togglePropertyEditor() {

	}

	public PropertyEditorWindow getPropertyView() {
		return propertyEditorWindow;
	}

	public Framework getFramework() {
		return framework;
	}

	public void closeEditorWindows() throws OperationCancelledException {
		LinkedHashSet<DockableWindow> windowsToClose = new LinkedHashSet<DockableWindow>(editorWindows);

		for (DockableWindow w : windowsToClose) {
			closeDockableWindow(w.getID());
		}
	}

	public void editSettings() {
		PersistentPropertyEditorDialog dlg = new PersistentPropertyEditorDialog(this);
		dlg.setModal(false);
		dlg.setResizable(true);
		dlg.setVisible(true);
	}
}

class ImporterFileFilter extends javax.swing.filechooser.FileFilter {
	private Importer importer;

	public ImporterFileFilter(Importer importer) {
		this.importer = importer;
	}

	public boolean accept(File f) {
		return importer.accept(f);
	}

	public String getDescription() {
		return importer.getDescription();
	}
}

class ExporterFileFilter extends javax.swing.filechooser.FileFilter {
	private Exporter exporter;

	public ExporterFileFilter(Exporter exporter) {
		this.exporter = exporter;
	}

	public boolean accept(File f) {
		return ( f.isDirectory() || f.getName().endsWith (exporter.getExtenstion()));
	}

	public String getDescription() {
		return exporter.getDescription();
	}
}