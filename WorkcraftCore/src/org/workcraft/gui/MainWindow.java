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
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.defaults.StandardBorderManager;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.drag.preview.GhostPreview;
import org.flexdock.docking.props.PropertyManager;
import org.flexdock.docking.state.PersistenceException;
import org.flexdock.perspective.Perspective;
import org.flexdock.perspective.PerspectiveManager;
import org.flexdock.perspective.persist.FilePersistenceHandler;
import org.flexdock.perspective.persist.PersistenceHandler;
import org.flexdock.perspective.persist.PerspectiveModel;
import org.flexdock.perspective.persist.xml.XMLPersister;
import org.flexdock.plaf.common.border.ShadowBorder;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceConstants.TabContentPaneBorderKind;
import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.LayoutException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.SettingsEditorDialog;
import org.workcraft.gui.tasks.TaskFailureNotifier;
import org.workcraft.gui.tasks.TaskManagerWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.layout.AbstractLayoutTool;
import org.workcraft.plugins.layout.DotLayoutTool;
import org.workcraft.plugins.layout.RandomLayoutTool;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.Import;
import org.workcraft.util.ListMap;
import org.workcraft.util.LogUtils;
import org.workcraft.util.Tools;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
    private static final String FLEXDOCK_WORKSPACE = "defaultWorkspace";
    private static final String FLEXDOCK_DOCKING_PORT = "defaultDockingPort";

    private static final String CONFIG_GUI_MAIN_MAXIMISED = "gui.main.maximised";
    private static final String CONFIG_GUI_MAIN_WIDTH = "gui.main.width";
    private static final String CONFIG_GUI_MAIN_HEIGHT = "gui.main.height";
    private static final String CONFIG_GUI_MAIN_LAST_SAVE_PATH = "gui.main.lastSavePath";
    private static final String CONFIG_GUI_MAIN_LAST_OPEN_PATH = "gui.main.lastOpenPath";
    private static final String CONFIG_GUI_MAIN_RECENT_FILE = "gui.main.recentFile";

    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 450;

    public static final String TITLE_WORKCRAFT = "Workcraft";
    public static final String TITLE_OUTPUT = "Output";
    public static final String TITLE_PROBLEMS = "Problems";
    public static final String TITLE_JAVASCRIPT = "Javascript";
    public static final String TITLE_TASKS = "Tasks";
    public static final String TITLE_WORKSPACE = "Workspace";
    public static final String TITLE_PROPERTY_EDITOR = "Property editor";
    public static final String TITLE_TOOL_CONTROLS = "Tool controls";
    public static final String TITLE_EDITOR_TOOLS = "Editor tools";
    public static final String TITLE_PLACEHOLDER = "";

    private final ScriptedActionListener defaultActionListener = new ScriptedActionListener() {
        public void actionPerformed(Action e) {
            e.run();
        }
    };

    private JPanel content;

    private DefaultDockingPort rootDockingPort;
    private DockableWindow documentPlaceholder;
    private DockableWindow outputDockable;
    private DockableWindow erroDockable;
    private DockableWindow javaScriptDockable;
    private DockableWindow tasksDockable;
    private DockableWindow workspaceDockable;
    private DockableWindow propertyEditorDockable;
    private DockableWindow toolControlsDockable;
    private DockableWindow editorToolsDockable;

    private OutputWindow outputWindow;
    private ErrorWindow errorWindow;
    private JavaScriptWindow javaScriptWindow;
    private PropertyEditorWindow propertyEditorWindow;
    private SimpleContainer toolControlsWindow;
    private SimpleContainer editorToolsWindow;
    private WorkspaceWindow workspaceWindow;

    private ListMap<WorkspaceEntry, DockableWindow> editorWindows = new ListMap<WorkspaceEntry, DockableWindow>();
    private LinkedList<DockableWindow> utilityWindows = new LinkedList<DockableWindow>();

    private GraphEditorPanel editorInFocus;
    private MainMenu mainMenu;

    private String lastSavePath = null;
    private String lastOpenPath = null;
    private LinkedHashSet<String> recentFiles = new LinkedHashSet<String>();

    private int dockableIDCounter = 0;
    private HashMap<Integer, DockableWindow> IDToDockableWindowMap = new HashMap<Integer, DockableWindow>();

    protected void createWindows() {
        workspaceWindow = new WorkspaceWindow();
        workspaceWindow.setVisible(true);

        outputWindow = new OutputWindow();
        errorWindow = new ErrorWindow();
        javaScriptWindow = new JavaScriptWindow();

        propertyEditorWindow = new PropertyEditorWindow();
        toolControlsWindow = new SimpleContainer();
        editorToolsWindow = new SimpleContainer();
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    }

    public MainWindow() {
        super();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                final Framework framework = Framework.getInstance();
                framework.shutdown();
            }
        });
    }

    private int getNextDockableID() {
        return dockableIDCounter++;
    }

    private DockableWindow createDockableWindow(JComponent component, String title, Dockable neighbour, int options) {
        return createDockableWindow(component, title, neighbour, options, DockingConstants.CENTER_REGION, title);
    }

    private DockableWindow createDockableWindow(JComponent component, String name, int options, String relativeRegion,
            float split) {
        return createDockableWindow(component, name, null, null, options, relativeRegion, split, name);
    }

    private DockableWindow createDockableWindow(JComponent component, String name, Dockable neighbour, int options,
            String relativeRegion, float split) {
        return createDockableWindow(component, name, null, neighbour, options, relativeRegion, split, name);
    }

    private DockableWindow createDockableWindow(JComponent component, String title, Dockable neighbour, int options,
            String relativeRegion, String persistentID) {

        int id = getNextDockableID();
        DockableWindowContentPanel panel = new DockableWindowContentPanel(this, id, title, component, options);

        DockableWindow dockable = new DockableWindow(this, panel, persistentID);
        DockingManager.registerDockable(dockable);
        IDToDockableWindowMap.put(id, dockable);

        if (neighbour != null) {
            DockingManager.dock(dockable, neighbour, relativeRegion);
        } else {
            DockingManager.dock(dockable, rootDockingPort, relativeRegion);
        }
        return dockable;
    }

    private DockableWindow createDockableWindow(JComponent component, String title, String tooltip, Dockable neighbour,
            int options, String relativeRegion, float split, String persistentID) {
        DockableWindow dockable = createDockableWindow(component, title, neighbour, options, relativeRegion,
                persistentID);
        DockingManager.setSplitProportion(dockable, split);
        return dockable;
    }

    public GraphEditorPanel createEditorWindow(final WorkspaceEntry we) {
        if (we.getModelEntry() == null) {
            throw new RuntimeException("Cannot open editor: the selected entry is not a Workcraft model.");
        }
        ModelEntry modelEntry = we.getModelEntry();
        ModelDescriptor descriptor = modelEntry.getDescriptor();
        VisualModel visualModel = null;
        if (modelEntry.getModel() instanceof VisualModel) {
            visualModel = (VisualModel) modelEntry.getModel();
            // Ignore saved selection (it is only useful for copy-paste)
            visualModel.selectNone();
        }

        if (visualModel == null) {
            VisualModelDescriptor vmd = descriptor.getVisualModelDescriptor();
            if (vmd == null) {
                JOptionPane.showMessageDialog(MainWindow.this,
                        "A visual model could not be created for the selected model.\n" + "Model '"
                                + descriptor.getDisplayName() + "' does not have visual model support.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            try {
                visualModel = vmd.create((MathModel) modelEntry.getModel());
                modelEntry.setModel(visualModel);
            } catch (VisualModelInstantiationException e) {
                JOptionPane.showMessageDialog(MainWindow.this,
                        "A visual model could not be created for the selected model.\nPlease refer to the Problems window for details.\n",
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return null;
            }
            try {
                applyDefaultLayout(visualModel);
                we.setModelEntry(modelEntry);
            } catch (LayoutException e) {
                // Layout failed for whatever reason, ignore
            }
        }

        final GraphEditorPanel editor = new GraphEditorPanel(this, we);
        String title = getTitle(we, visualModel);
        final DockableWindow editorWindow;
        if (editorWindows.isEmpty()) {
            editorWindow = createDockableWindow(editor, title, documentPlaceholder,
                    DockableWindowContentPanel.CLOSE_BUTTON | DockableWindowContentPanel.MAXIMIZE_BUTTON,
                    DockingConstants.CENTER_REGION, "Document" + we.getWorkspacePath());
            DockingManager.close(documentPlaceholder);
            DockingManager.unregisterDockable(documentPlaceholder);
            utilityWindows.remove(documentPlaceholder);
        } else {
            DockableWindow firstEditorWindow = editorWindows.values().iterator().next().iterator().next();
            editorWindow = createDockableWindow(editor, title, firstEditorWindow,
                    DockableWindowContentPanel.CLOSE_BUTTON | DockableWindowContentPanel.MAXIMIZE_BUTTON,
                    DockingConstants.CENTER_REGION, "Document" + we.getWorkspacePath());
        }
        editorWindow.addTabListener(new EditorWindowTabListener(editor));
        editorWindows.put(we, editorWindow);
        requestFocus(editor);
        setWorkActionsEnableness(true);
        editor.zoomFit();
        return editor;
    }

    private void applyDefaultLayout(VisualModel visualModel) {
        AbstractLayoutTool layoutTool = visualModel.getBestLayoutTool();
        if (layoutTool == null) {
            layoutTool = new DotLayoutTool();
        }
        try {
            layoutTool.layout(visualModel);
        } catch (LayoutException e) {
            layoutTool = new RandomLayoutTool();
            layoutTool.layout(visualModel);
        }
    }

    private void registerUtilityWindow(DockableWindow dockableWindow) {
        if (!rootDockingPort.getDockables().contains(dockableWindow)) {
            dockableWindow.setClosed(true);
            DockingManager.close(dockableWindow);
        }
        mainMenu.registerUtilityWindow(dockableWindow);
        utilityWindows.add(dockableWindow);
    }

    public void setWindowSize(boolean maximised, int width, int height) {
        if (maximised) {
            DisplayMode mode = this.getGraphicsConfiguration().getDevice().getDisplayMode();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());
            this.setMaximizedBounds(new Rectangle(mode.getWidth() - insets.right - insets.left,
                    mode.getHeight() - insets.top - insets.bottom));
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            this.setSize(mode.getWidth() - insets.right - insets.left, mode.getHeight() - insets.top - insets.bottom);
        } else {
            this.setExtendedState(JFrame.NORMAL);
        }
    }

    public void startup() {
        MainWindowIconManager.apply(this);

        JDialog.setDefaultLookAndFeelDecorated(true);
        UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);

        setTitle(TITLE_WORKCRAFT);
        mainMenu = new MainMenu(this);
        setJMenuBar(mainMenu);

        SilverOceanTheme.enable();
        LookAndFeelHelper.setDefaultLookAndFeel();
        SwingUtilities.updateComponentTreeUI(this);

        content = new JPanel(new BorderLayout(0, 0));
        setContentPane(content);

        PerspectiveManager pm = (PerspectiveManager) DockingManager.getLayoutManager();
        pm.add(new Perspective(FLEXDOCK_WORKSPACE, FLEXDOCK_WORKSPACE));
        pm.setCurrentPerspective(FLEXDOCK_WORKSPACE, true);

        PropertyManager.getDockingPortRoot().setTabPlacement(SwingConstants.TOP);

        rootDockingPort = new DefaultDockingPort(FLEXDOCK_DOCKING_PORT);
        content.add(rootDockingPort, BorderLayout.CENTER);

        createWindows();

        outputWindow.captureStream();
        errorWindow.captureStream();

        StandardBorderManager borderManager = new StandardBorderManager(new ShadowBorder());
        rootDockingPort.setBorderManager(borderManager);

        float xSplit = 0.888f;
        float ySplit = 0.8f;

        outputDockable = createDockableWindow(outputWindow, TITLE_OUTPUT, DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.SOUTH_REGION, ySplit);

        erroDockable = createDockableWindow(errorWindow, TITLE_PROBLEMS, outputDockable,
                DockableWindowContentPanel.CLOSE_BUTTON);

        javaScriptDockable = createDockableWindow(javaScriptWindow, TITLE_JAVASCRIPT, outputDockable,
                DockableWindowContentPanel.CLOSE_BUTTON);

        tasksDockable = createDockableWindow(new TaskManagerWindow(), TITLE_TASKS, outputDockable,
                DockableWindowContentPanel.CLOSE_BUTTON);

        workspaceDockable = createDockableWindow(workspaceWindow, TITLE_WORKSPACE,
                DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON, DockingManager.EAST_REGION,
                xSplit);

        propertyEditorDockable = createDockableWindow(propertyEditorWindow, TITLE_PROPERTY_EDITOR, workspaceDockable,
                DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.NORTH_REGION, ySplit);

        toolControlsDockable = createDockableWindow(editorToolsWindow, TITLE_TOOL_CONTROLS, propertyEditorDockable,
                DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.SOUTH_REGION, 0.4f);

        editorToolsDockable = createDockableWindow(toolControlsWindow, TITLE_EDITOR_TOOLS, toolControlsDockable,
                DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.SOUTH_REGION, 0.795f);

        documentPlaceholder = createDockableWindow(new DocumentPlaceholder(), TITLE_PLACEHOLDER, null, outputDockable,
                0, DockingManager.NORTH_REGION, ySplit, "DocumentPlaceholder");

        DockingManager.setAutoPersist(true);
        EffectsManager.setPreview(new GhostPreview());
        //EffectsManager.setPreview(new XORPreview());
        //EffectsManager.setPreview(new AlphaPreview(Color.BLACK, Color.GRAY, 0.5f));

        loadRecentFilesFromConfig();
        loadWindowGeometryFromConfig();
        // FIXME: Restoring the layout does not work well for FlexDock.
        //loadDockingLayout();

        DockingManager.display(outputDockable);
        setVisible(true);

        DockableWindow.updateHeaders(rootDockingPort, getDefaultActionListener());

        registerUtilityWindow(outputDockable);
        registerUtilityWindow(erroDockable);
        registerUtilityWindow(javaScriptDockable);
        registerUtilityWindow(tasksDockable);
        registerUtilityWindow(propertyEditorDockable);
        registerUtilityWindow(editorToolsDockable);
        registerUtilityWindow(toolControlsDockable);
        registerUtilityWindow(workspaceDockable);

        utilityWindows.add(documentPlaceholder);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Hack to fix the annoying delay occurring when
                // createGlyphVector is called for the first time.
                Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
                FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
                font.createGlyphVector(frc, TITLE_PLACEHOLDER);
                // Force SVG rendering classes to load.
                GUI.createIconFromSVG("images/icons/svg/place.svg");
            }
        }).start();

        setWorkActionsEnableness(false);
    }

    private void setWorkActionsEnableness(boolean enable) {
        getMainMenu().setExportMenuState(enable);
        MainWindowActions.MERGE_WORK_ACTION.setEnabled(enable);
        MainWindowActions.CLOSE_ACTIVE_EDITOR_ACTION.setEnabled(enable);
        MainWindowActions.CLOSE_ALL_EDITORS_ACTION.setEnabled(enable);
        MainWindowActions.SAVE_WORK_ACTION.setEnabled(enable);
        MainWindowActions.SAVE_WORK_AS_ACTION.setEnabled(enable);
        MainWindowActions.EDIT_DELETE_ACTION.setEnabled(enable);
        MainWindowActions.EDIT_SELECT_ALL_ACTION.setEnabled(enable);
        MainWindowActions.EDIT_SELECT_INVERSE_ACTION.setEnabled(enable);
        MainWindowActions.EDIT_SELECT_NONE_ACTION.setEnabled(enable);
        if (!enable) {
            MainWindowActions.EDIT_UNDO_ACTION.setEnabled(false);
            MainWindowActions.EDIT_REDO_ACTION.setEnabled(false);
            MainWindowActions.EDIT_CUT_ACTION.setEnabled(false);
            MainWindowActions.EDIT_COPY_ACTION.setEnabled(false);
            MainWindowActions.EDIT_PASTE_ACTION.setEnabled(false);
        }
        MainWindowActions.VIEW_ZOOM_IN.setEnabled(enable);
        MainWindowActions.VIEW_ZOOM_OUT.setEnabled(enable);
        MainWindowActions.VIEW_ZOOM_DEFAULT.setEnabled(enable);
        MainWindowActions.VIEW_PAN_CENTER.setEnabled(enable);
        MainWindowActions.VIEW_ZOOM_FIT.setEnabled(enable);
        MainWindowActions.VIEW_PAN_LEFT.setEnabled(enable);
        MainWindowActions.VIEW_PAN_UP.setEnabled(enable);
        MainWindowActions.VIEW_PAN_RIGHT.setEnabled(enable);
        MainWindowActions.VIEW_PAN_DOWN.setEnabled(enable);
    }

    public ScriptedActionListener getDefaultActionListener() {
        return defaultActionListener;
    }

    public void toggleDockableWindowMaximized(int id) {
        DockableWindow dockableWindow = IDToDockableWindowMap.get(id);

        if (dockableWindow != null) {
            DockingManager.toggleMaximized(dockableWindow);
            dockableWindow.setMaximized(!dockableWindow.isMaximized());
        } else {
            System.err.println("toggleDockableWindowMaximized: window with ID=" + id + " was not found.");
        }
    }

    public void closeDockableWindow(int id) throws OperationCancelledException {
        DockableWindow dockableWindow = IDToDockableWindowMap.get(id);
        if (dockableWindow != null)
            closeDockableWindow(dockableWindow);
        else
            System.err.println("closeDockableWindow: window with ID=" + id + " was not found.");
    }

    public void closeDockableWindow(DockableWindow dockableWindow) throws OperationCancelledException {
        if (dockableWindow == null) {
            throw new NullPointerException();
        }
        int id = dockableWindow.getID();
        GraphEditorPanel editor = getGraphEditorPanel(dockableWindow);
        if (editor != null) {
            // handle editor window close
            WorkspaceEntry we = editor.getWorkspaceEntry();

            if (we.isChanged()) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Document '" + we.getTitle() + "' has unsaved changes.\nSave before closing?", "Confirm",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                switch (result) {
                case JOptionPane.YES_OPTION:
                    save(we);
                    break;
                case JOptionPane.NO_OPTION:
                    break;
                default:
                    throw new OperationCancelledException("Operation cancelled by user.");
                }
            }

            if (DockingManager.isMaximized(dockableWindow)) {
                toggleDockableWindowMaximized(dockableWindow.getID());
            }

            if (editorInFocus == editor) {
                toolControlsWindow.setContent(null);
                mainMenu.removeToolsMenu();
                editorInFocus = null;
                setDockableTitle(getPropertyEditor(), TITLE_PROPERTY_EDITOR);
            }

            editorWindows.remove(we, dockableWindow);
            if (editorWindows.get(we).isEmpty()) {
                final Framework framework = Framework.getInstance();
                framework.getWorkspace().close(we);
            }

            if (editorWindows.isEmpty()) {
                DockingManager.registerDockable(documentPlaceholder);
                DockingManager.dock(documentPlaceholder, dockableWindow, DockingConstants.CENTER_REGION);
                utilityWindows.add(documentPlaceholder);
                propertyEditorWindow.removeAll();
                toolControlsWindow.removeAll();
                editorToolsWindow.removeAll();
                setWorkActionsEnableness(false);
            }

            DockingManager.close(dockableWindow);
            DockingManager.unregisterDockable(dockableWindow);
            dockableWindow.setClosed(true);
        } else {
            // handle utility window close
            mainMenu.utilityWindowClosed(id);
            DockingManager.close(dockableWindow);
            dockableWindow.setClosed(true);
        }
    }

    private GraphEditorPanel getGraphEditorPanel(DockableWindow dockableWindow) {
        JComponent content = dockableWindow.getContentPanel().getContent();
        return (content instanceof GraphEditorPanel) ? (GraphEditorPanel) content : null;
    }

    /** For use from Javascript **/
    public void toggleDockableWindow(int id) {
        DockableWindow window = IDToDockableWindowMap.get(id);
        if (window != null)
            toggleDockableWindow(window);
        else
            System.err.println("displayDockableWindow: window with ID=" + id + " was not found.");
    }

    /** For use from Javascript **/
    public void displayDockableWindow(int id) {
        DockableWindow window = IDToDockableWindowMap.get(id);
        if (window != null)
            displayDockableWindow(window);
        else
            System.err.println("displayDockableWindow: window with ID=" + id + " was not found.");
    }

    public void displayDockableWindow(DockableWindow window) {
        DockingManager.display(window);
        window.setClosed(false);
        mainMenu.utilityWindowDisplayed(window.getID());
    }

    public void toggleDockableWindow(DockableWindow window) {
        if (window.isClosed()) {
            displayDockableWindow(window);
        } else {
            try {
                closeDockableWindow(window);
            } catch (OperationCancelledException e) {
            }
        }
    }

    public void saveDockingLayout1() {
        PerspectiveManager pm = (PerspectiveManager) DockingManager.getLayoutManager();
        pm.getCurrentPerspective().cacheLayoutState(rootDockingPort);
        // pm.forceDockableUpdate();
        PerspectiveModel pmodel = new PerspectiveModel(pm.getDefaultPerspective().getPersistentId(),
                pm.getCurrentPerspectiveName(), pm.getPerspectives());
        XMLPersister pers = new XMLPersister();
        try {
            File file = new File(Framework.UILAYOUT_FILE_PATH);
            LogUtils.logMessageLine("Saving UI layout to " + file.getAbsolutePath());
            File parentDir = file.getParentFile();
            if ((parentDir != null) && !parentDir.exists()) {
                parentDir.mkdirs();
            }
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

    public void saveDockingLayout() {
    }

    public void loadDockingLayout1() {
        PerspectiveManager pm = (PerspectiveManager) DockingManager.getLayoutManager();
        XMLPersister pers = new XMLPersister();
        try {
            File file = new File(Framework.UILAYOUT_FILE_PATH);
            if (file.exists()) {
                LogUtils.logMessageLine("Loading UI layout from " + file.getAbsolutePath());
                FileInputStream is = new FileInputStream(file);
                PerspectiveModel pmodel = pers.load(is);
                pm.remove(FLEXDOCK_WORKSPACE);
                pm.setCurrentPerspective(FLEXDOCK_WORKSPACE);
                for (Perspective p : pmodel.getPerspectives()) {
                    pm.add(p, false);
                }
                // pm.reload(rootDockingPort);
                DockingManager.restoreLayout();
                is.close();
                DockingManager.display(outputDockable);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    public void loadDockingLayout() {
        File file = new File(Framework.UILAYOUT_FILE_PATH);
        PersistenceHandler persister = new FilePersistenceHandler(file, XMLPersister.newDefaultInstance());
        PerspectiveManager.setPersistenceHandler(persister);
        try {
            DockingManager.restoreLayout(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (PersistenceException ex) {
            ex.printStackTrace();
        }
    }

    public void shutdown() throws OperationCancelledException {
        closeEditorWindows();

        final Framework framework = Framework.getInstance();
        if (framework.getWorkspace().isChanged() && !framework.getWorkspace().isTemporary()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Current workspace has unsaved changes.\nSave before closing?", "Confirm",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            switch (result) {
            case JOptionPane.YES_OPTION:
                workspaceWindow.saveWorkspace();
                break;
            case JOptionPane.NO_OPTION:
                break;
            default:
                throw new OperationCancelledException("Operation cancelled by user.");
            }
        }
        saveDockingLayout();
        saveWindowGeometryToConfig();
        saveRecentFilesToConfig();

        content.remove(rootDockingPort);

        outputWindow.releaseStream();
        errorWindow.releaseStream();
        setVisible(false);
    }

    public void loadWindowGeometryFromConfig() {
        final Framework framework = Framework.getInstance();
        String maximisedStr = framework.getConfigVar(CONFIG_GUI_MAIN_MAXIMISED);
        String widthStr = framework.getConfigVar(CONFIG_GUI_MAIN_WIDTH);
        String heightStr = framework.getConfigVar(CONFIG_GUI_MAIN_HEIGHT);

        boolean maximised = (maximisedStr == null) ? true : Boolean.parseBoolean(maximisedStr);
        this.setExtendedState(maximised ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);

        DisplayMode mode = this.getGraphicsConfiguration().getDevice().getDisplayMode();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());
        int width = mode.getWidth() - insets.right - insets.left;
        int height = mode.getHeight() - insets.top - insets.bottom;
        if ((widthStr != null) && (heightStr != null)) {
            width = Integer.parseInt(widthStr);
            if (width < MIN_WIDTH) {
                width = MIN_WIDTH;
            }
            height = Integer.parseInt(heightStr);
            if (height < MIN_HEIGHT) {
                height = MIN_HEIGHT;
            }
        }
        this.setSize(width, height);
    }

    public void saveWindowGeometryToConfig() {
        final Framework framework = Framework.getInstance();
        boolean maximised = (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
        framework.setConfigVar(CONFIG_GUI_MAIN_MAXIMISED, Boolean.toString(maximised));
        framework.setConfigVar(CONFIG_GUI_MAIN_WIDTH, Integer.toString(getWidth()));
        framework.setConfigVar(CONFIG_GUI_MAIN_HEIGHT, Integer.toString(getHeight()));
    }

    public void loadRecentFilesFromConfig() {
        final Framework framework = Framework.getInstance();
        lastSavePath = framework.getConfigVar(CONFIG_GUI_MAIN_LAST_SAVE_PATH);
        lastOpenPath = framework.getConfigVar(CONFIG_GUI_MAIN_LAST_OPEN_PATH);
        for (int i = 0; i < CommonEditorSettings.getRecentCount(); i++) {
            String entry = framework.getConfigVar(CONFIG_GUI_MAIN_RECENT_FILE + i);
            pushRecentFile(entry, false);
        }
        updateRecentFilesMenu();
    }

    public void saveRecentFilesToConfig() {
        final Framework framework = Framework.getInstance();
        if (lastSavePath != null) {
            framework.setConfigVar(CONFIG_GUI_MAIN_LAST_SAVE_PATH, lastSavePath);
        }
        if (lastOpenPath != null) {
            framework.setConfigVar(CONFIG_GUI_MAIN_LAST_OPEN_PATH, lastOpenPath);
        }
        int recentCount = CommonEditorSettings.getRecentCount();
        String[] tmp = recentFiles.toArray(new String[recentCount]);
        for (int i = 0; i < recentCount; i++) {
            framework.setConfigVar(CONFIG_GUI_MAIN_RECENT_FILE + i, tmp[i]);
        }
    }

    public void pushRecentFile(String fileName, boolean updateMenu) {
        if ((fileName != null) && (new File(fileName).exists())) {
            // Remove previous entry of the fileName
            recentFiles.remove(fileName);
            // Make sure there is not too many entries
            int recentCount = CommonEditorSettings.getRecentCount();
            for (String entry : new ArrayList<String>(recentFiles)) {
                if (recentFiles.size() < recentCount) {
                    break;
                }
                recentFiles.remove(entry);
            }
            // Add the fileName if possible
            if (recentFiles.size() < recentCount) {
                recentFiles.add(fileName);
            }
        }
        if (updateMenu) {
            updateRecentFilesMenu();
        }
    }

    public void clearRecentFilesMenu() {
        recentFiles.clear();
        mainMenu.setRecentMenu(new ArrayList<String>(recentFiles));
    }

    public void updateRecentFilesMenu() {
        mainMenu.setRecentMenu(new ArrayList<String>(recentFiles));
    }

    public void createWork() throws OperationCancelledException {
        createWork(Path.<String> empty());
    }

    public void createWork(Path<String> path) throws OperationCancelledException {
        final Framework framework = Framework.getInstance();
        CreateWorkDialog dialog = new CreateWorkDialog(this);
        dialog.setVisible(true);
        if (dialog.getModalResult() == 1) {
            ModelDescriptor info = dialog.getSelectedModel();
            try {
                MathModel mathModel = info.createMathModel();
                String title = dialog.getModelTitle();
                if (!title.isEmpty()) {
                    mathModel.setTitle(title);
                }
                if (dialog.createVisualSelected()) {
                    VisualModelDescriptor v = info.getVisualModelDescriptor();
                    if (v == null) {
                        throw new VisualModelInstantiationException(
                                "visual model is not defined for '" + info.getDisplayName() + "'.");
                    }
                    VisualModel visualModel = v.create(mathModel);
                    ModelEntry me = new ModelEntry(info, visualModel);
                    framework.getWorkspace().add(path, title, me, false, dialog.openInEditorSelected());
                } else {
                    ModelEntry me = new ModelEntry(info, mathModel);
                    framework.getWorkspace().add(path, title, me, false, false);
                }
            } catch (VisualModelInstantiationException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Visual model could not be created: " + e.getMessage()
                                + "\n\nPlease see the Problems window for details.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            throw new OperationCancelledException("Create operation cancelled by user.");
        }
    }

    public void requestFocus(GraphEditorPanel sender) {
        sender.requestFocusInWindow();
        if (editorInFocus != sender) {
            editorInFocus = sender;

            toolControlsWindow.setContent(sender.getToolBox());
            editorToolsWindow.setContent(sender.getToolBox().getControlPanel());
            mainMenu.setMenuForWorkspaceEntry(editorInFocus.getWorkspaceEntry());
            sender.updatePropertyView();

            final Framework framework = Framework.getInstance();

            framework.deleteJavaScriptProperty("visualModel", framework.getJavaScriptGlobalScope());
            framework.setJavaScriptProperty("visualModel", sender.getModel(), framework.getJavaScriptGlobalScope(),
                    true);

            framework.deleteJavaScriptProperty("model", framework.getJavaScriptGlobalScope());
            framework.setJavaScriptProperty("model", sender.getModel().getMathModel(),
                    framework.getJavaScriptGlobalScope(), true);
        }
        editorInFocus.requestFocus();
    }

    private void printCause(Throwable e) {
        e.printStackTrace();
        System.err.println("-------------" + e);
        if (e.getCause() != null) {
            printCause(e.getCause());
        }
    }

    public JFileChooser createOpenDialog(String title, boolean multiSelection, Importer[] importers) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setMultiSelectionEnabled(multiSelection);
        fc.setDialogTitle(title);
        // Set working directory
        if (lastOpenPath != null) {
            fc.setCurrentDirectory(new File(lastOpenPath));
        } else if (lastSavePath != null) {
            fc.setCurrentDirectory(new File(lastSavePath));
        }
        // Set file filters
        fc.setAcceptAllFileFilterUsed(false);
        if (importers == null) {
            fc.setFileFilter(FileFilters.DOCUMENT_FILES);
        } else {
            for (Importer importer : importers) {
                fc.addChoosableFileFilter(new ImporterFileFilter(importer));
            }
        }
        return fc;
    }

    public JFileChooser createSaveDialog(String title, File file, Exporter exporter) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setDialogTitle(title);
        // Set working directory
        fc.setSelectedFile(file);
        if (file.exists()) {
            fc.setCurrentDirectory(file.getParentFile());
        } else if (lastSavePath != null) {
            fc.setCurrentDirectory(new File(lastSavePath));
        } else if (lastOpenPath != null) {
            fc.setCurrentDirectory(new File(lastOpenPath));
        }
        // Set file filters
        fc.setAcceptAllFileFilterUsed(false);
        if (exporter == null) {
            fc.setFileFilter(FileFilters.DOCUMENT_FILES);
        } else {
            fc.setFileFilter(new ExporterFileFilter(exporter));
        }
        return fc;
    }

    private String getValidSavePath(JFileChooser fc, Exporter exporter) throws OperationCancelledException {
        String path = null;
        while (true) {
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                path = fc.getSelectedFile().getPath();
                if (exporter == null) {
                    if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                        path += FileFilters.DOCUMENT_EXTENSION;
                    }
                } else {
                    if (!path.endsWith(exporter.getExtenstion())) {
                        path += exporter.getExtenstion();
                    }
                }
                File f = new File(path);
                if (!f.exists()) {
                    break;
                }
                if (JOptionPane.showConfirmDialog(this,
                        "The file '" + f.getName() + "' already exists. Do you want to overwrite it?", "Confirm",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    break;
                }
            } else {
                throw new OperationCancelledException("Save operation cancelled by user.");
            }
        }
        return path;
    }

    public void openWork() throws OperationCancelledException {
        JFileChooser fc = createOpenDialog("Open work file(s)", true, null);
        if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
            final HashSet<WorkspaceEntry> newWorkspaceEntries = new HashSet<>();
            for (File f : fc.getSelectedFiles()) {
                String path = f.getPath();
                if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                    path += FileFilters.DOCUMENT_EXTENSION;
                    f = new File(path);
                }
                WorkspaceEntry we = openWork(f);
                if (we != null) {
                    newWorkspaceEntries.add(we);
                }
            }
            // FIXME: Go through the newly open works and update their zoom, in
            // case tabs appeared and changed the viewport size.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (WorkspaceEntry we : newWorkspaceEntries) {
                        zoomFitWorkspaceEntryEditors(we);
                    }
                }

                private void zoomFitWorkspaceEntryEditors(WorkspaceEntry we) {
                    for (DockableWindow w : editorWindows.get(we)) {
                        GraphEditor editor = getGraphEditorPanel(w);
                        if (editor != null) {
                            editor.zoomFit();
                        }
                    }
                }
            });
        } else {
            throw new OperationCancelledException("Open operation cancelled by user.");
        }
    }

    public WorkspaceEntry openWork(File f) {
        final Framework framework = Framework.getInstance();
        WorkspaceEntry we = null;
        if (framework.checkFile(f, null)) {
            try {
                we = framework.getWorkspace().open(f, false);
                if (we.getModelEntry().isVisual()) {
                    createEditorWindow(we);
                }
                pushRecentFile(f.getPath(), true);
                lastOpenPath = f.getParent();
            } catch (DeserialisationException e) {
                JOptionPane
                        .showMessageDialog(this,
                                "A problem was encountered while trying to load '" + f.getPath() + "'.\n"
                                        + "Please see Problems window for details.",
                                "Load failed", JOptionPane.ERROR_MESSAGE);
                printCause(e);
            }
        }
        return we;
    }

    public void mergeWork() throws OperationCancelledException {
        JFileChooser fc = createOpenDialog("Merge work file(s)", true, null);
        if (fc.showDialog(this, "Merge") == JFileChooser.APPROVE_OPTION) {
            for (File f : fc.getSelectedFiles()) {
                String path = f.getPath();
                if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                    path += FileFilters.DOCUMENT_EXTENSION;
                    f = new File(path);
                }
                mergeWork(f);
            }
        } else {
            throw new OperationCancelledException("Merge operation cancelled by user.");
        }
    }

    public void mergeWork(File f) {
        if (editorInFocus == null) {
            openWork(f);
        } else {
            try {
                final Framework framework = Framework.getInstance();
                WorkspaceEntry we = editorInFocus.getWorkspaceEntry();
                framework.getWorkspace().merge(we, f);
            } catch (DeserialisationException e) {
                JOptionPane.showMessageDialog(this,
                        "A problem was encountered while trying to merge '" + f.getPath()
                                + "'.\nPlease see Problems window for details.",
                        "Load failed", JOptionPane.ERROR_MESSAGE);
                printCause(e);
            }
        }
    }

    public void saveWork() throws OperationCancelledException {
        if (editorInFocus != null) {
            save(editorInFocus.getWorkspaceEntry());
        } else {
            System.out.println("No editor in focus");
        }
    }

    public void saveWorkAs() throws OperationCancelledException {
        if (editorInFocus != null) {
            saveAs(editorInFocus.getWorkspaceEntry());
        } else {
            System.err.println("No editor in focus");
        }
    }

    public void save(WorkspaceEntry we) throws OperationCancelledException {
        if (!we.getFile().exists()) {
            saveAs(we);
        } else {
            try {
                if (we.getModelEntry() != null) {
                    final Framework framework = Framework.getInstance();
                    framework.save(we.getModelEntry(), we.getFile().getPath());
                } else {
                    throw new RuntimeException(
                            "Cannot save workspace entry - it does not have an associated Workcraft model.");
                }
            } catch (SerialisationException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage(), "Model export failed", JOptionPane.ERROR_MESSAGE);
            }
            we.setChanged(false);
            refreshWorkspaceEntryTitle(we, true);
            lastSavePath = we.getFile().getParent();
            pushRecentFile(we.getFile().getPath(), true);
        }
    }

    private static String removeSpecialFileNameCharacters(String fileName) {
        return fileName.replace('\\', '_').replace('/', '_').replace(':', '_').replace('"', '_').replace('<', '_')
                .replace('>', '_').replace('|', '_');
    }

    private String getFileNameForCurrentWork() {
        String fileName = TITLE_PLACEHOLDER;
        if (editorInFocus != null) {
            WorkspaceEntry we = editorInFocus.getWorkspaceEntry();
            if (we != null) {
                fileName = we.getTitle();
            }
        }
        if ((fileName == null) || fileName.isEmpty()) {
            fileName = "Untitled";
        }
        return removeSpecialFileNameCharacters(fileName);
    }

    public void saveAs(WorkspaceEntry we) throws OperationCancelledException {
        File file = we.getFile();
        if (file == null) {
            file = new File(getFileNameForCurrentWork());
        }
        JFileChooser fc = createSaveDialog("Save work as", file, null);
        String path = getValidSavePath(fc, null);
        try {
            File destination = new File(path);
            final Framework framework = Framework.getInstance();
            Workspace ws = framework.getWorkspace();

            Path<String> wsFrom = we.getWorkspacePath();
            Path<String> wsTo = ws.getWorkspacePath(destination);
            if (wsTo == null) {
                wsTo = ws.tempMountExternalFile(destination);
            }
            if (wsFrom != wsTo) {
                ws.moved(wsFrom, wsTo);
            }

            if (we.getModelEntry() != null) {
                framework.save(we.getModelEntry(), path);
            } else {
                throw new RuntimeException(
                        "Cannot save workspace entry - it does not have an associated Workcraft model.");
            }
            we.setChanged(false);
            refreshWorkspaceEntryTitle(we, true);
            lastSavePath = we.getFile().getParent();
            pushRecentFile(we.getFile().getPath(), true);
        } catch (SerialisationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Model export failed", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void importFrom() {
        final Framework framework = Framework.getInstance();
        Collection<PluginInfo<? extends Importer>> importerInfo = framework.getPluginManager()
                .getPlugins(Importer.class);
        Importer[] importers = new Importer[importerInfo.size()];
        int cnt = 0;
        for (PluginInfo<? extends Importer> info : importerInfo) {
            importers[cnt++] = info.getSingleton();
        }

        JFileChooser fc = createOpenDialog("Import model(s)", true, importers);
        if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
            for (File f : fc.getSelectedFiles()) {
                importFrom(f, importers);
            }
        }
    }

    public void importFrom(File f, Importer[] importers) {
        final Framework framework = Framework.getInstance();
        if (framework.checkFile(f, null)) {
            for (Importer importer : importers) {
                if (importer.accept(f)) {
                    try {
                        ModelEntry me = Import.importFromFile(importer, f);
                        String title = FileUtils.getFileNameWithoutExtension(f);
                        me.getModel().setTitle(title);
                        boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
                        framework.getWorkspace().add(Path.<String> empty(), f.getName(), me, false, openInEditor);
                        lastOpenPath = f.getParent();
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
    }

    public void runTool(Tool tool) {
        Tools.run(editorInFocus.getWorkspaceEntry(), tool);
    }

    public void export(Exporter exporter) throws OperationCancelledException {
        String title = "Export as " + exporter.getDescription();
        File file = new File(getFileNameForCurrentWork());
        JFileChooser fc = createSaveDialog(title, file, exporter);
        String path = getValidSavePath(fc, exporter);
        Task<Object> exportTask = new Export.ExportTask(exporter, editorInFocus.getModel(), path);
        final Framework framework = Framework.getInstance();
        framework.getTaskManager().queue(exportTask, "Exporting " + title, new TaskFailureNotifier());
        lastSavePath = fc.getCurrentDirectory().getPath();
    }

    private String getTitle(WorkspaceEntry we, VisualModel model) {
        String prefix = we.isChanged() ? "*" : TITLE_PLACEHOLDER;
        String suffix = null;
        switch (CommonEditorSettings.getTitleStyle()) {
        case LONG:
            suffix = " - " + model.getDisplayName();
            break;
        case SHORT:
            suffix = " [" + model.getShortName() + "]";
            break;
        default:
            suffix = TITLE_PLACEHOLDER;
            break;
        }
        return prefix + we.getTitle() + suffix;
    }

    public void refreshWorkspaceEntryTitle(WorkspaceEntry we, boolean updateHeaders) {
        for (DockableWindow w : editorWindows.get(we)) {
            final GraphEditorPanel editor = getCurrentEditor();
            String title = getTitle(we, editor.getModel());
            w.setTitle(title);
        }
        if (updateHeaders) {
            DockableWindow.updateHeaders(rootDockingPort, getDefaultActionListener());
        }
    }

    public void refreshWorkspaceEntryTitles() {
        for (WorkspaceEntry we : editorWindows.keySet()) {
            refreshWorkspaceEntryTitle(we, false);
        }
        DockableWindow.updateHeaders(rootDockingPort, getDefaultActionListener());
    }

    public void setDockableTitle(DockableWindow dockable, String title) {
        if (dockable != null) {
            dockable.setTitle(title);
            DockableWindow.updateHeaders(rootDockingPort, getDefaultActionListener());
        }
    }

    public List<GraphEditorPanel> getEditors(WorkspaceEntry we) {
        ArrayList<GraphEditorPanel> result = new ArrayList<GraphEditorPanel>();
        for (DockableWindow window : editorWindows.get(we)) {
            result.add(getGraphEditorPanel(window));
        }
        return result;
    }

    public GraphEditorPanel getCurrentEditor() {
        return editorInFocus;
    }

    public void repaintCurrentEditor() {
        if (editorInFocus != null) {
            editorInFocus.repaint();
        }
    }

    public void closeActiveEditor() throws OperationCancelledException {
        for (WorkspaceEntry k : editorWindows.keySet()) {
            for (DockableWindow w : editorWindows.get(k)) {
                DockableWindowContentPanel contentPanel = w.getContentPanel();
                if ((contentPanel != null) && (contentPanel.getContent() == editorInFocus)) {
                    closeDockableWindow(w);
                    return;
                }
            }
        }
    }

    public void closeEditorWindows() throws OperationCancelledException {
        LinkedHashSet<DockableWindow> windowsToClose = new LinkedHashSet<DockableWindow>();

        for (WorkspaceEntry k : editorWindows.keySet()) {
            for (DockableWindow w : editorWindows.get(k)) {
                windowsToClose.add(w);
            }
        }

        for (DockableWindow w : windowsToClose) {
            if (DockingManager.isMaximized(w)) {
                toggleDockableWindowMaximized(w.getID());
            }
        }

        for (DockableWindow w : windowsToClose) {
            closeDockableWindow(w);
        }
    }

    public void closeEditors(WorkspaceEntry openFile) throws OperationCancelledException {
        for (DockableWindow w : new ArrayList<DockableWindow>(editorWindows.get(openFile))) {
            closeDockableWindow(w);
        }
    }

    public void undo() {
        if (editorInFocus != null) {
            editorInFocus.getWorkspaceEntry().undo();
            editorInFocus.forceRedraw();
        }
    }

    public void redo() {
        if (editorInFocus != null) {
            editorInFocus.getWorkspaceEntry().redo();
            editorInFocus.forceRedraw();
        }
    }

    public void cut() {
        if (editorInFocus != null) {
            editorInFocus.getWorkspaceEntry().cut();
            editorInFocus.forceRedraw();
        }
    }

    public void copy() {
        if (editorInFocus != null) {
            editorInFocus.getWorkspaceEntry().copy();
            editorInFocus.forceRedraw();
        }
    }

    public void paste() {
        if (editorInFocus != null) {
            editorInFocus.getWorkspaceEntry().paste();
            editorInFocus.forceRedraw();
        }
    }

    public void delete() {
        if (editorInFocus != null) {
            editorInFocus.getWorkspaceEntry().delete();
            editorInFocus.forceRedraw();
        }
    }

    public void selectAll() {
        if (editorInFocus != null) {
            VisualModel visualModel = editorInFocus.getWorkspaceEntry().getModelEntry().getVisualModel();
            visualModel.selectAll();
        }
    }

    public void selectNone() {
        if (editorInFocus != null) {
            VisualModel visualModel = editorInFocus.getWorkspaceEntry().getModelEntry().getVisualModel();
            visualModel.selectNone();
        }
    }

    public void selectInverse() {
        if (editorInFocus != null) {
            VisualModel visualModel = editorInFocus.getWorkspaceEntry().getModelEntry().getVisualModel();
            visualModel.selectInverse();
        }
    }

    public void editSettings() {
        SettingsEditorDialog dlg = new SettingsEditorDialog(this);
        dlg.setModal(true);
        dlg.setResizable(true);
        dlg.setVisible(true);
        refreshWorkspaceEntryTitles();
    }

    public void resetLayout() {
        if (JOptionPane.showConfirmDialog(this,
                "This will reset the GUI to the default layout.\n\n" + "Are you sure you want to do this?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (JOptionPane.showConfirmDialog(this,
                    "This action requires GUI restart.\n\n"
                            + "This will cause the visual editor windows to be closed.\n\nProceed?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    final Framework framework = Framework.getInstance();
                    framework.shutdownGUI();
                    new File(Framework.UILAYOUT_FILE_PATH).delete();
                    framework.startGUI();
                } catch (OperationCancelledException e) {
                }
            }
        }
    }

    public MainMenu getMainMenu() {
        return mainMenu;
    }

    public DockableWindow getPropertyEditor() {
        return propertyEditorDockable;
    }

    public PropertyEditorWindow getPropertyView() {
        return propertyEditorWindow;
    }

    public WorkspaceWindow getWorkspaceView() {
        return workspaceWindow;
    }

}

class ImporterFileFilter extends javax.swing.filechooser.FileFilter {
    private Importer importer;

    ImporterFileFilter(Importer importer) {
        this.importer = importer;
    }

    public boolean accept(File f) {
        return f.isDirectory() || importer.accept(f);
    }

    public String getDescription() {
        return importer.getDescription();
    }
}

class ExporterFileFilter extends javax.swing.filechooser.FileFilter {
    private Exporter exporter;

    ExporterFileFilter(Exporter exporter) {
        this.exporter = exporter;
    }

    public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(exporter.getExtenstion());
    }

    public String getDescription() {
        return exporter.getDescription();
    }
}
