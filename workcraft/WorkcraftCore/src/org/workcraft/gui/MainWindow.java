package org.workcraft.gui;

import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.drag.preview.GhostPreview;
import org.flexdock.docking.props.PropertyManager;
import org.flexdock.docking.state.PersistenceException;
import org.flexdock.perspective.Perspective;
import org.flexdock.perspective.PerspectiveManager;
import org.flexdock.perspective.persist.FilePersistenceHandler;
import org.flexdock.perspective.persist.PersistenceHandler;
import org.flexdock.perspective.persist.xml.XMLPersister;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.dialogs.CreateWorkDialog;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.layouts.MultiBorderLayout;
import org.workcraft.gui.panels.*;
import org.workcraft.gui.properties.SettingsEditorDialog;
import org.workcraft.gui.tabs.*;
import org.workcraft.gui.tasks.TaskFailureNotifier;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.workspace.Path;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.interop.FormatFileFilter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.tasks.ExportTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.*;
import org.workcraft.workspace.FileFilters;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.plaf.MenuBarUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MainWindow extends JFrame {

    private static final String FLEXDOCK_WORKSPACE = "defaultWorkspace";

    private static final String CONFIG_WINDOW_MAXIMISED = "window.maximised";
    private static final String CONFIG_WINDOW_WIDTH = "window.width";
    private static final String CONFIG_WINDOW_HEIGHT = "window.height";
    private static final String CONFIG_TOOLBAR_GLOBAL_VISIBILITY = "toolbar.global.visibility";
    private static final String CONFIG_TOOLBAR_GLOBAL_POSITION = "toolbar.global.position";
    private static final String CONFIG_TOOLBAR_MODEL_VISIBILITY = "toolbar.model.visibility";
    private static final String CONFIG_TOOLBAR_MODEL_POSITION = "toolbar.model.position";
    private static final String CONFIG_TOOLBAR_CONTROL_VISIBILITY = "toolbar.tool.visibility";
    private static final String CONFIG_TOOLBAR_CONTROL_POSITION = "toolbar.tool.position";

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 450;

    private static final String TITLE_WORKCRAFT = "Workcraft";
    private static final String TITLE_OUTPUT = "Output";
    private static final String TITLE_PROBLEMS = "Problems";
    private static final String TITLE_JAVASCRIPT = "JavaScript";
    private static final String TITLE_TASKS = "Tasks";
    private static final String TITLE_WORKSPACE = "Workspace";
    private static final String TITLE_PROPERTY_EDITOR = "Property editor";
    private static final String TITLE_MODEL_TOOLS = "Model tools";
    private static final String TITLE_TOOL_CONTROLS = "Tool controls";
    private static final String TITLE_PLACEHOLDER = "";
    private static final String PREFIX_DOCUMENT = "Document";
    private static final String TITLE_CLOSE_WORK = "Close work";
    private static final String SYMBOL_INFO = Character.toString((char) 0x2709);
    private static final String SYMBOL_PROBLEM = Character.toString((char) 0x26A0);

    private MultiBorderLayout layout;
    private JPanel content;

    private ScrollDockingPort defaultDockingPort;
    private PanelDockable documentPlaceholder;
    private UtilityPanelDockable outputLogDockable;
    private UtilityPanelDockable errorLogDockable;
    private UtilityPanelDockable taskManagerDockable;
    private UtilityPanelDockable propertyEditorDockable;
    private UtilityPanelDockable toolControlsDockable;

    private OutputLogPanel outputLogPanel;
    private ErrorLogPanel errorLogPanel;
    private TaskManagerPanel taskManagerPanel;
    private PropertyEditorPanel propertyEditorPanel;
    private ToolControlsPanel toolControlsPanel;
    private WorkspacePanel workspacePanel;

    private GraphEditorPanel currentEditor;
    private Menu menu;
    private ToolBar globalToolbar;
    private JToolBar modelToolbar;
    private JToolBar controlToolbar;

    class TaskManagerProgressTimer extends Timer {
        private static final char[] PROGRESS_SYMBOLS = {(char) 0x2596, (char) 0x2598, (char) 0x259D, (char) 0x2597};
        private int progressIndex = 0;

        TaskManagerProgressTimer() {
            super(250, null);
            addActionListener(event -> {
                taskManagerDockable.setTitle(PROGRESS_SYMBOLS[progressIndex] + " " + TITLE_TASKS);
                DockingUtils.updateHeader(taskManagerDockable);
                progressIndex = (progressIndex + 1) % PROGRESS_SYMBOLS.length;
            });
        }

        @Override
        public void start() {
            progressIndex = 0;
            setRepeats(true);
            setInitialDelay(0);
            super.start();
        }

        @Override
        public void stop() {
            super.stop();
            taskManagerDockable.setTitle(TITLE_TASKS);
            DockingUtils.updateHeader(taskManagerDockable);
        }
    }

    private final TaskManagerProgressTimer taskManagerTimer = new TaskManagerProgressTimer();

    private final Map<WorkspaceEntry, EditorPanelDockable> weDockableMap = new HashMap<>();

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

    private void createPanels() {
        workspacePanel = new WorkspacePanel();
        workspacePanel.setVisible(true);

        outputLogPanel = new OutputLogPanel(this::updateOutputTitle);
        outputLogPanel.captureStream();

        errorLogPanel = new ErrorLogPanel(this::updateErrorTitle);
        errorLogPanel.captureStream();

        taskManagerPanel = new TaskManagerPanel(this::updatTaskManagerTitle);

        propertyEditorPanel = new PropertyEditorPanel();
        toolControlsPanel = new ToolControlsPanel();
        setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    private void updateOutputTitle() {
        if (outputLogDockable.isHiddenTab() && !outputLogPanel.isEmpty()) {
            outputLogDockable.setTitle(SYMBOL_INFO + " " + TITLE_OUTPUT);
        } else {
            outputLogDockable.setTitle(TITLE_OUTPUT);
        }
        DockingUtils.updateHeader(outputLogDockable);
    }

    private void updateErrorTitle() {
        if (errorLogDockable.isHiddenTab() && !errorLogPanel.isEmpty()) {
            errorLogDockable.setTitle(SYMBOL_PROBLEM + " " + TITLE_PROBLEMS);
        } else {
            errorLogDockable.setTitle(TITLE_PROBLEMS);
        }
        DockingUtils.updateHeader(errorLogDockable);
    }

    private void updatTaskManagerTitle() {
        if (taskManagerDockable.isHiddenTab() && !taskManagerPanel.isEmpty()) {
            if (!taskManagerTimer.isRunning()) {
                taskManagerTimer.start();
            }
        } else if (taskManagerTimer.isRunning()) {
            taskManagerTimer.stop();
        }
    }

    public GraphEditor getOrCreateEditor(WorkspaceEntry we) {
        EditorPanelDockable editorPanelDockable = weDockableMap.get(we);
        if (editorPanelDockable == null) {
            return createEditor(we);
        }
        // Check if the currently active editor is the needed one (it is often the case)
        GraphEditor currentEditor = getCurrentEditor();
        if ((currentEditor != null) && (currentEditor.getWorkspaceEntry() == we)) {
            return currentEditor;
        }
        requestFocus(editorPanelDockable);
        return getEditor(editorPanelDockable);
    }

    private GraphEditor createEditor(WorkspaceEntry we) {
        EditorPanelDockable editorPanelDockable;
        GraphEditorPanel editorPanel = new GraphEditorPanel(we);
        String title = we.getHtmlDetailedTitle();
        String persistentID = PREFIX_DOCUMENT + we.getWorkspacePath();
        if (weDockableMap.isEmpty()) {
            editorPanelDockable = DockingUtils.createEditorDockable(editorPanel, title, documentPlaceholder, persistentID);
            DockingManager.close(documentPlaceholder);
            DockingManager.unregisterDockable(documentPlaceholder);
        } else {
            Collection<EditorPanelDockable> editorPanelDockables = weDockableMap.values();
            DockingUtils.unmaximise(editorPanelDockables);
            EditorPanelDockable firstEditorPanelDockable = editorPanelDockables.iterator().next();
            editorPanelDockable = DockingUtils.createEditorDockable(editorPanel, title, firstEditorPanelDockable, persistentID);
        }
        weDockableMap.put(we, editorPanelDockable);
        requestFocus(editorPanel);
        setWorkActionsEnabledness(true);
        // Refresh bounding boxes of all model components, so connections are clipped correctly
        VisualModel visualModel = we.getModelEntry().getVisualModel();
        if (visualModel != null) {
            ModelUtils.refreshBoundingBox(visualModel);
        }
        editorPanel.zoomFit();
        return editorPanel;
    }

    private void registerUtilityPanelDockable(UtilityPanelDockable utilityPanelDockable) {
        if (!defaultDockingPort.getDockables().contains(utilityPanelDockable)) {
            utilityPanelDockable.setClosed(true);
            DockingManager.close(utilityPanelDockable);
        }
        menu.registerUtilityPanelDockable(utilityPanelDockable);
    }

    public void startup() {
        MainWindowIconManager.apply(this);
        setTitle(TITLE_WORKCRAFT);

        // Create main menu
        menu = new Menu();
        MenuBarUI menuUI = menu.getUI();
        setJMenuBar(menu);
        menu.updateRecentMenu();

        // Tweak look-and-feel
        updateLookAndFeel();
        if (DesktopApi.getOs().isMac()) {
            // Menu UI needs to be restored for OSX (global menu Look-and-Feel)
            menu.setUI(menuUI);
        }

        // Create content panel and docking ports
        layout = new MultiBorderLayout();
        content = new JPanel(layout);
        setContentPane(content);
        defaultDockingPort = new ScrollDockingPort();
        content.add(defaultDockingPort, BorderLayout.CENTER);

        // Create toolbars
        globalToolbar = new ToolBar();
        modelToolbar = new JToolBar(TITLE_MODEL_TOOLS);
        controlToolbar = new JToolBar(TITLE_TOOL_CONTROLS);
        menu.registerToolbar(globalToolbar);
        menu.registerToolbar(modelToolbar);
        menu.registerToolbar(controlToolbar);
        loadToolbarParametersFromConfig();

        // Create dockable panels
        createPanels();
        createDockingLayout();
        loadWindowGeometryFromConfig();

        // Display window in its default state
        setVisible(true);
        DockingUtils.updateHeaders(defaultDockingPort);
        DockingManager.display(outputLogDockable);
        setWorkActionsEnabledness(false);
        updateUtilityPanelDockableVisibility();

        new Thread(() -> {
            // Hack to fix the annoying delay occurring when createGlyphVector is called for the first time
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
            FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
            font.createGlyphVector(frc, TITLE_PLACEHOLDER);
            // Force SVG rendering classes to load
            GuiUtils.createIconFromSVG("images/icon.svg");
        }).start();
    }

    private void updateLookAndFeel() {
        boolean isDefaultLafStyle = EditorCommonSettings.getDialogStyle().isDefaultLafStyle();
        JDialog.setDefaultLookAndFeelDecorated(isDefaultLafStyle);
        MetalLookAndFeel.setCurrentTheme(new SilverOceanTheme());
        LookAndFeelHelper.setDefaultLookAndFeel();
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void setWorkActionsEnabledness(boolean enable) {
        menu.setExportMenuState(enable);
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
        menu.updateCommandsMenuState(canModify);
    }

    public void togglePanelDockableMaximized(PanelDockable panelDockable) {
        if (panelDockable != null) {
            DockingManager.toggleMaximized(panelDockable);
            panelDockable.setMaximized(!panelDockable.isMaximized());
        }
    }

    public void closePanelDockable(PanelDockable panelDockable) {
        if (panelDockable instanceof EditorPanelDockable editorPanelDockable) {
            closeEditorPanelDockable(editorPanelDockable);
        } else if (panelDockable instanceof UtilityPanelDockable utilityPanelDockable) {
            closeUtilityPanelDockable(utilityPanelDockable);
        }
    }

    private void closeEditorPanelDockable(EditorPanelDockable editorPanelDockable) {
        GraphEditor editor = getEditor(editorPanelDockable);
        if (editor == null) {
            return;
        }
        WorkspaceEntry we = editor.getWorkspaceEntry();
        try {
            // Switch to default tool in case there were captured mementos
            Toolbox toolbox = getToolbox(we);
            toolbox.selectDefaultTool();
            // Prompt to save changes if necessary
            saveChangedOrCancel(we);
            // Un-maximise the editor panel
            if (DockingManager.isMaximized(editorPanelDockable)) {
                togglePanelDockableMaximized(editorPanelDockable);
            }
            EditorPanelDockable removedEditorPanelDockable = weDockableMap.remove(we);
            // Close the work if it has no associated dockables, or clear listeners otherwise
            if (removedEditorPanelDockable == null) {
                Framework.getInstance().closeWork(we);
            } else {
                removedEditorPanelDockable.clearTabListeners();
            }
            // Remove commands menu and update property editor panel
            if (currentEditor == editor) {
                menu.setCommandsMenus(null);
                currentEditor = null;
                propertyEditorPanel.clear();
                setPropertyEditorTitle(TITLE_PROPERTY_EDITOR);
            }
            // If no more editor panel dockables left, then activate the document placeholder
            if (weDockableMap.isEmpty()) {
                DockingManager.registerDockable(documentPlaceholder);
                DockingManager.dock(documentPlaceholder, editorPanelDockable, DockingConstants.CENTER_REGION);
                setWorkActionsEnabledness(false);
                modelToolbar.removeAll();
                controlToolbar.removeAll();
                propertyEditorPanel.clear();
                toolControlsPanel.setContent(null);
                displayUtilityPanelDockable(propertyEditorDockable);
                closeUtilityPanelDockable(toolControlsDockable);
                setPropertyEditorTitle(TITLE_PROPERTY_EDITOR);
            }
            // Unregister editor panale dockable from docking manager
            DockingManager.close(editorPanelDockable);
            DockingManager.unregisterDockable(editorPanelDockable);
            editorPanelDockable.setClosed(true);
        } catch (OperationCancelledException ignored) {
            // Operation cancelled by the user
        }
    }

    private void saveChangedOrCancel(WorkspaceEntry we) throws OperationCancelledException {
        if (we.isChanged()) {
            requestFocus(we);
            String message = "Document '" + we.getTitle() + "' has unsaved changes.\n" + "Save before closing?";
            switch (DialogUtils.showYesNoCancel(message, TITLE_CLOSE_WORK)) {
                case JOptionPane.YES_OPTION -> saveWorkOrCancel(we);
                case JOptionPane.NO_OPTION -> { }
                default -> throw new OperationCancelledException();
            }
        }
    }

    private void setToolbarVisibility(JToolBar toolbar, boolean visibility) {
        menu.setToolbarVisibility(toolbar, visibility);
        toolbar.setVisible(visibility);
    }

    private void closeUtilityPanelDockable(UtilityPanelDockable utilityPanelDockable) {
        menu.setUtilityPanelDockableVisibility(utilityPanelDockable, false);
        DockingManager.close(utilityPanelDockable);
        utilityPanelDockable.setClosed(true);
    }

    public void displayUtilityPanelDockable(UtilityPanelDockable utilityPanelDockable) {
        DockingManager.display(utilityPanelDockable);
        utilityPanelDockable.setClosed(false);
        menu.setUtilityPanelDockableVisibility(utilityPanelDockable, true);
    }

    public void toggleUtilityPanelDockable(UtilityPanelDockable utilityPanelDockable) {
        if (utilityPanelDockable.isClosed()) {
            displayUtilityPanelDockable(utilityPanelDockable);
        } else {
            closePanelDockable(utilityPanelDockable);
        }
    }

    private GraphEditor getEditor(EditorPanelDockable editorPanelDockable) {
        JComponent content = (editorPanelDockable == null) ? null : editorPanelDockable.getComponent().getContent();
        return (content instanceof GraphEditor) ? (GraphEditor) content : null;
    }

    public EditorPanelDockable getEditorPanelDockable(GraphEditor editor) {
        for (EditorPanelDockable editorPanelDockable : weDockableMap.values()) {
            if (editor == getEditor(editorPanelDockable)) {
                return editorPanelDockable;
            }
        }
        return null;
    }

    private void createDockingLayout() {
        // Setup docking manger (should go before perspective manager for correctly restoring window layout)
        EffectsManager.setPreview(new GhostPreview());
        DockingManager.setFloatingEnabled(true);
        DockingManager.setAutoPersist(true);
        PropertyManager.getDockingPortRoot().setTabPlacement(SwingConstants.TOP);

        // Set default perspective
        PerspectiveManager pm = PerspectiveManager.getInstance();
        pm.add(new Perspective(FLEXDOCK_WORKSPACE, FLEXDOCK_WORKSPACE));
        pm.setCurrentPerspective(FLEXDOCK_WORKSPACE, true);

        // Configure perspective manager (should go after docking manager for correctly restoring window layout)
        PerspectiveManager.setRestoreFloatingOnLoad(true);
        File file = new File(Framework.UILAYOUT_FILE_PATH);
        PersistenceHandler persistenceHandler = new FilePersistenceHandler(file, XMLPersister.newDefaultInstance());
        PerspectiveManager.setPersistenceHandler(persistenceHandler);

        try {
            DockingManager.loadLayoutModel();
        } catch (IOException | PersistenceException e) {
            LogUtils.logWarning("Window layout could not be loaded from '" + file.getAbsolutePath() + "'.");
        }

        float xSplit = 0.87f;
        float ySplit = 0.8f;

        documentPlaceholder = DockingUtils.createPlaceholderDockable(new DocumentPlaceholderPanel(),
                TITLE_PLACEHOLDER, defaultDockingPort);

        UtilityPanelDockable workspaceDockable = DockingUtils.createUtilityDockable(workspacePanel,
                TITLE_WORKSPACE, documentPlaceholder, DockingManager.EAST_REGION, xSplit);

        propertyEditorDockable = DockingUtils.createUtilityDockable(propertyEditorPanel,
                TITLE_PROPERTY_EDITOR, workspaceDockable, DockingManager.NORTH_REGION, ySplit);

        toolControlsDockable = DockingUtils.createUtilityDockable(toolControlsPanel,
                TITLE_TOOL_CONTROLS, propertyEditorDockable, DockingManager.SOUTH_REGION, 0.5f);

        outputLogDockable = DockingUtils.createUtilityDockable(outputLogPanel, TITLE_OUTPUT, documentPlaceholder,
                DockingManager.SOUTH_REGION, ySplit);

        outputLogDockable.registerUpdater(this::updateOutputTitle);

        errorLogDockable = DockingUtils.createUtilityDockable(errorLogPanel, TITLE_PROBLEMS, outputLogDockable);
        errorLogDockable.registerUpdater(this::updateErrorTitle);

        UtilityPanelDockable javaScriptDockable = DockingUtils.createUtilityDockable(new JavaScriptPanel(),
                TITLE_JAVASCRIPT, outputLogDockable);

        taskManagerDockable = DockingUtils.createUtilityDockable(taskManagerPanel, TITLE_TASKS, outputLogDockable);
        taskManagerDockable.registerUpdater(this::updatTaskManagerTitle);

        registerUtilityPanelDockable(outputLogDockable);
        registerUtilityPanelDockable(errorLogDockable);
        registerUtilityPanelDockable(javaScriptDockable);
        registerUtilityPanelDockable(taskManagerDockable);
        registerUtilityPanelDockable(propertyEditorDockable);
        registerUtilityPanelDockable(toolControlsDockable);
        registerUtilityPanelDockable(workspaceDockable);

        // FIXME: Restoring previously saved layout does not work as expected:
        // "default" and "restored" layouts interfere with each other, which does not look nice.
        //DockingManager.restoreLayout();
    }

    public void shutdown() throws OperationCancelledException {
        closeEditorPanelDockables();
        if (!weDockableMap.isEmpty()) {
            throw new OperationCancelledException();
        }

        Workspace workspace = Framework.getInstance().getWorkspace();
        if (workspace.isChanged() && !workspace.isTemporary()) {
            String message = "Current workspace has unsaved changes.\n" + "Save before closing?";
            switch (DialogUtils.showYesNoCancel(message, TITLE_CLOSE_WORK)) {
                case JOptionPane.YES_OPTION -> workspacePanel.saveWorkspace();
                case JOptionPane.NO_OPTION -> { }
                default -> throw new OperationCancelledException();
            }
        }
        saveWindowGeometryToConfig();
        saveToolbarParametersToConfig();

        content.remove(defaultDockingPort);

        outputLogPanel.releaseStream();
        errorLogPanel.releaseStream();
        setVisible(false);
    }

    private void loadToolbarParametersFromConfig() {
        loadToolbarParametersFromConfig(globalToolbar,
                CONFIG_TOOLBAR_GLOBAL_VISIBILITY,
                CONFIG_TOOLBAR_GLOBAL_POSITION);

        loadToolbarParametersFromConfig(modelToolbar,
                CONFIG_TOOLBAR_MODEL_VISIBILITY,
                CONFIG_TOOLBAR_MODEL_POSITION);

        loadToolbarParametersFromConfig(controlToolbar,
                CONFIG_TOOLBAR_CONTROL_VISIBILITY,
                CONFIG_TOOLBAR_CONTROL_POSITION);
    }

    private void loadToolbarParametersFromConfig(JToolBar toolbar, String keyVisibility, String keyPosition) {
        final Framework framework = Framework.getInstance();

        boolean visible = true;
        String visibleVal = framework.getConfigVar(keyVisibility, false);
        if (visibleVal != null) {
            visible = Boolean.parseBoolean(visibleVal);
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
                CONFIG_TOOLBAR_GLOBAL_VISIBILITY,
                CONFIG_TOOLBAR_GLOBAL_POSITION);

        saveToolbarParametersToConfig(modelToolbar,
                CONFIG_TOOLBAR_MODEL_VISIBILITY,
                CONFIG_TOOLBAR_MODEL_POSITION);

        saveToolbarParametersToConfig(controlToolbar,
                CONFIG_TOOLBAR_CONTROL_VISIBILITY,
                CONFIG_TOOLBAR_CONTROL_POSITION);
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
        String maximisedStr = framework.getConfigVar(CONFIG_WINDOW_MAXIMISED, false);
        boolean maximised = ParseUtils.parseBoolean(maximisedStr, false);
        setExtendedState(maximised ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);

        String widthStr = framework.getConfigVar(CONFIG_WINDOW_WIDTH, false);
        int width = ParseUtils.parseInt(widthStr, DEFAULT_WIDTH);

        String heightStr = framework.getConfigVar(CONFIG_WINDOW_HEIGHT, false);
        int height = ParseUtils.parseInt(heightStr, DEFAULT_HEIGHT);

        setSize(width, height);
    }

    public void saveWindowGeometryToConfig() {
        final Framework framework = Framework.getInstance();
        boolean maximised = (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
        framework.setConfigVar(CONFIG_WINDOW_MAXIMISED, Boolean.toString(maximised), false);
        framework.setConfigVar(CONFIG_WINDOW_WIDTH, Integer.toString(getWidth()), false);
        framework.setConfigVar(CONFIG_WINDOW_HEIGHT, Integer.toString(getHeight()), false);
    }

    public void createWork() {
        createWork(Path.empty());
    }

    public void createWork(Path<String> directory) {
        CreateWorkDialog dialog = new CreateWorkDialog(this);
        if (dialog.reveal()) {
            ModelDescriptor md = dialog.getSelectedModel();
            if (md == null) {
                DialogUtils.showError("Model type is not selected.");
                return;
            }
            try {
                VisualModelDescriptor vmd = md.getVisualModelDescriptor();
                if (vmd == null) {
                    DialogUtils.showError("Visual model is not defined for '" + md.getDisplayName() + "'.");
                    return;
                }
                MathModel mathModel = md.createMathModel();
                VisualModel visualModel = vmd.create(mathModel);
                ModelEntry me = new ModelEntry(md, visualModel);
                final Framework framework = Framework.getInstance();
                WorkspaceEntry we = framework.createWork(me, directory, null);
                we.setChanged(false);
            } catch (VisualModelInstantiationException e) {
                DialogUtils.showError(e.getMessage());
            }
        }
    }

    public void requestFocus(WorkspaceEntry we) {
        EditorPanelDockable editorPanelDockable = weDockableMap.get(we);
        if (editorPanelDockable != null) {
            requestFocus(editorPanelDockable);
        }
    }

    public void requestFocus(EditorPanelDockable editorPanelDockable) {
        ContentPanel component = editorPanelDockable.getComponent();
        Container parent = component.getParent();
        if (parent instanceof JTabbedPane tabbedPane) {
            tabbedPane.setSelectedComponent(component);
        } else if (parent instanceof ScrollDockingPort dockingPort) {
            dockingPort.getRootPane().requestFocus();
        }
        GraphEditor editor = getEditor(editorPanelDockable);
        if (editor != null) {
            editor.requestFocus();
        }
    }

    public void requestFocus(GraphEditorPanel editor) {
        if (currentEditor != editor) {
            currentEditor = editor;
            currentEditor.updateToolsView();
            currentEditor.updatePropertyView();
            updateUtilityPanelDockableVisibility();

            WorkspaceEntry we = currentEditor.getWorkspaceEntry();
            menu.setMenuForWorkspaceEntry(we);
            Framework.getInstance().updateJavaScript(we);
        }
        SwingUtilities.invokeLater(() -> {
            if (currentEditor != null) {
                currentEditor.requestFocus();
            }
        });
    }

    public void updateUtilityPanelDockableVisibility() {
        // In order to preserve the layout, display both the Property
        // editor and the Tool controls, and then close the empty one.
        displayUtilityPanelDockable(propertyEditorDockable);
        displayUtilityPanelDockable(toolControlsDockable);
        if (toolControlsPanel.isEmpty()) {
            closeUtilityPanelDockable(toolControlsDockable);
        } else if (propertyEditorPanel.isEmpty()) {
            closeUtilityPanelDockable(propertyEditorDockable);
        }
    }

    public void openWork() {
        JFileChooser fc = DialogUtils.createFileOpener("Open work file(s)", true, null);
        fc.setMultiSelectionEnabled(true);
        if (DialogUtils.showFileOpener(fc)) {
            final HashSet<WorkspaceEntry> newWorkspaceEntries = new HashSet<>();
            for (File file : fc.getSelectedFiles()) {
                WorkspaceEntry we = openWork(FileFilters.addWorkExtensionIfMissing(file));
                if (we != null) {
                    newWorkspaceEntries.add(we);
                }
            }
            // FIXME: Go through the newly open works and update their zoom,
            // in case tabs appeared and changed the viewport size.
            SwingUtilities.invokeLater(() -> {
                for (WorkspaceEntry we : newWorkspaceEntries) {
                    EditorPanelDockable editorPanelDockable = weDockableMap.get(we);
                    GraphEditor editor = getEditor(editorPanelDockable);
                    if (editor != null) {
                        editor.zoomFit();
                    }
                }
            });
        }
    }

    public WorkspaceEntry openWork(File file) {
        final Framework framework = Framework.getInstance();
        WorkspaceEntry we = null;
        if (FileUtils.checkFileReadability(file, "File access error")) {
            try {
                we = framework.loadWork(file);
                requestFocus(we);
                framework.setLastDirectory(file);
                framework.pushRecentFilePath(file);
                menu.updateRecentMenu();
            } catch (DeserialisationException e) {
                DialogUtils.showError("A problem was encountered while trying to load '"
                        + file.getPath() + "'.\n" + e.getMessage());
            }
        }
        return we;
    }

    public void mergeWork() {
        JFileChooser fc = DialogUtils.createFileOpener("Merge work file(s)", true, null);
        fc.setMultiSelectionEnabled(true);
        if (DialogUtils.showFileOpener(fc)) {
            for (File file : fc.getSelectedFiles()) {
                mergeWork(FileFilters.addWorkExtensionIfMissing(file));
            }
        }
    }

    public void mergeWork(File file) {
        if (currentEditor == null) {
            openWork(file);
        } else if (FileUtils.checkFileReadability(file, "File access error")) {
            try {
                final Framework framework = Framework.getInstance();
                WorkspaceEntry we = currentEditor.getWorkspaceEntry();
                framework.mergeWork(we, file);
            } catch (DeserialisationException e) {
                DialogUtils.showError("A problem was encountered while merging '" + file.getPath() + "'.\n"
                        + e.getMessage());
            }
        }
    }

    public void saveWork() {
        if (currentEditor != null) {
            saveWork(currentEditor.getWorkspaceEntry());
        } else {
            System.out.println("No editor in focus");
        }
    }

    public void saveWork(WorkspaceEntry we) {
        try {
            saveWorkOrCancel(we);
        } catch (OperationCancelledException ignored) {
            // Operation cancelled by the user
        }
    }
    public void saveWorkOrCancel(WorkspaceEntry we) throws OperationCancelledException {
        Workspace workspace = Framework.getInstance().getWorkspace();
        File file = workspace.getFile(we);
        if ((file != null) && file.exists()) {
            saveWork(we, file);
        } else {
            saveWorkAsOrCancel(we);
        }
    }

    public void saveWorkAs() {
        if (currentEditor != null) {
            saveWorkAs(currentEditor.getWorkspaceEntry());
        } else {
            System.err.println("No editor in focus");
        }
    }

    public void saveWorkAs(WorkspaceEntry we) {
        try {
            saveWorkAsOrCancel(we);
        } catch (OperationCancelledException ignored) {
            // Operation cancelled by the user
        }
    }

    public void saveWorkAsOrCancel(WorkspaceEntry we) throws OperationCancelledException {
        Workspace workspace = Framework.getInstance().getWorkspace();
        File file = workspace.getFile(we);
        if (file == null) {
            file = new File(we.getFileName());
        }
        JFileChooser fc = DialogUtils.createFileSaver("Save work as", file, null);
        file = DialogUtils.chooseValidSaveFileOrCancel(fc, null);
        saveWork(we, file);
    }

    private void saveWork(WorkspaceEntry we, File file) {
        if (we.getModelEntry() == null) {
            throw new RuntimeException(
                    "Cannot save workspace entry - it does not have an associated Workcraft model.");
        }
        Framework framework = Framework.getInstance();
        if (FileUtils.checkFileWritability(file, "File write access error")) {
            try {
                framework.saveWork(we, file);
                framework.setLastDirectory(file);
                framework.pushRecentFilePath(file);
                menu.updateRecentMenu();
            } catch (SerialisationException e) {
                DialogUtils.showError(e.getMessage());
            }
        }
    }

    public void importFrom(Importer importer) {
        Format format = importer.getFormat();
        JFileChooser fc = DialogUtils.createFileOpener("Import model(s)", false, format);
        fc.setMultiSelectionEnabled(true);
        if (DialogUtils.showFileOpener(fc)) {
            for (File file : fc.getSelectedFiles()) {
                importFrom(importer, FileFilters.addImporterExtensionIfMissing(file, importer));
            }
        }
    }

    private void importFrom(Importer importer, File file) {
        if (FileUtils.checkFileReadability(file, "File access error")
                && FormatFileFilter.checkFileFormat(file, importer.getFormat())) {

            Framework framework = Framework.getInstance();
            // If context directory is undefined, then set it to that of imported file
            if (framework.getImportContextDirectory() == null) {
                framework.setImportContextDirectory(FileUtils.getFileDirectory(file));
            }

            try {
                ModelEntry me = importer.importFromFile(file, null);
                // Set model title, if empty
                String title = me.getMathModel().getTitle();
                if ((title == null) || title.isEmpty()) {
                    title = FileUtils.getFileNameWithoutExtension(file);
                    me.getMathModel().setTitle(title);
                }
                // Create work with desired name set by the importer
                framework.createWork(me, me.getDesiredName());
                framework.setLastDirectory(FileUtils.getFileDirectory(file));
            } catch (DeserialisationException e) {
                DialogUtils.showError(e.getMessage());
            } catch (OperationCancelledException ignored) {
                // Operation cancelled by the user - restore last directory
            } finally {
                // Reset context directory for future imports
                framework.setImportContextDirectory(null);
            }
        }
    }

    public void export(Exporter exporter, WorkspaceEntry we) {
        Format format = exporter.getFormat();
        String title = "Export as " + format.getDescription();
        File file = new File(we.getFileName());
        JFileChooser fc = DialogUtils.createFileSaver(title, file, format);
        try {
            file = DialogUtils.chooseValidSaveFileOrCancel(fc, format);
            Model<?, ?> model = we.getModelEntry().getModel();
            ExportTask exportTask = new ExportTask(exporter, model, file);
            final TaskManager taskManager = Framework.getInstance().getTaskManager();
            String description = "Exporting " + format.getDescription();
            final TaskFailureNotifier monitor = new TaskFailureNotifier(description);
            taskManager.queue(exportTask, description, monitor);
            Framework.getInstance().setLastDirectory(fc.getCurrentDirectory());
        } catch (OperationCancelledException ignored) {
            // Operation cancelled by the user
        }
    }

    public void refreshWorkspaceEntryTitle(WorkspaceEntry we) {
        refreshWorkspaceEntryTitles(Collections.singleton(we));
    }

    public void refreshWorkspaceEntryTitles(Collection<WorkspaceEntry> wes) {
        for (WorkspaceEntry we : wes) {
            EditorPanelDockable editorPanelDockable = weDockableMap.get(we);
            if ((editorPanelDockable != null) && !editorPanelDockable.isClosed()) {
                String title = we.getHtmlDetailedTitle();
                editorPanelDockable.setTitle(title);
                DockingUtils.updateHeader(editorPanelDockable);
            }
        }
    }

    public void setPropertyEditorTitle(String title) {
        propertyEditorDockable.setTitle(title);
        DockingUtils.updateHeader(propertyEditorDockable);
    }

    public GraphEditor getCurrentEditor() {
        return currentEditor;
    }

    public Toolbox getToolbox(final WorkspaceEntry we) {
        GraphEditor editor = getOrCreateEditor(we);
        return (editor == null) ? null : editor.getToolBox();
    }

    public Toolbox getCurrentToolbox() {
        GraphEditor editor = getCurrentEditor();
        return (editor == null) ? null : editor.getToolBox();
    }

    public void closeActiveEditor() {
        for (WorkspaceEntry we : weDockableMap.keySet()) {
            EditorPanelDockable editorPanelDockable = weDockableMap.get(we);
            ContentPanel contentPanel = (editorPanelDockable == null) ? null : editorPanelDockable.getComponent();
            if ((contentPanel != null) && (contentPanel.getContent() == currentEditor)) {
                closeEditorPanelDockable(editorPanelDockable);
            }
        }
    }

    public void closeEditorPanelDockables() {
        Set<EditorPanelDockable> editorPanelDockables = new HashSet<>(weDockableMap.values());
        for (EditorPanelDockable editorPanelDockable : editorPanelDockables) {
            if (DockingManager.isMaximized(editorPanelDockable)) {
                togglePanelDockableMaximized(editorPanelDockable);
            }
        }
        for (EditorPanelDockable editorPanelDockable : editorPanelDockables) {
            try {
                closeEditorPanelDockable(editorPanelDockable);
            } catch (ClassCastException ignored) {
                // FIXME: Flexdock may throw ClassCast exception when closing Workcraft.
            }
        }
    }

    public void closeEditor(WorkspaceEntry we) {
        closeEditorPanelDockable(weDockableMap.get(we));
    }

    public void undo() {
        if (currentEditor != null) {
            currentEditor.getWorkspaceEntry().undo();
            currentEditor.forceRedraw();
            currentEditor.requestFocus();
        }
    }

    public void redo() {
        if (currentEditor != null) {
            currentEditor.getWorkspaceEntry().redo();
            currentEditor.forceRedraw();
            currentEditor.requestFocus();
        }
    }

    public void cut() {
        if (currentEditor != null) {
            currentEditor.getWorkspaceEntry().cut();
            currentEditor.forceRedraw();
            currentEditor.requestFocus();
        }
    }

    public void copy() {
        if (currentEditor != null) {
            currentEditor.getWorkspaceEntry().copy();
            currentEditor.forceRedraw();
            currentEditor.requestFocus();
        }
    }

    public void paste() {
        if (currentEditor != null) {
            currentEditor.getWorkspaceEntry().paste();
            currentEditor.forceRedraw();
            currentEditor.requestFocus();
        }
    }

    public void delete() {
        if (currentEditor != null) {
            currentEditor.getWorkspaceEntry().delete();
            currentEditor.forceRedraw();
            currentEditor.requestFocus();
        }
    }

    public void selectAll() {
        if (currentEditor != null) {
            VisualModel visualModel = currentEditor.getWorkspaceEntry().getModelEntry().getVisualModel();
            visualModel.selectAll();
            currentEditor.requestFocus();
        }
    }

    public void selectNone() {
        if (currentEditor != null) {
            VisualModel visualModel = currentEditor.getWorkspaceEntry().getModelEntry().getVisualModel();
            visualModel.selectNone();
            currentEditor.requestFocus();
        }
    }

    public void selectInverse() {
        if (currentEditor != null) {
            VisualModel visualModel = currentEditor.getWorkspaceEntry().getModelEntry().getVisualModel();
            visualModel.selectInverse();
            currentEditor.requestFocus();
        }
    }

    public void editSettings() {
        JTextArea outputTextArea = outputLogPanel.getTextEditor();
        Collection<HighlightUtils.HighlightData> outputHighlights = HighlightUtils.getHighlights(outputTextArea);
        SettingsEditorDialog dialog = new SettingsEditorDialog(this);
        if (dialog.reveal()) {
            if (currentEditor != null) {
                menu.setMenuForWorkspaceEntry(currentEditor.getWorkspaceEntry());
            }
            refreshWorkspaceEntryTitles(weDockableMap.keySet());
            if (globalToolbar != null) {
                globalToolbar.refreshToggles();
            }
            updateLookAndFeel();
            HighlightUtils.highlightLines(outputTextArea, outputHighlights);
        }
    }

    public void resetLayout() {
        if (DialogUtils.showConfirm("This will close all works and reset the GUI to the default layout",
                ".\n\nAre you sure you want to do this?", "Reset layout", false,
                JOptionPane.WARNING_MESSAGE, false)) {
            try {
                final Framework framework = Framework.getInstance();
                framework.shutdownGUI();
                File file = new File(Framework.UILAYOUT_FILE_PATH);
                file.delete();
                framework.startGUI();
            } catch (OperationCancelledException ignored) {
                // Operation cancelled by the user
            }
        }
    }

    public PropertyEditorPanel getPropertyView() {
        return propertyEditorPanel;
    }

    public ToolControlsPanel getControlsView() {
        return toolControlsPanel;
    }

    public JToolBar getModelToolbar() {
        return modelToolbar;
    }

    public JToolBar getControlToolbar() {
        return controlToolbar;
    }

    public WorkspacePanel getWorkspaceView() {
        return workspacePanel;
    }

}
