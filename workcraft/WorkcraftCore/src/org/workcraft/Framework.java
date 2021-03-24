package org.workcraft;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mozilla.javascript.*;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.*;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.properties.Settings;
import org.workcraft.gui.workspace.Path;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.builtin.commands.DotLayoutCommand;
import org.workcraft.plugins.builtin.commands.RandomLayoutCommand;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.tasks.ExtendedTaskManager;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.*;
import org.workcraft.workspace.*;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Framework {

    private static final String SETTINGS_DIRECTORY_NAME = "workcraft";
    private static final String CONFIG_FILE_NAME = "config.xml";
    private static final String UILAYOUT_FILE_NAME = "uilayout.xml";

    public static final String SETTINGS_DIRECTORY_PATH = DesktopApi.getConfigPath() + File.separator + SETTINGS_DIRECTORY_NAME;
    public static final String CONFIG_FILE_PATH = SETTINGS_DIRECTORY_PATH + File.separator + CONFIG_FILE_NAME;
    public static final String UILAYOUT_FILE_PATH = SETTINGS_DIRECTORY_PATH + File.separator + UILAYOUT_FILE_NAME;

    private static final String FRAMEWORK_VARIABLE = "framework";
    private static final String MAIN_WINDOW_VARIABLE = "mainWindow";
    private static final String WORKSPACE_ENTRY_VARIABLE = "workspaceEntry";
    private static final String MODEL_ENTRY_VARIABLE = "modelEntry";
    private static final String MATH_MODEL_VARIABLE = "mathModel";
    private static final String VISUAL_MODEL_VARIABLE = "visualModel";
    private static final String ARGS_VARIABLE = "args";

    private static final String CONFIG_RECENT_LAST_DIRECTORY = "recent.lastDirectory";
    private static final String CONFIG_RECENT_FILE = "recent.file";

    private static final Pattern JAVASCRIPT_FUNCTION_PATTERN =
            Pattern.compile("\\s*function\\s+(\\w+)\\s*\\((.*)\\).*");
    private static final int JAVASCRIPT_FUNCTION_NAME_GROUP = 1;
    private static final int JAVASCRIPT_FUNCTION_PARAMS_GROUP = 2;


    private static Framework instance = null;
    private File lastDirectory = null;
    private final LinkedHashSet<String> recentFilePaths = new LinkedHashSet<>();

    static class ExecuteScriptAction implements ContextAction<Object> {
        private final String script;
        private final Scriptable scope;

        ExecuteScriptAction(String script, Scriptable scope) {
            this.script = script;
            this.scope = scope;
        }

        @Override
        public Object run(Context cx) {
            return cx.evaluateString(scope, script, "<string>", 1, null);
        }
    }

    static class ExecuteCompiledScriptAction implements ContextAction<Object> {
        private final Script script;
        private final Scriptable scope;

        ExecuteCompiledScriptAction(Script script, Scriptable scope) {
            this.script = script;
            this.scope = scope;
        }

        @Override
        public Object run(Context cx) {
            return script.exec(cx, scope);
        }
    }

    static class CompileScriptFromReaderAction implements ContextAction<Object> {
        private final String sourceName;
        private final BufferedReader reader;

        CompileScriptFromReaderAction(BufferedReader reader, String sourceName) {
            this.sourceName = sourceName;
            this.reader = reader;
        }

        @Override
        public Script run(Context cx) {
            try {
                return cx.compileReader(reader, sourceName, 1, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class SetArgs implements ContextAction<Void> {
        private Object[] args;

        public void setArgs(Object[] args) {
            this.args = args;
        }

        @Override
        public Void run(Context cx) {
            Object scriptable = Context.javaToJS(args, systemScope);
            ScriptableObject.putProperty(systemScope, ARGS_VARIABLE, scriptable);
            systemScope.setAttributes(ARGS_VARIABLE, ScriptableObject.READONLY);
            return null;
        }
    }

    static class JavascriptPassThroughException extends RuntimeException {
        private static final long serialVersionUID = 8906492547355596206L;
        private final String scriptTrace;

        JavascriptPassThroughException(Throwable wrapped, String scriptTrace) {
            super(wrapped);
            this.scriptTrace = scriptTrace;
        }

        @Override
        public String getMessage() {
            return String.format("Java %s was unhandled in javascript. \nJavascript stack trace: %s",
                    getCause().getClass().getSimpleName(), getScriptTrace());
        }

        public String getScriptTrace() {
            return scriptTrace;
        }
    }

    private static class JavascriptItem {
        public final String name;
        public final String params;
        public final String description;

        JavascriptItem(String name, String params, String description) {
            this.name = name;
            this.params = params;
            this.description = description;
        }

        @Override
        public String toString() {
            return name + (params == null ? "" : "(" + params + ")") + " - " + description;
        }
    }

    private final PluginManager pluginManager;
    private final TaskManager taskManager;
    private final CompatibilityManager compatibilityManager;
    private final Workspace workspace;

    private Config config;
    private ScriptableObject systemScope;
    private ScriptableObject globalScope;

    private boolean inGuiMode = false;
    private boolean shutdownRequested = false;
    private final ContextFactory contextFactory = new ContextFactory();
    private File workingDirectory = null;
    private MainWindow mainWindow;
    public Resource clipboard;
    private final HashMap<String, JavascriptItem> javascriptHelp = new HashMap<>();

    private Framework() {
        pluginManager = new PluginManager();
        taskManager = new ExtendedTaskManager();
        compatibilityManager = new CompatibilityManager();
        config = new Config();
        workspace = new Workspace();
    }

    public static Framework getInstance() {
        if (instance == null) {
            instance = new Framework();
        }
        return instance;
    }

    private void loadPluginsSettings() {
        for (Settings settings : pluginManager.getSortedSettings()) {
            settings.load(config);
        }
    }

    private void savePluginsSettings() {
        for (Settings settings : pluginManager.getSortedSettings()) {
            settings.save(config);
        }
    }

    public void resetConfig() {
        config = new Config();
        loadPluginsSettings();
        savePluginsSettings();
    }

    public void loadConfig() {
        File file = new File(CONFIG_FILE_PATH);
        LogUtils.logMessage("Loading global preferences from " + file.getAbsolutePath());
        config.load(file);
        loadPluginsSettings();
        loadRecentFilesFromConfig();
    }

    public void saveConfig() {
        saveRecentFilesToConfig();
        savePluginsSettings();
        File file = new File(CONFIG_FILE_PATH);
        LogUtils.logMessage("Saving global preferences to " + file.getAbsolutePath());
        config.save(file);
    }

    /**
     * Set a config variable. If requested, reload plugin settings.
     */
    public void setConfigVar(String key, String value, boolean reloadPluginSettings) {
        config.set(key, value);
        if (reloadPluginSettings) {
            loadPluginsSettings();
        }
    }

    /**
     * Get a config variable. If requested, flush plugin settings before that.
     */
    public String getConfigVar(String key, boolean flushPluginSettings) {
        if (flushPluginSettings) {
            savePluginsSettings();
        }
        return config.get(key);
    }

    public void init() {
        // Configure logj4 output and set INFO verbosity.
        // This is necessary for some plugins (e.g. PdfExporter) that use log4j.
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);

        initJavaScript();
        initPlugins();
    }

    private void initPlugins() {
        try {
            pluginManager.initPlugins();
        } catch (PluginInstantiationException e) {
            e.printStackTrace();
        }
    }

    private void initJavaScript() {
        LogUtils.logMessage("Initialising JavaScript...");
        contextFactory.call(cx -> {
            ImporterTopLevel importer = new ImporterTopLevel();
            importer.initStandardObjects(cx, false);
            systemScope = importer;

            Object frameworkScriptable = Context.javaToJS(this, systemScope);
            ScriptableObject.putProperty(systemScope, FRAMEWORK_VARIABLE, frameworkScriptable);
            systemScope.setAttributes(FRAMEWORK_VARIABLE, ScriptableObject.READONLY);

            globalScope = (ScriptableObject) cx.newObject(systemScope);
            globalScope.setPrototype(systemScope);
            globalScope.setParentScope(null);

            return null;
        });
    }

    public void updateJavaScript(WorkspaceEntry we) {
        if (we != null) {
            ScriptableObject jsGlobalScope = getJavaScriptGlobalScope();
            setJavaScriptProperty(WORKSPACE_ENTRY_VARIABLE, we, jsGlobalScope, true);

            ModelEntry me = we.getModelEntry();
            setJavaScriptProperty(MODEL_ENTRY_VARIABLE, me, jsGlobalScope, true);

            VisualModel visualModel = me.getVisualModel();
            setJavaScriptProperty(VISUAL_MODEL_VARIABLE, visualModel, jsGlobalScope, true);

            MathModel mathModel = me.getMathModel();
            setJavaScriptProperty(MATH_MODEL_VARIABLE, mathModel, jsGlobalScope, true);
        }
    }

    public ScriptableObject getJavaScriptGlobalScope() {
        return globalScope;
    }

    public void registerJavaScriptFunction(String function, String description) {
        Matcher matcher = JAVASCRIPT_FUNCTION_PATTERN.matcher(function);
        if (matcher.find()) {
            String name = matcher.group(JAVASCRIPT_FUNCTION_NAME_GROUP);
            String params = matcher.group(JAVASCRIPT_FUNCTION_PARAMS_GROUP);
            addJavaScriptHelp(name, params, description);
            execJavaScript(function, globalScope);
        } else {
            LogUtils.logWarning("Cannot determine the function name in the following JavaScript code:\n" + function);
        }
    }

    public void addJavaScriptHelp(String name, String params, String description) {
        JavascriptItem item = new JavascriptItem(name, params, description);
        if (javascriptHelp.containsKey(name)) {
            LogUtils.logWarning("Overwriting JavaScrip function '" + name + "':\n"
                    + "  Old: " + javascriptHelp.get(name) + "\n"
                    + "  New: " + item);
        }
        javascriptHelp.put(name, item);
    }

    /**
     * Used in core-help.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public String getJavaScriptHelp(String regex, boolean searchDescription) {
        ArrayList<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        for (Entry<String, JavascriptItem> entry : javascriptHelp.entrySet()) {
            String name = entry.getKey();
            JavascriptItem item = entry.getValue();
            Matcher nameMatcher = pattern.matcher(name);
            Matcher descriptionMatcher = pattern.matcher(item.description);
            if (nameMatcher.find() || (searchDescription && descriptionMatcher.find())) {
                result.add(item.toString() + "\n");
            }
        }
        Collections.sort(result);
        return String.join("",  result);
    }

    public void setJavaScriptProperty(final String name, final Object object,
            final ScriptableObject scope, final boolean readOnly) {

        deleteJavaScriptProperty(name, scope);

        contextFactory.call(arg0 -> {
            Object scriptable = Context.javaToJS(object, scope);
            ScriptableObject.putProperty(scope, name, scriptable);
            if (readOnly) {
                scope.setAttributes(name, ScriptableObject.READONLY);
            }
            return scriptable;
        });
    }

    public void deleteJavaScriptProperty(final String name, final ScriptableObject scope) {
        contextFactory.call(arg0 -> ScriptableObject.deleteProperty(scope, name));
    }

    /**
     * Used in core-exec.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public Object execJavaScript(File file) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        return execJavaScript(compileJavaScript(reader, file.getPath()));
    }

    public Object execJavaScript(Script script) {
        return execJavaScript(script, globalScope);
    }

    public Object execJavaScript(String script) {
        return execJavaScript(script, globalScope);
    }

    private Object execJavaScript(Script script, Scriptable scope) {
        return doContextAction(new ExecuteCompiledScriptAction(script, scope));
    }

    private Object execJavaScript(String script, Scriptable scope) {
        return doContextAction(new ExecuteScriptAction(script, scope));
    }

    private Object doContextAction(ContextAction<Object> action) {
        try {
            return contextFactory.call(action);
        } catch (JavaScriptException ex) {
            System.out.println("Script stack trace: " + ex.getScriptStackTrace());
            Object value = ex.getValue();
            if (value instanceof NativeJavaObject) {
                Object wrapped = ((NativeJavaObject) value).unwrap();
                if (wrapped instanceof Throwable) {
                    throw new JavascriptPassThroughException((Throwable) wrapped, ex.getScriptStackTrace());
                }
            }
            throw ex;
        }
    }

    public void execJavaScriptResource(String resourceName) throws IOException {
        String script = FileUtils.readAllTextFromSystemResource(resourceName);
        execJavaScript(script);
    }

    /**
     * Used in core-exec.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public void execJavaScriptFile(String path) throws IOException {
        File file = getFileByAbsoluteOrRelativePath(path);
        execJavaScriptFile(file);
    }

    public void execJavaScriptFile(File file) throws IOException {
        String script = FileUtils.readAllText(file);
        execJavaScript(script, globalScope);
    }

    public Script compileJavaScript(BufferedReader source, String sourceName) {
        return (Script) doContextAction(new CompileScriptFromReaderAction(source, sourceName));
    }

    public void startGUI() {
        if (inGuiMode) {
            System.out.println("Already in GUI mode");
            return;
        }
        System.out.println("Switching to GUI mode...");

        if (SwingUtilities.isEventDispatchThread()) {
            mainWindow = new MainWindow();
            mainWindow.startup();
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    mainWindow = new MainWindow();
                    mainWindow.startup();
                });
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        contextFactory.call(cx -> {
            Object guiScriptable = Context.javaToJS(mainWindow, systemScope);
            ScriptableObject.putProperty(systemScope, MAIN_WINDOW_VARIABLE, guiScriptable);
            systemScope.setAttributes(MAIN_WINDOW_VARIABLE, ScriptableObject.READONLY);
            return null;
        });

        inGuiMode = true;
    }

    public void shutdownGUI() throws OperationCancelledException {
        if (isInGuiMode()) {
            mainWindow.shutdown();
            mainWindow.dispose();
            mainWindow = null;
            inGuiMode = false;

            contextFactory.call(cx -> {
                ScriptableObject.deleteProperty(systemScope, MAIN_WINDOW_VARIABLE);
                return null;
            });
        }
    }

    public void shutdown() {
        shutdownRequested = true;
    }

    public boolean isShutdownRequested() {
        return shutdownRequested;
    }

    public void abortShutdown() {
        shutdownRequested = false;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public CompatibilityManager getCompatibilityManager() {
        return compatibilityManager;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public boolean isInGuiMode() {
        return inGuiMode;
    }

    public void setArgs(Collection<String> args) {
        SetArgs setargs = new SetArgs();
        setargs.setArgs(args.toArray());
        contextFactory.call(setargs);
    }

    /**
     * Used in core-exec.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public void runCommand(WorkspaceEntry we, String className) {
        CommandUtils.run(we, className);
    }

    /**
     * Used in core-exec.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public <R> R executeCommand(WorkspaceEntry we, String className) {
        return CommandUtils.execute(we, className);
    }

    /**
     * Used in core-exec.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public <R, D> R executeCommand(WorkspaceEntry we, String className, String serialisedData) {
        return CommandUtils.execute(we, className, serialisedData);
    }

    public WorkspaceEntry createWork(ModelEntry me, String desiredName) {
        return createWork(me, Path.empty(), desiredName);
    }

    public WorkspaceEntry createWork(ModelEntry me, Path<String> parent, String name) {
        final Path<String> path = getWorkspace().createWorkPath(parent, name);
        boolean open = me.isVisual() || EditorCommonSettings.getOpenNonvisual();
        return createWork(me, path, open, true);
    }

    public WorkspaceEntry createWork(ModelEntry me, Path<String> path, boolean open, boolean changed) {
        WorkspaceEntry we = new WorkspaceEntry();
        we.setModelEntry(createVisualIfAbsent(me));
        getWorkspace().addWork(path, we);
        if (open && isInGuiMode()) {
            if ((me == we.getModelEntry()) || attemptLayout(we.getModelEntry().getVisualModel())) {
                getMainWindow().createEditorWindow(we);
            }
        }
        we.setChanged(changed);
        return we;
    }

    private ModelEntry createVisualIfAbsent(ModelEntry me) {
        ModelEntry result = me;
        VisualModel visualModel = me.getVisualModel();
        if (visualModel != null) {
            visualModel.selectNone();
        } else {
            ModelDescriptor md = me.getDescriptor();
            if (md == null) {
                DialogUtils.showError("Model descriptor is not defined.");
                return result;
            }
            VisualModelDescriptor vmd = md.getVisualModelDescriptor();
            if (vmd == null) {
                DialogUtils.showError("Visual model is not defined for '" + md.getDisplayName() + "'.");
                return result;
            }

            try {
                visualModel = vmd.create(me.getMathModel());
                result = new ModelEntry(md, visualModel);
            } catch (VisualModelInstantiationException e) {
                DialogUtils.showError(e.getMessage());
            }
        }
        return result;
    }

    private boolean attemptLayout(VisualModel model) {
        int answer = 0;
        Container root = model.getRoot();
        int nodeCount = Hierarchy.getDescendantsOfType(root, VisualNode.class).size();
        if (nodeCount > EditorCommonSettings.getLargeModelSize()) {
            String message = "The model may be too large for automatic"
                    + "\nlayout (" + nodeCount + " elements)."
                    + "\nPerform layout anyway before opening in editor?";

            answer = DialogUtils.showYesNoCancel(message, "Layout", 2);
        }

        if (answer > 1) {
            return false;
        }
        // FIXME: Send notification to components, so their dimensions are updated before layout.
        for (VisualComponent component : Hierarchy.getDescendantsOfType(root, VisualComponent.class)) {
            if (component instanceof StateObserver) {
                ((StateObserver) component).notify(new ModelModifiedEvent(model));
            }
        }
        AbstractLayoutCommand layoutCommand = answer == 0 ? model.getBestLayouter() : new RandomLayoutCommand();
        if (layoutCommand == null) {
            layoutCommand = new DotLayoutCommand();
        }
        try {
            layoutCommand.layout(model);
        } catch (LayoutException e) {
            layoutCommand = new RandomLayoutCommand();
            layoutCommand.layout(model);
        }
        return true;
    }

    /**
     * Used in core-file.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public WorkspaceEntry loadWork(String path) throws DeserialisationException {
        File file = getFileByAbsoluteOrRelativePath(path);
        if (FileUtils.checkAvailability(file, null, false)) {
            return loadWork(file);
        }
        return null;
    }

    public WorkspaceEntry loadWork(File file) throws DeserialisationException {
        // Check if work is already loaded
        Path<String> path = getWorkspace().getPath(file);
        for (WorkspaceEntry we : getWorkspace().getWorks()) {
            if (we.getWorkspacePath().equals(path)) {
                return we;
            }
        }
        WorkspaceEntry we = null;
        ModelEntry me = WorkUtils.loadModel(file);
        if (me != null) {
            // Load (from *.work) or import (other extensions) work
            boolean isWorkFile = FileFilters.isWorkFile(file);
            if (isWorkFile) {
                if (path == null) {
                    path = getWorkspace().tempMountExternalFile(file);
                }
            } else {
                String desiredName = FileUtils.getFileNameWithoutExtension(file);
                Path<String> parent = path == null ? Path.empty() : path.getParent();
                path = getWorkspace().createWorkPath(parent, desiredName);
            }
            we = createWork(me, path, true, false);
            if (isWorkFile) {
                Collection<Resource> resources = WorkUtils.loadResources(file);
                resources.forEach(we::addResource);
            }
        }
        updateJavaScript(we);
        return we;
    }

    public void mergeWork(WorkspaceEntry we, File file) throws DeserialisationException {
        if ((we != null) && FileFilters.isWorkFile(file)) {
            ModelEntry me = WorkUtils.loadModel(file);
            if (me != null) {
                we.insert(me);
            }
        }
    }

    /**
     * Used in core-workspace.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public void closeWork(WorkspaceEntry we) {
        getWorkspace().removeWork(we);
    }

    /**
     * Used in core-workspace.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public void closeAllWorks() {
        for (WorkspaceEntry we : getWorkspace().getWorks()) {
            closeWork(we);
        }
    }

    /**
     * Used in core-file.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public void saveWork(WorkspaceEntry we, String path) throws SerialisationException {
        if (we == null) return;
        File file = getFileByAbsoluteOrRelativePath(path);
        saveWork(we, file);
    }

    public void saveWork(WorkspaceEntry we, File file) throws SerialisationException {
        if (we == null) return;
        Path<String> wsFrom = we.getWorkspacePath();
        Path<String> wsTo = workspace.getPath(file);
        if (wsTo == null) {
            wsTo = workspace.tempMountExternalFile(file);
        }
        if (wsFrom != wsTo) {
            try {
                workspace.moveEntry(wsFrom, wsTo);
            } catch (IOException e) {
                LogUtils.logError(e.getMessage());
            }
        }
        WorkUtils.saveModel(we.getModelEntry(), we.getResources(), file);

        we.setChanged(false);
        if (mainWindow != null) {
            mainWindow.refreshWorkspaceEntryTitle(we);
        }
    }

    /**
     * Used in core-file.js JavaScript wrapper.
     */
    @SuppressWarnings("unused")
    public void exportWork(WorkspaceEntry we, String path, String formatName) throws SerialisationException {
        File file = getFileByAbsoluteOrRelativePath(path);
        exportModel(we.getModelEntry(), file, formatName, null);
    }

    public void exportWork(WorkspaceEntry we, File file, Format format) throws SerialisationException {
        exportModel(we.getModelEntry(), file, format.getName(), format.getUuid());
    }

    public void exportModel(ModelEntry me, File file, Format format) throws SerialisationException {
        exportModel(me, file, format.getName(), format.getUuid());
    }

    private void exportModel(ModelEntry me, File file, String formatName, UUID formatUuid) throws SerialisationException {
        if (me == null) return;
        // Try to find exporter for visual model first.
        Exporter exporter = ExportUtils.chooseBestExporter(me.getVisualModel(), formatName, formatUuid);
        if (exporter == null) {
            // If no exporter found for visual model, then try to find exporter for math model.
            exporter = ExportUtils.chooseBestExporter(me.getMathModel(), formatName, formatUuid);
        }
        if (exporter == null) {
            String modelName = me.getMathModel().getDisplayName();
            LogUtils.logError("Cannot find exporter to " + formatName + " for " + modelName + ".");
        } else {
            try {
                ExportUtils.exportToFile(exporter, me.getModel(), file);
            } catch (IOException | ModelValidationException e) {
                throw new SerialisationException(e);
            }
        }
    }

    public void loadWorkspace(File file) throws DeserialisationException {
        workspace.load(file);
    }

    public Config getConfig() {
        return config;
    }

    private void loadRecentFilesFromConfig() {
        String lastDirectoryPath = getConfigVar(CONFIG_RECENT_LAST_DIRECTORY, false);
        File lastDirectory = (lastDirectoryPath == null) ? null : new File(lastDirectoryPath);
        setLastDirectory(lastDirectory);
        for (int i = 0; i < EditorCommonSettings.getRecentCount(); i++) {
            String entry = getConfigVar(CONFIG_RECENT_FILE + i, false);
            pushRecentFilePath(entry);
        }
    }

    private void saveRecentFilesToConfig() {
        String lastDirectoryPath = FileUtils.getFullPath(getLastDirectory());
        if (lastDirectoryPath != null) {
            setConfigVar(CONFIG_RECENT_LAST_DIRECTORY, lastDirectoryPath, false);
        }
        int recentCount = EditorCommonSettings.getRecentCount();
        String[] tmp = recentFilePaths.toArray(new String[recentCount]);
        for (int i = 0; i < recentCount; i++) {
            setConfigVar(CONFIG_RECENT_FILE + i, tmp[i], false);
        }
    }

    public void pushRecentFilePath(File file) {
        pushRecentFilePath(FileUtils.getFullPath(file));
    }

    public void pushRecentFilePath(String filePath) {
        if ((filePath != null) && (new File(filePath).exists())) {
            // Remove previous entry of the fileName
            recentFilePaths.remove(filePath);
            // Make sure there is not too many entries
            int recentCount = EditorCommonSettings.getRecentCount();
            for (String entry: new ArrayList<>(recentFilePaths)) {
                if (recentFilePaths.size() < recentCount) {
                    break;
                }
                recentFilePaths.remove(entry);
            }
            // Add the fileName if possible
            if (recentFilePaths.size() < recentCount) {
                recentFilePaths.add(filePath);
            }
        }
    }

    public void clearRecentFilePaths() {
        recentFilePaths.clear();
    }

    public ArrayList<String> getRecentFilePaths() {
        ArrayList<String> result = new ArrayList<>(recentFilePaths);
        Collections.reverse(result);
        return result;
    }

    public void setLastDirectory(File value) {
        if (value != null) {
            if (value.isDirectory()) {
                lastDirectory = value;
            } else {
                File parentFile = value.getParentFile();
                if ((parentFile != null) && parentFile.isDirectory()) {
                    lastDirectory = parentFile;
                }
            }
        }
    }

    public File getLastDirectory() {
        return lastDirectory;
    }

    public File getFileByAbsoluteOrRelativePath(String path) {
        return FileUtils.getFileByAbsoluteOrRelativePath(path, getWorkingDirectory());
    }

    public void setWorkingDirectory(File dir) {
        workingDirectory = dir;
    }

    public File getWorkingDirectory() {
        if (workingDirectory == null) {
            String path = System.getProperty("user.dir");
            setWorkingDirectory(path == null ? null : new File(path));
        }
        return workingDirectory;
    }

    public WorkspaceEntry getWorkspaceEntry(ModelEntry me) {
        for (WorkspaceEntry we : getWorkspace().getWorks()) {
            if (we.getModelEntry() == me) {
                return we;
            }
        }
        return null;
    }

    public void updatePropertyView() {
        if (isInGuiMode()) {
            GraphEditorPanel editor = getMainWindow().getCurrentEditor();
            SwingUtilities.invokeLater(editor::updatePropertyView);
        }
    }

}
