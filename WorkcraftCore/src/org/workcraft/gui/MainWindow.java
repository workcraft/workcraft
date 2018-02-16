package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
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
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.MenuBarUI;

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
import org.flexdock.perspective.persist.xml.XMLPersister;
import org.flexdock.plaf.common.border.ShadowBorder;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceConstants.TabContentPaneBorderKind;
import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.commands.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.MultiBorderLayout;
import org.workcraft.gui.propertyeditor.SettingsEditorDialog;
import org.workcraft.gui.tasks.TaskFailureNotifier;
import org.workcraft.gui.tasks.TaskManagerWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExportTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Commands;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.ExceptionUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.Import;
import org.workcraft.util.ListMap;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
    private static final String FLEXDOCK_WORKSPACE = "defaultWorkspace";
    private static final String FLEXDOCK_DOCKING_PORT = "defaultDockingPort";

    private static final String CONFIG_GUI_MAIN_MAXIMISED = "gui.main.maximised";
    private static final String CONFIG_GUI_MAIN_WIDTH = "gui.main.width";
    private static final String CONFIG_GUI_MAIN_HEIGHT = "gui.main.height";
    private static final String CONFIG_GUI_MAIN_LAST_DIRECTORY = "gui.main.lastDirectory";
    private static final String CONFIG_GUI_MAIN_RECENT_FILE = "gui.main.recentFile";
    private static final String CONFIG_GUI_MAIN_TOOLBAR_GLOBAL_VISIBILITY = "gui.main.toolbar.global.visibility";
    private static final String CONFIG_GUI_MAIN_TOOLBAR_GLOBAL_POSITION = "gui.main.toolbar.global.position";
    private static final String CONFIG_GUI_MAIN_TOOLBAR_MODEL_VISIBILITY = "gui.main.toolbar.model.visibility";
    private static final String CONFIG_GUI_MAIN_TOOLBAR_MODEL_POSITION = "gui.main.toolbar.model.position";
    private static final String CONFIG_GUI_MAIN_TOOLBAR_CONTROL_VISIBILITY = "gui.main.toolbar.tool.visibility";
    private static final String CONFIG_GUI_MAIN_TOOLBAR_CONTROL_POSITION = "gui.main.toolbar.tool.position";

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

    private static final String DIALOG_CLOSE_WORK = "Close work";
    private static final String DIALOG_SAVE_WORK = "Save work";
    private static final String DIALOG_RESET_LAYOUT = "Reset layout";

    private final ScriptedActionListener defaultActionListener = new ScriptedActionListener() {
        @Override
        public void actionPerformed(Action action) {
            action.run();
        }
    };

    private MultiBorderLayout layout;
    private JPanel content;

    private DefaultDockingPort rootDockingPort;
    private DockableWindow outputDockable;
    private DockableWindow propertyEditorDockable;
    private DockableWindow toolControlsDockable;
    private DockableWindow documentPlaceholder;

    private OutputWindow outputWindow;
    private ErrorWindow errorWindow;
    private JavaScriptWindow javaScriptWindow;
    private PropertyEditorWindow propertyEditorWindow;
    private ToolControlsWindow toolControlsWindow;
    private WorkspaceWindow workspaceWindow;

    private final ListMap<WorkspaceEntry, DockableWindow> editorWindows = new ListMap<>();
    private final LinkedList<DockableWindow> utilityWindows = new LinkedList<>();

    private GraphEditorPanel editorInFocus;
    private MainMenu mainMenu;
    private ToolBar globalToolbar;
    private JToolBar modelToolbar;
    private JToolBar controlToolbar;

    private String lastDirectory = null;
    private final LinkedHashSet<String> recentFiles = new LinkedHashSet<>();

    private int dockableIDCounter = 0;
    private final HashMap<Integer, DockableWindow> idToDockableWindowMap = new HashMap<>();

    public MainWindow() {
        super();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Framework.getInstance().shutdown();
            }
        });
    }

    private void createWindows() {
        workspaceWindow = new WorkspaceWindow();
        workspaceWindow.setVisible(true);

        outputWindow = new OutputWindow();
        outputWindow.captureStream();

        errorWindow = new ErrorWindow();
        errorWindow.captureStream();

        javaScriptWindow = new JavaScriptWindow();

        propertyEditorWindow = new PropertyEditorWindow();
        toolControlsWindow = new ToolControlsWindow();
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
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
        idToDockableWindowMap.put(id, dockable);
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
        final GraphEditorPanel editor = new GraphEditorPanel(we);
        String title = we.getTitleAndModel();
        final DockableWindow editorWindow;
        int options = DockableWindowContentPanel.CLOSE_BUTTON | DockableWindowContentPanel.MAXIMIZE_BUTTON;
        if (editorWindows.isEmpty()) {
            editorWindow = createDockableWindow(editor, title, documentPlaceholder, options,
                    DockingConstants.CENTER_REGION, "Document" + we.getWorkspacePath());
            DockingManager.close(documentPlaceholder);
            DockingManager.unregisterDockable(documentPlaceholder);
            utilityWindows.remove(documentPlaceholder);
        } else {
            unmaximiseAllDockableWindows();
            DockableWindow firstEditorWindow = editorWindows.values().iterator().next().iterator().next();
            editorWindow = createDockableWindow(editor, title, firstEditorWindow, options,
                    DockingConstants.CENTER_REGION, "Document" + we.getWorkspacePath());
        }
        EditorWindowTabListener tabListener = new EditorWindowTabListener(editor);
        editorWindow.addTabListener(tabListener);
        editorWindows.put(we, editorWindow);
        requestFocus(editor);
        setWorkActionsEnableness(true);
        editor.zoomFit();
        return editor;
    }

    private void unmaximiseAllDockableWindows() {
        for (LinkedList<DockableWindow> dockableWindows: editorWindows.values()) {
            for (DockableWindow dockableWindow: dockableWindows) {
                if (dockableWindow.isMaximized()) {
                    DockingManager.toggleMaximized(dockableWindow);
                    dockableWindow.setMaximized(false);
                }
            }
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

    public void startup() {
        MainWindowIconManager.apply(this);
        JDialog.setDefaultLookAndFeelDecorated(true);
        UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
        setTitle(TITLE_WORKCRAFT);

        // Create main menu.
        mainMenu = new MainMenu(this);
        MenuBarUI menuUI = mainMenu.getUI();
        setJMenuBar(mainMenu);

        // Tweak look-and-feel.
        SilverOceanTheme.enable();
        LookAndFeelHelper.setDefaultLookAndFeel();
        SwingUtilities.updateComponentTreeUI(this);
        if (DesktopApi.getOs().isMac()) {
            // Menu UI needs to be restored for OSX (global menu Look-and-Feel).
            mainMenu.setUI(menuUI);
        }

        // Create content panel and docking ports.
        layout = new MultiBorderLayout();
        content = new JPanel(layout);
        setContentPane(content);
        rootDockingPort = new DefaultDockingPort(FLEXDOCK_DOCKING_PORT);
        content.add(rootDockingPort, BorderLayout.CENTER);
        StandardBorderManager borderManager = new StandardBorderManager(new ShadowBorder());
        rootDockingPort.setBorderManager(borderManager);

        // Create toolbars.
        globalToolbar = new ToolBar(this);
        modelToolbar = new JToolBar("Model tools");
        controlToolbar = new JToolBar("Tool controls");
        mainMenu.registerToolbar(globalToolbar);
        mainMenu.registerToolbar(modelToolbar);
        mainMenu.registerToolbar(controlToolbar);
        loadToolbarParametersFromConfig();

        // Create dockable windows.
        createWindows();
        createDockingLayout();
        loadWindowGeometryFromConfig();
        loadRecentFilesFromConfig();

        // Display window in its default state.
        setVisible(true);
        DockableWindow.updateHeaders(rootDockingPort, getDefaultActionListener());
        DockingManager.display(outputDockable);
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
                GUI.createIconFromSVG("images/icon.svg");
            }
        }).start();

        setWorkActionsEnableness(false);
    }

    private void setWorkActionsEnableness(boolean enable) {
        mainMenu.setExportMenuState(enable);
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

    public void updateMainMenuState(boolean canModify) {
        mainMenu.updateCommandsMenuState(canModify);
    }

    public ScriptedActionListener getDefaultActionListener() {
        return defaultActionListener;
    }

    public void toggleDockableWindowMaximized(int id) {
        DockableWindow dockableWindow = idToDockableWindowMap.get(id);
        if (dockableWindow != null) {
            DockingManager.toggleMaximized(dockableWindow);
            dockableWindow.setMaximized(!dockableWindow.isMaximized());
        } else {
            System.err.println("toggleDockableWindowMaximized: window with ID=" + id + " was not found.");
        }
    }

    public void closeDockableWindow(int id) throws OperationCancelledException {
        DockableWindow dockableWindow = idToDockableWindowMap.get(id);
        if (dockableWindow != null) {
            closeDockableWindow(dockableWindow);
        } else {
            System.err.println("closeDockableWindow: window with ID=" + id + " was not found.");
        }
    }

    public void closeDockableWindow(DockableWindow dockableWindow) throws OperationCancelledException {
        if (dockableWindow == null) {
            throw new NullPointerException();
        }
        GraphEditorPanel editor = getGraphEditorPanel(dockableWindow);
        if (editor != null) {
            closeDocableEditorWindow(dockableWindow, editor);
        } else {
            closeDockableUtilityWindow(dockableWindow);
        }
    }

    private void closeDocableEditorWindow(DockableWindow dockableWindow, GraphEditorPanel editor)
            throws OperationCancelledException {

        WorkspaceEntry we = editor.getWorkspaceEntry();
        if (we.isChanged()) {
            String title = we.getTitle();
            int result = JOptionPane.showConfirmDialog(this,
                    "Document '" + title + "' has unsaved changes.\n" + "Save before closing?",
                    DIALOG_CLOSE_WORK, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            switch (result) {
            case JOptionPane.YES_OPTION:
                saveWork(we);
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
            mainMenu.removeCommandsMenu();
            editorInFocus = null;
            setPropertyEditorTitle(TITLE_PROPERTY_EDITOR);
        }

        editorWindows.remove(we, dockableWindow);
        if (editorWindows.get(we).isEmpty()) {
            final Framework framework = Framework.getInstance();
            framework.closeWork(we);
        }

        if (editorWindows.isEmpty()) {
            DockingManager.registerDockable(documentPlaceholder);
            DockingManager.dock(documentPlaceholder, dockableWindow, DockingConstants.CENTER_REGION);
            utilityWindows.add(documentPlaceholder);
            setWorkActionsEnableness(false);
            modelToolbar.removeAll();
            controlToolbar.removeAll();
            propertyEditorWindow.clearObject();
            toolControlsWindow.setContent(null);
            displayDockableWindow(propertyEditorDockable);
            closeDockableWindow(toolControlsDockable);
            setPropertyEditorTitle(TITLE_PROPERTY_EDITOR);
        }

        DockingManager.close(dockableWindow);
        DockingManager.unregisterDockable(dockableWindow);
        dockableWindow.setClosed(true);
    }

    private void setToolbarVisibility(JToolBar toolbar, boolean visibility) {
        mainMenu.setToolbarVisibility(toolbar, visibility);
        toolbar.setVisible(visibility);
    }

    private void closeDockableUtilityWindow(DockableWindow dockableWindow) {
        mainMenu.setWindowVisibility(dockableWindow.getID(), false);
        DockingManager.close(dockableWindow);
        dockableWindow.setClosed(true);
    }

    public void displayDockableWindow(DockableWindow window) {
        DockingManager.display(window);
        window.setClosed(false);
        mainMenu.setWindowVisibility(window.getID(), true);
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

    public DisplayMode getDisplayMode() {
        return getGraphicsConfiguration().getDevice().getDisplayMode();
    }

    private GraphEditorPanel getGraphEditorPanel(DockableWindow dockableWindow) {
        JComponent content = dockableWindow.getContentPanel().getContent();
        return (content instanceof GraphEditorPanel) ? (GraphEditorPanel) content : null;
    }

    private void createDockingLayout() {
        // Setup docking manger (should go before perspective manager for correctly restoring window position).
        EffectsManager.setPreview(new GhostPreview());
        DockingManager.setFloatingEnabled(true);
        DockingManager.setAutoPersist(true);
        PropertyManager.getDockingPortRoot().setTabPlacement(SwingConstants.TOP);

        // Set default perspective.
        PerspectiveManager pm = PerspectiveManager.getInstance();
        pm.add(new Perspective(FLEXDOCK_WORKSPACE, FLEXDOCK_WORKSPACE));
        pm.setCurrentPerspective(FLEXDOCK_WORKSPACE, true);

        // Configure perspective manager (should go after docking manager for correctly restoring window position).
        PerspectiveManager.setRestoreFloatingOnLoad(true);
        File file = new File(Framework.UILAYOUT_FILE_PATH);
        PersistenceHandler persister = new FilePersistenceHandler(file, XMLPersister.newDefaultInstance());
        PerspectiveManager.setPersistenceHandler(persister);

        try {
            DockingManager.loadLayoutModel();
        } catch (IOException | PersistenceException e) {
            LogUtils.logWarning("Window layout could not be loaded from '" + file.getAbsolutePath() + "'.");
        }

        float xSplit = 0.888f;
        float ySplit = 0.8f;

        outputDockable = createDockableWindow(outputWindow, TITLE_OUTPUT, DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.SOUTH_REGION, ySplit);

        DockableWindow erroDockable = createDockableWindow(errorWindow, TITLE_PROBLEMS, outputDockable,
                DockableWindowContentPanel.CLOSE_BUTTON);

        DockableWindow javaScriptDockable = createDockableWindow(javaScriptWindow, TITLE_JAVASCRIPT, outputDockable,
                DockableWindowContentPanel.CLOSE_BUTTON);

        DockableWindow tasksDockable = createDockableWindow(new TaskManagerWindow(), TITLE_TASKS, outputDockable,
                DockableWindowContentPanel.CLOSE_BUTTON);

        DockableWindow workspaceDockable = createDockableWindow(workspaceWindow, TITLE_WORKSPACE,
                DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.EAST_REGION, xSplit);

        propertyEditorDockable = createDockableWindow(propertyEditorWindow, TITLE_PROPERTY_EDITOR,
                workspaceDockable, DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.NORTH_REGION, ySplit);

        toolControlsDockable = createDockableWindow(toolControlsWindow, TITLE_TOOL_CONTROLS,
                propertyEditorDockable, DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.SOUTH_REGION, 0.4f);

        documentPlaceholder = createDockableWindow(new DocumentPlaceholder(), TITLE_PLACEHOLDER, null, outputDockable,
                0, DockingManager.NORTH_REGION, ySplit, "DocumentPlaceholder");

        registerUtilityWindow(outputDockable);
        registerUtilityWindow(erroDockable);
        registerUtilityWindow(javaScriptDockable);
        registerUtilityWindow(tasksDockable);
        registerUtilityWindow(propertyEditorDockable);
        registerUtilityWindow(toolControlsDockable);
        registerUtilityWindow(workspaceDockable);

        // FIXME: Restoring previously saved layout does not work as expected:
        // "default" and "restored" layouts interfere with each other, which does not look nice.
        //DockingManager.restoreLayout();
    }

    public void shutdown() throws OperationCancelledException {
        closeEditorWindows();

        final Framework framework = Framework.getInstance();
        if (framework.getWorkspace().isChanged() && !framework.getWorkspace().isTemporary()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Current workspace has unsaved changes.\n" + "Save before closing?",
                    DIALOG_CLOSE_WORK, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

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
        saveWindowGeometryToConfig();
        saveRecentFilesToConfig();
        saveToolbarParametersToConfig();

        content.remove(rootDockingPort);

        outputWindow.releaseStream();
        errorWindow.releaseStream();
        setVisible(false);
    }

    private void loadToolbarParametersFromConfig() {
        loadToolbarParametersFromConfig(globalToolbar,
                CONFIG_GUI_MAIN_TOOLBAR_GLOBAL_VISIBILITY,
                CONFIG_GUI_MAIN_TOOLBAR_GLOBAL_POSITION);

        loadToolbarParametersFromConfig(modelToolbar,
                CONFIG_GUI_MAIN_TOOLBAR_MODEL_VISIBILITY,
                CONFIG_GUI_MAIN_TOOLBAR_MODEL_POSITION);

        loadToolbarParametersFromConfig(controlToolbar,
                CONFIG_GUI_MAIN_TOOLBAR_CONTROL_VISIBILITY,
                CONFIG_GUI_MAIN_TOOLBAR_CONTROL_POSITION);
    }

    private void loadToolbarParametersFromConfig(JToolBar toolbar, String keyVisibility, String keyPosition) {
        final Framework framework = Framework.getInstance();

        boolean visible = true;
        String visibleVal = framework.getConfigVar(keyVisibility, false);
        if (visibleVal != null) {
            visible = Boolean.valueOf(visibleVal);
        }
        setToolbarVisibility(toolbar, visible);

        String position = framework.getConfigVar(keyPosition, false);
        if (position == null) {
            position = BorderLayout.NORTH;
        }
        if (BorderLayout.EAST.equals(position) || BorderLayout.WEST.equals(position)) {
            toolbar.setOrientation(ToolBar.VERTICAL);
        } else {
            toolbar.setOrientation(ToolBar.HORIZONTAL);
        }
        add(toolbar, position);
    }

    private void saveToolbarParametersToConfig() {
        saveToolbarParametersToConfig(globalToolbar,
                CONFIG_GUI_MAIN_TOOLBAR_GLOBAL_VISIBILITY,
                CONFIG_GUI_MAIN_TOOLBAR_GLOBAL_POSITION);

        saveToolbarParametersToConfig(modelToolbar,
                CONFIG_GUI_MAIN_TOOLBAR_MODEL_VISIBILITY,
                CONFIG_GUI_MAIN_TOOLBAR_MODEL_POSITION);

        saveToolbarParametersToConfig(controlToolbar,
                CONFIG_GUI_MAIN_TOOLBAR_CONTROL_VISIBILITY,
                CONFIG_GUI_MAIN_TOOLBAR_CONTROL_POSITION);
    }

    private void saveToolbarParametersToConfig(JToolBar toolbar, String keyVisibility, String keyPosition) {
        final Framework framework = Framework.getInstance();

        String visibleVal = String.valueOf(toolbar.isVisible());
        framework.setConfigVar(keyVisibility, visibleVal, false);

        Object positionVal = layout.getConstraints(toolbar);
        if (positionVal instanceof String) {
            framework.setConfigVar(keyPosition, (String) positionVal, false);
        }
    }

    public void loadWindowGeometryFromConfig() {
        final Framework framework = Framework.getInstance();
        String maximisedStr = framework.getConfigVar(CONFIG_GUI_MAIN_MAXIMISED, false);
        String widthStr = framework.getConfigVar(CONFIG_GUI_MAIN_WIDTH, false);
        String heightStr = framework.getConfigVar(CONFIG_GUI_MAIN_HEIGHT, false);

        boolean maximised = (maximisedStr == null) ? true : Boolean.parseBoolean(maximisedStr);
        setExtendedState(maximised ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);

        DisplayMode mode = getDisplayMode();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
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
        setSize(width, height);
    }

    public void saveWindowGeometryToConfig() {
        final Framework framework = Framework.getInstance();
        boolean maximised = (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
        framework.setConfigVar(CONFIG_GUI_MAIN_MAXIMISED, Boolean.toString(maximised), false);
        framework.setConfigVar(CONFIG_GUI_MAIN_WIDTH, Integer.toString(getWidth()), false);
        framework.setConfigVar(CONFIG_GUI_MAIN_HEIGHT, Integer.toString(getHeight()), false);
    }

    public void loadRecentFilesFromConfig() {
        final Framework framework = Framework.getInstance();
        lastDirectory = framework.getConfigVar(CONFIG_GUI_MAIN_LAST_DIRECTORY, false);
        for (int i = 0; i < CommonEditorSettings.getRecentCount(); i++) {
            String entry = framework.getConfigVar(CONFIG_GUI_MAIN_RECENT_FILE + i, false);
            pushRecentFile(entry, false);
        }
        updateRecentFilesMenu();
    }

    public void saveRecentFilesToConfig() {
        final Framework framework = Framework.getInstance();
        if (lastDirectory != null) {
            framework.setConfigVar(CONFIG_GUI_MAIN_LAST_DIRECTORY, lastDirectory, false);
        }
        int recentCount = CommonEditorSettings.getRecentCount();
        String[] tmp = recentFiles.toArray(new String[recentCount]);
        for (int i = 0; i < recentCount; i++) {
            framework.setConfigVar(CONFIG_GUI_MAIN_RECENT_FILE + i, tmp[i], false);
        }
    }

    public void pushRecentFile(String fileName, boolean updateMenu) {
        if ((fileName != null) && (new File(fileName).exists())) {
            // Remove previous entry of the fileName
            recentFiles.remove(fileName);
            // Make sure there is not too many entries
            int recentCount = CommonEditorSettings.getRecentCount();
            for (String entry: new ArrayList<String>(recentFiles)) {
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

    public void setLastDirectory(String value) {
        lastDirectory = value;
    }

    public String getLastDirectory() {
        return lastDirectory;
    }

    public void createWork() throws OperationCancelledException {
        createWork(Path.<String>empty());
    }

    public void createWork(Path<String> directory) throws OperationCancelledException {
        CreateWorkDialog dialog = new CreateWorkDialog(this);
        dialog.pack();
        GUI.centerToParent(dialog, this);
        dialog.setVisible(true);
        if (dialog.getModalResult() == 0) {
            throw new OperationCancelledException("Create operation cancelled by user.");
        } else {
            ModelDescriptor info = dialog.getSelectedModel();
            try {
                MathModel mathModel = info.createMathModel();
                VisualModelDescriptor visualModelDescriptor = info.getVisualModelDescriptor();
                if (visualModelDescriptor == null) {
                    throw new VisualModelInstantiationException(
                            "Visual model is not defined for '" + info.getDisplayName() + "'.");
                }
                VisualModel visualModel = visualModelDescriptor.create(mathModel);
                ModelEntry me = new ModelEntry(info, visualModel);
                final Framework framework = Framework.getInstance();
                WorkspaceEntry we = framework.createWork(me, directory, null);
                we.setChanged(false);
            } catch (VisualModelInstantiationException e) {
                e.printStackTrace();
                DialogUtils.showError("Visual model could not be created: " + e.getMessage());
            }
        }
    }

    public void requestFocus(final GraphEditorPanel editor) {
        if (editorInFocus != editor) {
            editorInFocus = editor;
            editorInFocus.updateToolsView();
            editorInFocus.updatePropertyView();
            updateDockableWindowVisibility();

            WorkspaceEntry we = editorInFocus.getWorkspaceEntry();
            mainMenu.setMenuForWorkspaceEntry(we);
            Framework.getInstance().updateJavaScript(we);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (editorInFocus != null) {
                    editorInFocus.requestFocus();
                }
            }
        });
    }

    public void updateDockableWindowVisibility() {
        try {
            // To preserve the layout, first display both the property editor
            // and the tool controls. Only after that close the one of them (if empty).
            displayDockableWindow(propertyEditorDockable);
            displayDockableWindow(toolControlsDockable);
            if (toolControlsWindow.isEmpty()) {
                closeDockableWindow(toolControlsDockable);
            } else if (propertyEditorWindow.isEmpty()) {
                closeDockableWindow(propertyEditorDockable);
            }

        } catch (OperationCancelledException e) {
        }
    }

    public JFileChooser createOpenDialog(String title, boolean multiSelection, boolean allowWorkFiles, Importer[] importers) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle(title);
        GUI.sizeFileChooserToScreen(fc, getDisplayMode());
        if (lastDirectory != null) {
            fc.setCurrentDirectory(new File(lastDirectory));
        }
        fc.setAcceptAllFileFilterUsed(false);
        fc.setMultiSelectionEnabled(multiSelection);
        if (allowWorkFiles) {
            fc.setFileFilter(FileFilters.DOCUMENT_FILES);
        }
        if (importers != null) {
            for (Importer importer: importers) {
                fc.addChoosableFileFilter(new ImporterFileFilter(importer));
            }
        }
        return fc;
    }

    public JFileChooser createSaveDialog(String title, File file, Exporter exporter) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setDialogTitle(title);
        GUI.sizeFileChooserToScreen(fc, getDisplayMode());
        // Set working directory
        fc.setSelectedFile(file);
        if (file.exists()) {
            fc.setCurrentDirectory(file.getParentFile());
        } else if (lastDirectory != null) {
            fc.setCurrentDirectory(new File(lastDirectory));
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
                    String extension = exporter.getFormat().getExtension();
                    if (!path.endsWith(extension)) {
                        path += extension;
                    }
                }
                File f = new File(path);
                if (!f.exists()) {
                    break;
                }
                String msg = "The file '" + f.getName() + "' already exists.\n" + "Overwrite it?";
                if (DialogUtils.showConfirmWarning(msg, DIALOG_SAVE_WORK, false)) {
                    break;
                }
            } else {
                throw new OperationCancelledException("Save operation cancelled by user.");
            }
        }
        return path;
    }

    public void runCommand(Command command) {
        WorkspaceEntry we = editorInFocus.getWorkspaceEntry();
        Commands.run(we, command);
    }

    public void openWork() throws OperationCancelledException {
        JFileChooser fc = createOpenDialog("Open work file(s)", true, true, null);
        if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
            final HashSet<WorkspaceEntry> newWorkspaceEntries = new HashSet<>();
            for (File file: fc.getSelectedFiles()) {
                String path = file.getPath();
                if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                    path += FileFilters.DOCUMENT_EXTENSION;
                    file = new File(path);
                }
                WorkspaceEntry we = openWork(file);
                if (we != null) {
                    newWorkspaceEntries.add(we);
                }
            }
            // FIXME: Go through the newly open works and update their zoom,
            // in case tabs appeared and changed the viewport size.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (WorkspaceEntry we: newWorkspaceEntries) {
                        for (DockableWindow window: editorWindows.get(we)) {
                            GraphEditor editor = getGraphEditorPanel(window);
                            if (editor != null) {
                                editor.zoomFit();
                            }
                        }
                    }
                }
            });
        } else {
            throw new OperationCancelledException("Open operation cancelled by user.");
        }
    }

    public WorkspaceEntry openWork(File file) {
        final Framework framework = Framework.getInstance();
        WorkspaceEntry we = null;
        if (FileUtils.checkAvailability(file, null, true)) {
            try {
                we = framework.loadWork(file);
                pushRecentFile(file.getPath(), true);
                lastDirectory = file.getParent();
            } catch (DeserialisationException e) {
                DialogUtils.showError("A problem was encountered while trying to load '" + file.getPath() + "'.");
                ExceptionUtils.printCause(e);
            }
        }
        return we;
    }

    public void mergeWork() throws OperationCancelledException {
        JFileChooser fc = createOpenDialog("Merge work file(s)", true, true, null);
        if (fc.showDialog(this, "Merge") == JFileChooser.APPROVE_OPTION) {
            for (File file: fc.getSelectedFiles()) {
                String path = file.getPath();
                if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                    path += FileFilters.DOCUMENT_EXTENSION;
                    file = new File(path);
                }
                mergeWork(file);
            }
        } else {
            throw new OperationCancelledException("Merge operation cancelled by user.");
        }
    }

    public void mergeWork(File file) {
        if (editorInFocus == null) {
            openWork(file);
        } else {
            try {
                final Framework framework = Framework.getInstance();
                WorkspaceEntry we = editorInFocus.getWorkspaceEntry();
                framework.mergeWork(we, file);
            } catch (DeserialisationException e) {
                DialogUtils.showError("A problem was encountered while trying to merge '" + file.getPath() + "'.");
                ExceptionUtils.printCause(e);
            }
        }
    }

    public void saveWork() throws OperationCancelledException {
        if (editorInFocus != null) {
            saveWork(editorInFocus.getWorkspaceEntry());
        } else {
            System.out.println("No editor in focus");
        }
    }

    public void saveWorkAs() throws OperationCancelledException {
        if (editorInFocus != null) {
            saveWorkAs(editorInFocus.getWorkspaceEntry());
        } else {
            System.err.println("No editor in focus");
        }
    }

    public void saveWork(WorkspaceEntry we) throws OperationCancelledException {
        if (!we.getFile().exists()) {
            saveWorkAs(we);
        } else {
            String path = we.getFile().getPath();
            saveWork(we, path);
        }
    }

    public void saveWorkAs(WorkspaceEntry we) throws OperationCancelledException {
        File file = we.getFile();
        if (file == null) {
            file = new File(getFileNameForCurrentWork());
        }
        JFileChooser fc = createSaveDialog("Save work as", file, null);
        String path = getValidSavePath(fc, null);
        saveWork(we, path);
    }

    private void saveWork(WorkspaceEntry we, String path) {
        if (we.getModelEntry() == null) {
            throw new RuntimeException(
                    "Cannot save workspace entry - it does not have an associated Workcraft model.");
        }
        try {
            Framework.getInstance().saveWork(we, path);
        } catch (SerialisationException e) {
            DialogUtils.showError(e.getMessage());
        }
        lastDirectory = we.getFile().getParent();
        pushRecentFile(we.getFile().getPath(), true);
    }

    public void importFrom() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        Collection<PluginInfo<? extends Importer>> importerInfo = pm.getPlugins(Importer.class);
        Importer[] importers = new Importer[importerInfo.size()];
        int cnt = 0;
        for (PluginInfo<? extends Importer> info: importerInfo) {
            importers[cnt++] = info.getSingleton();
        }

        JFileChooser fc = createOpenDialog("Import model(s)", true, false, importers);
        if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
            for (File file: fc.getSelectedFiles()) {
                importFrom(file, importers);
            }
        }
    }

    public void importFrom(File file, Importer[] importers) {
        if (FileUtils.checkAvailability(file, null, true)) {
            for (Importer importer: importers) {
                if (!importer.accept(file)) continue;
                try {
                    ModelEntry me = Import.importFromFile(importer, file);
                    String title = me.getMathModel().getTitle();
                    if ((title == null) || title.isEmpty()) {
                        title = FileUtils.getFileNameWithoutExtension(file);
                        me.getMathModel().setTitle(title);
                    }
                    final Framework framework = Framework.getInstance();
                    framework.createWork(me, Path.<String>empty(), file.getName());
                    lastDirectory = file.getParent();
                    break;
                } catch (IOException | DeserialisationException e) {
                    e.printStackTrace();
                    DialogUtils.showError(e.getMessage());
                }
            }
        }
    }

    public void export(Exporter exporter) throws OperationCancelledException {
        String title = "Export as " + exporter.getFormat().getDescription();
        File file = new File(getFileNameForCurrentWork());
        JFileChooser fc = createSaveDialog(title, file, exporter);
        String path = getValidSavePath(fc, exporter);
        VisualModel model = editorInFocus.getModel();
        ExportTask exportTask = new ExportTask(exporter, model, path);
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        String description = "Exporting " + title;
        final TaskFailureNotifier monitor = new TaskFailureNotifier(description);
        taskManager.queue(exportTask, description, monitor);
        lastDirectory = fc.getCurrentDirectory().getPath();
    }

    private String getFileNameForCurrentWork() {
        String fileName = "";
        if (editorInFocus != null) {
            WorkspaceEntry we = editorInFocus.getWorkspaceEntry();
            if (we != null) {
                fileName = we.getFileName();
            }
        }
        return fileName;
    }

    public void refreshWorkspaceEntryTitle(WorkspaceEntry we, boolean updateHeaders) {
        for (DockableWindow window: editorWindows.get(we)) {
            String title = we.getTitleAndModel();
            window.setTitle(title);
        }
        if (updateHeaders) {
            DockableWindow.updateHeaders(rootDockingPort, getDefaultActionListener());
        }
    }

    public void setPropertyEditorTitle(String title) {
        propertyEditorDockable.setTitle(title);
        DockableWindow.updateHeaders(rootDockingPort, getDefaultActionListener());
    }

    public List<GraphEditorPanel> getEditors(WorkspaceEntry we) {
        ArrayList<GraphEditorPanel> result = new ArrayList<>();
        for (DockableWindow window: editorWindows.get(we)) {
            result.add(getGraphEditorPanel(window));
        }
        return result;
    }

    public GraphEditorPanel getEditor(final WorkspaceEntry we) {
        GraphEditorPanel result = this.getCurrentEditor();
        if ((result == null) || (result.getWorkspaceEntry() != we)) {
            final List<GraphEditorPanel> editors = getEditors(we);
            if (editors.size() > 0) {
                result = editors.get(0);
                this.requestFocus(result);
            } else {
                result = this.createEditorWindow(we);
            }
        }
        return result;
    }

    public GraphEditorPanel getCurrentEditor() {
        return editorInFocus;
    }

    public Toolbox getToolbox(final WorkspaceEntry we) {
        GraphEditorPanel editor = getEditor(we);
        return (editor == null) ? null : editor.getToolBox();
    }

    public Toolbox getCurrentToolbox() {
        GraphEditorPanel editor = getCurrentEditor();
        return (editor == null) ? null : editor.getToolBox();
    }

    public void repaintCurrentEditor() {
        if (editorInFocus != null) {
            editorInFocus.repaint();
        }
    }

    public void closeActiveEditor() throws OperationCancelledException {
        for (WorkspaceEntry we: editorWindows.keySet()) {
            for (DockableWindow window: editorWindows.get(we)) {
                DockableWindowContentPanel contentPanel = window.getContentPanel();
                if ((contentPanel != null) && (contentPanel.getContent() == editorInFocus)) {
                    closeDockableWindow(window);
                    return;
                }
            }
        }
    }

    public void closeEditorWindows() throws OperationCancelledException {
        LinkedHashSet<DockableWindow> windowsToClose = new LinkedHashSet<>();
        for (WorkspaceEntry we: editorWindows.keySet()) {
            for (DockableWindow window: editorWindows.get(we)) {
                windowsToClose.add(window);
            }
        }
        for (DockableWindow window: windowsToClose) {
            if (DockingManager.isMaximized(window)) {
                toggleDockableWindowMaximized(window.getID());
            }
        }
        for (DockableWindow window: windowsToClose) {
            closeDockableWindow(window);
        }
    }

    public void closeEditors(WorkspaceEntry openFile) throws OperationCancelledException {
        for (DockableWindow window: new ArrayList<DockableWindow>(editorWindows.get(openFile))) {
            closeDockableWindow(window);
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
        SettingsEditorDialog dialog = new SettingsEditorDialog(this);
        dialog.setVisible(true);
        for (WorkspaceEntry we: editorWindows.keySet()) {
            refreshWorkspaceEntryTitle(we, false);
        }
        DockableWindow.updateHeaders(rootDockingPort, getDefaultActionListener());
        globalToolbar.refreshToggles();
    }

    public void resetLayout() {
        if (DialogUtils.showConfirmWarning(
                "This will close all works and reset the GUI to the default layout.\n\n"
                        + "Are you sure you want to do this?", DIALOG_RESET_LAYOUT, false)) {
            try {
                final Framework framework = Framework.getInstance();
                framework.shutdownGUI();
                new File(Framework.UILAYOUT_FILE_PATH).delete();
                framework.startGUI();
            } catch (OperationCancelledException e) {
            }
        }
    }

    public PropertyEditorWindow getPropertyView() {
        return propertyEditorWindow;
    }

    public ToolControlsWindow getControlsView() {
        return toolControlsWindow;
    }

    public JToolBar getModelToolbar() {
        return modelToolbar;
    }

    public JToolBar getControlToolbar() {
        return controlToolbar;
    }

    public WorkspaceWindow getWorkspaceView() {
        return workspaceWindow;
    }

    private static class ImporterFileFilter extends FileFilter {
        private final Importer importer;

        ImporterFileFilter(Importer importer) {
            this.importer = importer;
        }

        public boolean accept(File f) {
            return f.isDirectory() || importer.accept(f);
        }

        public String getDescription() {
            Format format = importer.getFormat();
            return format.getDescription() + " (*" + format.getExtension() + ")";
        }
    }

    private static class ExporterFileFilter extends FileFilter {
        private final Exporter exporter;

        ExporterFileFilter(Exporter exporter) {
            this.exporter = exporter;
        }

        public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(exporter.getFormat().getExtension());
        }

        public String getDescription() {
            Format format = exporter.getFormat();
            return format.getDescription() + " (*" + format.getExtension() + ")";
        }
    }

}
