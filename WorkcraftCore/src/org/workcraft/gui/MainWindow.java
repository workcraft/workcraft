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
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.SettingsEditorDialog;
import org.workcraft.gui.tasks.TaskFailureNotifier;
import org.workcraft.gui.tasks.TaskManagerWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Commands;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.Import;
import org.workcraft.util.ListMap;
import org.workcraft.util.LogUtils;
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

    private static final String DIALOG_CLOSE_WORK = "Close work";
    private static final String DIALOG_SAVE_WORK = "Save work";
    private static final String DIALOG_RESET_LAYOUT = "Reset layout";

    private final ScriptedActionListener defaultActionListener = new ScriptedActionListener() {
        public void actionPerformed(Action action) {
            action.run();
        }
    };

    private JPanel content;

    private DefaultDockingPort rootDockingPort;
    private DockableWindow documentPlaceholder;
    private DockableWindow outputDockable;
    private DockableWindow propertyEditorDockable;

    private OutputWindow outputWindow;
    private ErrorWindow errorWindow;
    private JavaScriptWindow javaScriptWindow;
    private PropertyEditorWindow propertyEditorWindow;
    private SimpleContainer toolControlsWindow;
    private SimpleContainer editorToolsWindow;
    private WorkspaceWindow workspaceWindow;

    private final ListMap<WorkspaceEntry, DockableWindow> editorWindows = new ListMap<>();
    private final LinkedList<DockableWindow> utilityWindows = new LinkedList<>();

    private GraphEditorPanel editorInFocus;
    private MainMenu mainMenu;

    private String lastSavePath = null;
    private String lastOpenPath = null;
    private final LinkedHashSet<String> recentFiles = new LinkedHashSet<>();

    private int dockableIDCounter = 0;
    private final HashMap<Integer, DockableWindow> idToDockableWindowMap = new HashMap<>();

    protected void createWindows() {
        workspaceWindow = new WorkspaceWindow();
        workspaceWindow.setVisible(true);

        outputWindow = new OutputWindow();
        outputWindow.captureStream();

        errorWindow = new ErrorWindow();
        errorWindow.captureStream();

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
        String title = getTitle(we);
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
            DisplayMode mode = getDisplayMode();
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

        MenuBarUI menuUI = mainMenu.getUI();
        setJMenuBar(mainMenu);

        SilverOceanTheme.enable();
        LookAndFeelHelper.setDefaultLookAndFeel();
        SwingUtilities.updateComponentTreeUI(this);

        if (DesktopApi.getOs().isMac()) {
            mainMenu.setUI(menuUI);
        }

        content = new JPanel(new BorderLayout(0, 0));
        setContentPane(content);
        rootDockingPort = new DefaultDockingPort(FLEXDOCK_DOCKING_PORT);
        content.add(rootDockingPort, BorderLayout.CENTER);
        StandardBorderManager borderManager = new StandardBorderManager(new ShadowBorder());
        rootDockingPort.setBorderManager(borderManager);

        createWindows();
        createDockingLayout();
        loadRecentFilesFromConfig();
        loadWindowGeometryFromConfig();

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
        int id = dockableWindow.getID();
        GraphEditorPanel editor = getGraphEditorPanel(dockableWindow);
        if (editor != null) {
            // handle editor window close
            WorkspaceEntry we = editor.getWorkspaceEntry();

            if (we.isChanged()) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Document '" + we.getTitle() + "' has unsaved changes.\n" + "Save before closing?",
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
                toolControlsWindow.setContent(null);
                mainMenu.removeCommandsMenu();
                editorInFocus = null;
                setDockableTitle(getPropertyEditor(), TITLE_PROPERTY_EDITOR);
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
        DockableWindow window = idToDockableWindowMap.get(id);
        if (window != null) {
            toggleDockableWindow(window);
        } else {
            System.err.println("displayDockableWindow: window with ID=" + id + " was not found.");
        }
    }

    /** For use from Javascript **/
    public void displayDockableWindow(int id) {
        DockableWindow window = idToDockableWindowMap.get(id);
        if (window != null) {
            displayDockableWindow(window);
        } else {
            System.err.println("displayDockableWindow: window with ID=" + id + " was not found.");
        }
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

    public void createDockingLayout() {
        PerspectiveManager pm = (PerspectiveManager) DockingManager.getLayoutManager();
        pm.add(new Perspective(FLEXDOCK_WORKSPACE, FLEXDOCK_WORKSPACE));
        pm.setCurrentPerspective(FLEXDOCK_WORKSPACE, true);
        PropertyManager.getDockingPortRoot().setTabPlacement(SwingConstants.TOP);

        File file = new File(Framework.UILAYOUT_FILE_PATH);
        PersistenceHandler persister = new FilePersistenceHandler(file, XMLPersister.newDefaultInstance());
        PerspectiveManager.setPersistenceHandler(persister);
        PerspectiveManager.setRestoreFloatingOnLoad(true);

        try {
            DockingManager.loadLayoutModel();
        } catch (IOException | PersistenceException e) {
            LogUtils.logWarningLine("Window layout could not be loaded from '" + file.getAbsolutePath() + "'.");
        }
        DockingManager.setFloatingEnabled(true);
        DockingManager.setAutoPersist(true);
        EffectsManager.setPreview(new GhostPreview());

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
                DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON, DockingManager.EAST_REGION,
                xSplit);

        DockableWindow propertyEditorDockable = createDockableWindow(propertyEditorWindow, TITLE_PROPERTY_EDITOR,
                workspaceDockable, DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.NORTH_REGION, ySplit);

        DockableWindow toolControlsDockable = createDockableWindow(editorToolsWindow, TITLE_TOOL_CONTROLS,
                propertyEditorDockable, DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.SOUTH_REGION, 0.4f);

        DockableWindow editorToolsDockable = createDockableWindow(toolControlsWindow, TITLE_EDITOR_TOOLS,
                toolControlsDockable, DockableWindowContentPanel.HEADER | DockableWindowContentPanel.CLOSE_BUTTON,
                DockingManager.SOUTH_REGION, 0.795f);

        documentPlaceholder = createDockableWindow(new DocumentPlaceholder(), TITLE_PLACEHOLDER, null, outputDockable,
                0, DockingManager.NORTH_REGION, ySplit, "DocumentPlaceholder");

        registerUtilityWindow(outputDockable);
        registerUtilityWindow(erroDockable);
        registerUtilityWindow(javaScriptDockable);
        registerUtilityWindow(tasksDockable);
        registerUtilityWindow(propertyEditorDockable);
        registerUtilityWindow(editorToolsDockable);
        registerUtilityWindow(toolControlsDockable);
        registerUtilityWindow(workspaceDockable);
    }

    public void shutdown() throws OperationCancelledException {
        closeEditorWindows();

        final Framework framework = Framework.getInstance();
        if (framework.getWorkspace().isChanged() && !framework.getWorkspace().isTemporary()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Current workspace has unsaved changes.\n" + "Save before closing?", DIALOG_CLOSE_WORK,
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
        saveWindowGeometryToConfig();
        saveRecentFilesToConfig();

        content.remove(rootDockingPort);

        outputWindow.releaseStream();
        errorWindow.releaseStream();
        setVisible(false);
    }

    public void loadWindowGeometryFromConfig() {
        final Framework framework = Framework.getInstance();
        String maximisedStr = framework.getConfigCoreVar(CONFIG_GUI_MAIN_MAXIMISED);
        String widthStr = framework.getConfigCoreVar(CONFIG_GUI_MAIN_WIDTH);
        String heightStr = framework.getConfigCoreVar(CONFIG_GUI_MAIN_HEIGHT);

        boolean maximised = (maximisedStr == null) ? true : Boolean.parseBoolean(maximisedStr);
        this.setExtendedState(maximised ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);

        DisplayMode mode = getDisplayMode();
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

    public DisplayMode getDisplayMode() {
        return getGraphicsConfiguration().getDevice().getDisplayMode();
    }

    public void saveWindowGeometryToConfig() {
        final Framework framework = Framework.getInstance();
        boolean maximised = (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
        framework.setConfigCoreVar(CONFIG_GUI_MAIN_MAXIMISED, Boolean.toString(maximised));
        framework.setConfigCoreVar(CONFIG_GUI_MAIN_WIDTH, Integer.toString(getWidth()));
        framework.setConfigCoreVar(CONFIG_GUI_MAIN_HEIGHT, Integer.toString(getHeight()));
    }

    public void loadRecentFilesFromConfig() {
        final Framework framework = Framework.getInstance();
        lastSavePath = framework.getConfigCoreVar(CONFIG_GUI_MAIN_LAST_SAVE_PATH);
        lastOpenPath = framework.getConfigCoreVar(CONFIG_GUI_MAIN_LAST_OPEN_PATH);
        for (int i = 0; i < CommonEditorSettings.getRecentCount(); i++) {
            String entry = framework.getConfigCoreVar(CONFIG_GUI_MAIN_RECENT_FILE + i);
            pushRecentFile(entry, false);
        }
        updateRecentFilesMenu();
    }

    public void saveRecentFilesToConfig() {
        final Framework framework = Framework.getInstance();
        if (lastSavePath != null) {
            framework.setConfigCoreVar(CONFIG_GUI_MAIN_LAST_SAVE_PATH, lastSavePath);
        }
        if (lastOpenPath != null) {
            framework.setConfigCoreVar(CONFIG_GUI_MAIN_LAST_OPEN_PATH, lastOpenPath);
        }
        int recentCount = CommonEditorSettings.getRecentCount();
        String[] tmp = recentFiles.toArray(new String[recentCount]);
        for (int i = 0; i < recentCount; i++) {
            framework.setConfigCoreVar(CONFIG_GUI_MAIN_RECENT_FILE + i, tmp[i]);
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
        createWork(Path.<String>empty());
    }

    public void createWork(Path<String> directory) throws OperationCancelledException {
        final Framework framework = Framework.getInstance();
        CreateWorkDialog dialog = new CreateWorkDialog(this);
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
                            "visual model is not defined for '" + info.getDisplayName() + "'.");
                }
                VisualModel visualModel = visualModelDescriptor.create(mathModel);
                ModelEntry me = new ModelEntry(info, visualModel);
                WorkspaceEntry we = framework.createWork(me, directory, null);
                we.setChanged(false);
            } catch (VisualModelInstantiationException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Visual model could not be created: " + e.getMessage()
                                + "\n\nSee the Problems window for details.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void requestFocus(GraphEditorPanel sender) {
        sender.requestFocusInWindow();
        if (editorInFocus != sender) {
            editorInFocus = sender;

            WorkspaceEntry we = editorInFocus.getWorkspaceEntry();
            mainMenu.setMenuForWorkspaceEntry(we);

            ToolboxPanel toolBox = sender.getToolBox();
            toolControlsWindow.setContent(toolBox);
            editorToolsWindow.setContent(toolBox.getControlPanel());

            GraphEditorTool selectedTool = toolBox.getSelectedTool();
            selectedTool.setup(editorInFocus);
            sender.updatePropertyView();

            Framework.getInstance().updateJavaScript(we);
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
        GUI.sizeFileChooserToScreen(fc, getDisplayMode());
        // Set working directory
        if (lastOpenPath != null) {
            fc.setCurrentDirectory(new File(lastOpenPath));
        } else if (lastSavePath != null) {
            fc.setCurrentDirectory(new File(lastSavePath));
        }
        // Set file filters
        fc.setAcceptAllFileFilterUsed(false);
        if ((importers == null) || (importers.length == 0)) {
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
        GUI.sizeFileChooserToScreen(fc, getDisplayMode());
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
                        "The file '" + f.getName() + "' already exists.\n" + "Overwrite it?", DIALOG_SAVE_WORK,
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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
            // FIXME: Go through the newly open works and update their zoom,
            // in case tabs appeared and changed the viewport size.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (WorkspaceEntry we : newWorkspaceEntries) {
                        for (DockableWindow window : editorWindows.get(we)) {
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
        if (checkFileMessageDialog(file, null)) {
            try {
                we = framework.loadWork(file);
                if (we.getModelEntry().isVisual()) {
                    createEditorWindow(we);
                }
                pushRecentFile(file.getPath(), true);
                lastOpenPath = file.getParent();
            } catch (DeserialisationException e) {
                JOptionPane.showMessageDialog(this,
                        "A problem was encountered while trying to load '" + file.getPath() + "'.\n"
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

    public void mergeWork(File file) {
        if (editorInFocus == null) {
            openWork(file);
        } else {
            try {
                final Framework framework = Framework.getInstance();
                WorkspaceEntry we = editorInFocus.getWorkspaceEntry();
                framework.mergeWork(we, file);
            } catch (DeserialisationException e) {
                JOptionPane.showMessageDialog(this,
                        "A problem was encountered while trying to merge '" + file.getPath()
                                + "'.\nPlease see Problems window for details.",
                        "Load failed", JOptionPane.ERROR_MESSAGE);
                printCause(e);
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
            try {
                if (we.getModelEntry() != null) {
                    final Framework framework = Framework.getInstance();
                    framework.saveWork(we, path);
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
            pushRecentFile(path, true);
        }
    }

    public void saveWorkAs(WorkspaceEntry we) throws OperationCancelledException {
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
            Path<String> wsTo = ws.getPath(destination);
            if (wsTo == null) {
                wsTo = ws.tempMountExternalFile(destination);
            }
            if (wsFrom != wsTo) {
                ws.moveEntry(wsFrom, wsTo);
            }

            if (we.getModelEntry() != null) {
                framework.saveWork(we, path);
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
        PluginManager pm = framework.getPluginManager();
        Collection<PluginInfo<? extends Importer>> importerInfo = pm.getPlugins(Importer.class);
        Importer[] importers = new Importer[importerInfo.size()];
        int cnt = 0;
        for (PluginInfo<? extends Importer> info : importerInfo) {
            importers[cnt++] = info.getSingleton();
        }

        JFileChooser fc = createOpenDialog("Import model(s)", true, importers);
        if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
            for (File file : fc.getSelectedFiles()) {
                importFrom(file, importers);
            }
        }
    }

    public void importFrom(File file, Importer[] importers) {
        final Framework framework = Framework.getInstance();
        if (checkFileMessageDialog(file, null)) {
            for (Importer importer : importers) {
                if (importer.accept(file)) {
                    try {
                        ModelEntry me = Import.importFromFile(importer, file);
                        String title = FileUtils.getFileNameWithoutExtension(file);
                        me.getModel().setTitle(title);
                        framework.createWork(me, Path.<String>empty(), file.getName());
                        lastOpenPath = file.getParent();
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

    public void export(Exporter exporter) throws OperationCancelledException {
        String title = "Export as " + exporter.getDescription();
        File file = new File(getFileNameForCurrentWork());
        JFileChooser fc = createSaveDialog(title, file, exporter);
        String path = getValidSavePath(fc, exporter);
        VisualModel model = editorInFocus.getModel();
        Task<Object> exportTask = new Export.ExportTask(exporter, model, path);
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final TaskFailureNotifier monitor = new TaskFailureNotifier();
        taskManager.queue(exportTask, "Exporting " + title, monitor);
        lastSavePath = fc.getCurrentDirectory().getPath();
    }

    private String getFileNameForCurrentWork() {
        String fileName = TITLE_PLACEHOLDER;
        if (editorInFocus != null) {
            WorkspaceEntry we = editorInFocus.getWorkspaceEntry();
            if (we != null) {
                fileName = we.getFileName();
            }
        }
        return fileName;
    }

    private String getTitle(WorkspaceEntry we) {
        String prefix = we.isChanged() ? "*" : TITLE_PLACEHOLDER;
        String suffix = TITLE_PLACEHOLDER;
        VisualModel model = we.getModelEntry().getVisualModel();
        if (model != null) {
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
        }
        return prefix + we.getTitle() + suffix;
    }

    public boolean checkFileMessageDialog(File file, String title) {
        boolean result = true;
        if (title == null) {
            title = "File access error";
        }
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "The path  \"" + file.getPath() + "\" does not exisit.\n", title,
                    JOptionPane.ERROR_MESSAGE);
            result = false;
        } else if (!file.isFile()) {
            JOptionPane.showMessageDialog(this, "The path  \"" + file.getPath() + "\" is not a file.\n", title,
                    JOptionPane.ERROR_MESSAGE);
            result = false;
        } else if (!file.canRead()) {
            JOptionPane.showMessageDialog(this, "The file  \"" + file.getPath() + "\" cannot be read.\n", title,
                    JOptionPane.ERROR_MESSAGE);
            result = false;
        }
        return result;
    }

    public void openExternally(String fileName, String errorTitle) {
        File file = new File(fileName);
        if (checkFileMessageDialog(file, errorTitle)) {
            DesktopApi.open(file);
        }
    }

    public void refreshWorkspaceEntryTitle(WorkspaceEntry we, boolean updateHeaders) {
        for (DockableWindow w : editorWindows.get(we)) {
            // final GraphEditorPanel editor = getCurrentEditor();
            // String title = getTitle(we, editor.getModel());
            String title = getTitle(we);
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
        ArrayList<GraphEditorPanel> result = new ArrayList<>();
        for (DockableWindow window : editorWindows.get(we)) {
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

    public ToolboxPanel getToolbox(final WorkspaceEntry we) {
        GraphEditorPanel editor = getEditor(we);
        return (editor == null) ? null : editor.getToolBox();
    }

    public ToolboxPanel getCurrentToolbox() {
        GraphEditorPanel editor = getCurrentEditor();
        return (editor == null) ? null : editor.getToolBox();
    }

    public WorkspaceEntry getCurrentWorkspaceEntry() {
        return getCurrentEditor().getWorkspaceEntry();
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
        LinkedHashSet<DockableWindow> windowsToClose = new LinkedHashSet<>();

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
        SettingsEditorDialog dialog = new SettingsEditorDialog(this);
        dialog.setVisible(true);
        refreshWorkspaceEntryTitles();
    }

    public void resetLayout() {
        if (JOptionPane.showConfirmDialog(this,
                "This will reset the GUI to the default layout.\n" + "Are you sure you want to do this?",
                DIALOG_RESET_LAYOUT, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (JOptionPane.showConfirmDialog(this,
                    "This action requires GUI restart.\n\n" + "Close all editor windows?", DIALOG_RESET_LAYOUT,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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

    private static class ImporterFileFilter extends javax.swing.filechooser.FileFilter {
        private final Importer importer;

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

    private static class ExporterFileFilter extends javax.swing.filechooser.FileFilter {
        private final Exporter exporter;

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

}
