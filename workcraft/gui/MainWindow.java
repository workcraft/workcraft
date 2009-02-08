package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import org.flexdock.perspective.persist.PerspectiveModel;
import org.flexdock.perspective.persist.xml.XMLPersister;
import org.flexdock.plaf.common.border.ShadowBorder;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.utils.SubstanceConstants.TabContentPaneBorderKind;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.Framework;
import org.workcraft.framework.ModelFactory;
import org.workcraft.framework.ModelSaveFailedException;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.workspace.WorkspaceEntry;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.edit.graph.GraphEditorPanel;
import org.workcraft.gui.edit.graph.ToolboxWindow;
import org.workcraft.gui.workspace.WorkspaceWindow;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
	public static class Actions {
		public static final ScriptedAction CREATE_WORK_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.createWork()";
			}
			public String getText() {
				return "Create work...";
			};
		};
		public static final ScriptedAction OPEN_WORK_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.openWork()";
			}
			public String getText() {
				return "Open work...";
			};
		};
		public static final ScriptedAction SAVE_WORK_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.saveWork()";
			}
			public String getText() {
				return "Save work";
			};
		};
		public static final ScriptedAction SAVE_WORK_AS_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.saveWorkAs()";
			}
			public String getText() {
				return "Save work as...";
			};
		};
		public static final ScriptedAction EXIT_ACTION = new ScriptedAction() {
			public String getScript() {
				return "framework.shutdown()";
			}
			public String getText() {
				return "Exit";
			};
		};
		public static final ScriptedAction SHUTDOWN_GUI_ACTION = new ScriptedAction() {
			public String getScript() {
				return "framework.shutdownGUI()";
			}
			public String getText() {
				return "Switch to console mode";
			};
		};
		public static final ScriptedAction RECONFIGURE_PLUGINS_ACTION = new ScriptedAction() {
			public String getScript() {
				return "framework.getPluginManager().reconfigure()";
			}
			public String getText() {
				return "Reconfigure plugins";
			};
		};
		public static final ScriptedAction SAVE_UI_LAYOUT = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.saveDockingLayout()";
			}
			public String getText() {
				return "Save UI layout";
			};
		};
	}

	private final ScriptedActionListener defaultActionListener = new ScriptedActionListener() {
		public void actionPerformed(ScriptedAction e) {
			 if (e.getScript() == null)
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
			}

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
	private DockableWindow lastEditorDockable;

	private GraphEditorPanel editorInFocus;

	private MainMenu mainMenu;

	private String lastSavePath = null;
	private String lastOpenPath = null;

	private int dockableIDCounter = 0;
	private HashMap<Integer, DockableWindow> IDToDockableWindowMap = new HashMap<Integer, DockableWindow>();

	protected void createWindows() {
		workspaceWindow  = new WorkspaceWindow(framework);
		framework.getWorkspace().addListener(workspaceWindow);
		workspaceWindow.setVisible(true);
		propertyEditorWindow = new PropertyEditorWindow(framework);

		outputWindow = new OutputWindow(framework);
		errorWindow = new ErrorWindow(framework);
		jsWindow = new JavaScriptWindow(framework);

		toolboxWindow = new ToolboxWindow(framework);

		lastEditorDockable = null;
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

	public void setLAF(String laf) {
		if (JOptionPane.showConfirmDialog(this, "Changing Look and Feel requires GUI restart.\n\n" +
				"This will not affect the workspace (i.e. open documents will stay open),\n" +
				"but the visual editor windows will be closed.\n\nProceed?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
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
		return createDockableWindow(component, name, neighbour, options, DockingConstants.CENTER_REGION, 0.5f, name);
	}

	private DockableWindow createDockableWindow(JComponent component, String name, int options, String relativeRegion, float split) {
		return createDockableWindow(component, name, null, options, relativeRegion, split, name);
	}

	private DockableWindow createDockableWindow(JComponent component, String name, Dockable neighbour, int options, String relativeRegion, float split) {
		return createDockableWindow(component, name, neighbour, options, relativeRegion, split, name);
	}

	private DockableWindow createDockableWindow(JComponent component, String name, Dockable neighbour, int options, String relativeRegion, float split, String persistentID) {
		int ID = getNextDockableID();

		DockableWindowContentPanel panel = new DockableWindowContentPanel(this, ID, name, component, options);
		DockableWindow dockable = new DockableWindow(panel, name);
		DockingManager.registerDockable(dockable);

		IDToDockableWindowMap.put(ID, dockable);

		if (neighbour != null)
			DockingManager.dock(dockable, neighbour, relativeRegion);
		else
			DockingManager.dock(dockable, rootDockingPort, relativeRegion);
		DockingManager.setSplitProportion(dockable, split);

		return dockable;
	}

	public void createEditorView(WorkspaceEntry we) {
		if (we.getModel() == null) {
			JOptionPane.showMessageDialog(this, "The selected entry is not a Workcraft model, and cannot be edited.", "Cannot open editor", JOptionPane.ERROR_MESSAGE);
			return;
		}

		VisualModel visualModel = we.getModel().getVisualModel();

		if (visualModel == null)
				try {
					visualModel = ModelFactory.createVisualModel(we.getModel().getMathModel());
					we.setModel(visualModel);
				} catch (VisualModelInstantiationException e) {
					JOptionPane.showMessageDialog(this, "A visual model could not be created for the model you have chosen:\n" + e.getMessage(), "Error creating visual model", JOptionPane.ERROR_MESSAGE);
					return;
				}

				GraphEditorPanel editor = new GraphEditorPanel(this, we);
				String dockableTitle = we.getTitle() + " - " + visualModel.getDisplayName();
				DockableWindow dockable;

				if (lastEditorDockable == null)
					dockable = createDockableWindow (editor, dockableTitle, outputDockable,
							DockableWindowContentPanel.CLOSE_BUTTON | DockableWindowContentPanel.MAXIMIZE_BUTTON, DockingConstants.NORTH_REGION, 0.8f);
				else
					dockable = createDockableWindow (editor, dockableTitle, lastEditorDockable,
							DockableWindowContentPanel.CLOSE_BUTTON | DockableWindowContentPanel.MAXIMIZE_BUTTON);

				lastEditorDockable = dockable;

				requestFocus(editor);
	}

	public void startup() {
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
		//SwingUtilities.updateComponentTreeUI(MainWindow.this);

		String laf = framework.getConfigVar("gui.lookandfeel");
		if (laf == null)
			laf = UIManager.getCrossPlatformLookAndFeelClassName();
		LAF.setLAF(laf);

		content = new JPanel(new BorderLayout(0,0));

		rootDockingPort = new DefaultDockingPort();
		// rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()));
		//this.rootDockingPort.setSingleTabAllowed(true);
		content.add(rootDockingPort, BorderLayout.CENTER);

		setContentPane(content);

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
		DockableWindow javaScript =  createDockableWindow (jsWindow, "JavaScript", outputDockable, DockableWindowContentPanel.CLOSE_BUTTON);

		DockableWindow wsvd = createDockableWindow (workspaceWindow, "Workspace", DockableWindowContentPanel.CLOSE_BUTTON, DockingManager.EAST_REGION, 0.8f);
		DockableWindow propertyEditor = createDockableWindow (propertyEditorWindow, "Property Editor", wsvd,  DockableWindowContentPanel.CLOSE_BUTTON, DockingManager.NORTH_REGION, 0.5f);
		DockableWindow toolbox = createDockableWindow (toolboxWindow, "Editor Tools", wsvd, DockableWindowContentPanel.CLOSE_BUTTON, DockingManager.NORTH_REGION, 0.5f);

		mainMenu.addWindow(outputDockable);
		mainMenu.addWindow(problems);
		mainMenu.addWindow(javaScript);
		mainMenu.addWindow(wsvd);
		mainMenu.addWindow(propertyEditor);
		mainMenu.addWindow(toolbox);

		DockingManager.display(outputDockable);
		EffectsManager.setPreview(new AlphaPreview(Color.BLACK, Color.GRAY, 0.5f));

		workspaceWindow.startup();

		setVisible(true);

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

	public void closeDockableWindow(int ID) {
		DockableWindow dockableWindow = IDToDockableWindowMap.get(ID);

		if (dockableWindow != null) {
			DockingManager.close(dockableWindow);
			dockableWindow.setClosed(true);
			mainMenu.windowClosed(ID);

		} else {
			System.err.println ("closeDockableWindow: window with ID="+ID+" was not found.");
		}
	}

	public void displayDockableWindow(int ID) {
		DockableWindow dockableWindow = IDToDockableWindowMap.get(ID);
		if (dockableWindow != null) {
			DockingManager.display(dockableWindow);
			dockableWindow.setClosed(false);
			mainMenu.windowDisplayed(ID);

		} else {
			System.err.println ("displayDockableWindow: window with ID="+ID+" was not found.");
		}
	}

	public void toggleDockableWindow(int ID) {
		DockableWindow dockableWindow = IDToDockableWindowMap.get(ID);

		if (dockableWindow.isClosed())
			displayDockableWindow(ID);
		else
			closeDockableWindow(ID);
	}

	public void saveDockingLayout() {
		Perspective p = new Perspective ("a", "a");
		p.cacheLayoutState(rootDockingPort);
		PerspectiveModel pm = new PerspectiveModel("a", "a", new Perspective[] {p});
		XMLPersister pers = new XMLPersister();
		try {
			pers.store(new FileOutputStream(new File ("./config/uilayout.xml")), pm);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
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

	public void createWork() {
		CreateWorkDialog dialog = new CreateWorkDialog(MainWindow.this);
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1) {
			PluginInfo info = dialog.getSelectedModel();
			try {
				MathModel mathModel = (MathModel)framework.getPluginManager().getInstance(info);

				if (!dialog.getModelTitle().isEmpty())
					mathModel.setTitle(dialog.getModelTitle());

				if (dialog.createVisualSelected()) {
					VisualModel visualModel = ModelFactory.createVisualModel(mathModel);
					WorkspaceEntry we = framework.getWorkspace().add(visualModel);
					if (dialog.openInEditorSelected())
						createEditorView (we);
					//rootDockingPort.dock(new GraphEditorPane(visualModel), CENTER_REGION);
					//addView(new GraphEditorPane(visualModel), mathModel.getTitle() + " - " + mathModel.getDisplayName(), DockingManager.NORTH_REGION, 0.8f);
				} else
					framework.getWorkspace().add(mathModel);
			} catch (PluginInstantiationException e) {
				System.err.println(e.getMessage());
			} catch (VisualModelInstantiationException e) {
				System.err.println(e.getMessage());
			}
		}
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

		//editorInFocus.requestFocusInWindow();
	}

	public ToolboxWindow getToolboxWindow() {
		return toolboxWindow;
	}

	public void openWork() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);

		if (lastOpenPath != null)
			fc.setCurrentDirectory(new File(lastOpenPath));

		fc.setFileFilter(FileFilters.DOCUMENT_FILES);
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Open work file(s))");

		if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
			for (File f : fc.getSelectedFiles()) {
				try {
					WorkspaceEntry we = framework.getWorkspace().add(f.getPath());
					if (we.getModel() instanceof VisualModel)
						createEditorView(we);
				} catch (ModelLoadFailedException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot load " + f.getPath() , JOptionPane.ERROR_MESSAGE);
				}
			}
			lastOpenPath = fc.getCurrentDirectory().getPath();
		}
	}

	public void saveWork() {
		if (editorInFocus != null)
			save(editorInFocus.getWorkspaceEntry());
		else
			System.out.println ("No editor in focus");
	}

	public void saveWorkAs() {
		if (editorInFocus != null)
			saveAs(editorInFocus.getWorkspaceEntry());
		else
			System.out.println ("No editor in focus");
	}

	public void save(WorkspaceEntry we) {
		if (we.getFile() == null) {
			saveAs(we);
			return;
		}

		try {
			framework.save(we.getModel(), we.getFile().getPath());
			we.setUnsaved(false);
			lastSavePath = we.getFile().getParent();

		} catch (ModelSaveFailedException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
		}
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

	public void saveAs(WorkspaceEntry we) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.SAVE_DIALOG);

		String title = we.getModel().getTitle();
		title = removeSpecialFileNameCharacters(title);

		fc.setSelectedFile(new File(title));
		fc.setFileFilter(FileFilters.DOCUMENT_FILES);

		if (lastSavePath != null)
			fc.setCurrentDirectory(new File(lastSavePath));

		if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			if (!fc.getSelectedFile().exists())
				if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION))
					path += FileFilters.DOCUMENT_EXTENSION;

			File f = new File(path);
			if (f.exists())
				if (JOptionPane.showConfirmDialog(this, "The file \"" + f.getName()+"\" already exists. Do you want to overwrite it?", "Confirm", JOptionPane.YES_NO_OPTION)==JOptionPane.NO_OPTION)
					return;
			try {
				framework.save(we.getModel(), path);
				we.setFile(fc.getSelectedFile());
				we.setUnsaved(false);
				lastSavePath = fc.getCurrentDirectory().getPath();
			} catch (ModelSaveFailedException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
			}
		}
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

}