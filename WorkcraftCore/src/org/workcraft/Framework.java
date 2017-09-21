package org.workcraft;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.LayoutException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.AbstractLayoutCommand;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.graph.commands.ScriptableCommand;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.gui.workspace.Path;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.interop.Importer;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.layout.DotLayoutCommand;
import org.workcraft.plugins.layout.RandomLayoutCommand;
import org.workcraft.plugins.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.serialisation.DeserialisationResult;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.tasks.ExtendedTaskManager;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Commands;
import org.workcraft.util.DataAccumulator;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Import;
import org.workcraft.util.LogUtils;
import org.workcraft.util.XmlUtils;
import org.workcraft.workspace.Memento;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Stamp;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.xml.sax.SAXException;

public final class Framework {

    private static final String SETTINGS_DIRECTORY_NAME = "workcraft";
    private static final String CONFIG_FILE_NAME = "config.xml";
    private static final String PLUGINS_FILE_NAME = "plugins.xml";
    private static final String UILAYOUT_FILE_NAME = "uilayout.xml";

    public static final String SETTINGS_DIRECTORY_PATH = DesktopApi.getConfigPath() + File.separator + SETTINGS_DIRECTORY_NAME;
    public static final String CONFIG_FILE_PATH = SETTINGS_DIRECTORY_PATH + File.separator + CONFIG_FILE_NAME;
    public static final String PLUGINS_FILE_PATH = SETTINGS_DIRECTORY_PATH + File.separator + PLUGINS_FILE_NAME;
    public static final String UILAYOUT_FILE_PATH = SETTINGS_DIRECTORY_PATH + File.separator + UILAYOUT_FILE_NAME;

    private static final String FRAMEWORK_VARIABLE = "framework";
    private static final String MATH_MODEL_VARIABLE = "mathModel";
    private static final String VISUAL_MODEL_VARIABLE = "visualModel";

    public static final String META_WORK_ENTRY = "meta";
    public static final String STATE_WORK_ENTRY = "state.xml";
    public static final String MATH_MODEL_WORK_ENTRY = "model.xml";
    public static final String VISUAL_MODEL_WORK_ENTRY = "visualModel.xml";

    public static final String META_WORK_ELEMENT = "workcraft-meta";
    public static final String META_DESCRIPTOR_WORK_ELEMENT = "descriptor";
    public static final String META_DESCRIPTOR_CLASS_WORK_ATTRIBUTE = "class";
    public static final String META_VERSION_WORK_ELEMENT = "version";
    public static final String META_VERSION_MAJOR_WORK_ATTRIBUTE = "major";
    public static final String META_VERSION_MINOR_WORK_ATTRIBUTE = "minor";
    public static final String META_VERSION_REVISION_WORK_ATTRIBUTE = "revision";
    public static final String META_VERSION_STATUS_WORK_ATTRIBUTE = "status";
    public static final String META_STAMP_WORK_ELEMENT = "stamp";
    public static final String META_STAMP_TIME_WORK_ATTRIBUTE = "time";
    public static final String META_STAMP_UUID_WORK_ATTRIBUTE = "uuid";
    public static final String META_MATH_MODEL_WORK_ELEMENT = "math";
    public static final String META_VISUAL_MODEL_WORK_ELEMENT = "visual";
    public static final String META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE = "entry-name";
    public static final String META_MODEL_FORMAT_UUID_WORK_ATTRIBUTE = "format-uuid";

    public static final String STATE_WORK_ELEMENT = "workcraft-state";
    public static final String STATE_LEVEL_WORK_ELEMENT = "level";
    public static final String STATE_SELECTION_WORK_ELEMENT = "selection";

    public static final String COMMON_CLASS_WORK_ATTRIBUTE = "class";
    public static final String COMMON_NODE_WORK_ATTRIBUTE = "node";
    public static final String COMMON_REF_WORK_ATTRIBUTE = "ref";

    private static Framework instance = null;

    class ExecuteScriptAction implements ContextAction {
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

    class ExecuteCompiledScriptAction implements ContextAction {
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

    class CompileScriptFromReaderAction implements ContextAction {
        private final String sourceName;
        private final BufferedReader reader;

        CompileScriptFromReaderAction(BufferedReader reader, String sourceName) {
            this.sourceName = sourceName;
            this.reader = reader;
        }

        @Override
        public Object run(Context cx) {
            try {
                return cx.compileReader(reader, sourceName, 1, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class CompileScriptAction implements ContextAction {
        private final String source, sourceName;

        CompileScriptAction(String source, String sourceName) {
            this.source = source;
            this.sourceName = sourceName;
        }

        @Override
        public Object run(Context cx) {
            return cx.compileString(source, sourceName, 1, null);
        }
    }

    class SetArgs implements ContextAction {
        Object[] args;

        public void setArgs(Object[] args) {
            this.args = args;
        }

        @Override
        public Object run(Context cx) {
            Object scriptable = Context.javaToJS(args, systemScope);
            ScriptableObject.putProperty(systemScope, "args", scriptable);
            systemScope.setAttributes("args", ScriptableObject.READONLY);
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

    private final PluginManager pluginManager;
    private final TaskManager taskManager;
    private final CompatibilityManager compatibilityManager;
    private final Workspace workspace;

    private Config config;
    private ScriptableObject systemScope;
    private ScriptableObject globalScope;

    private boolean inGuiMode = false;
    private boolean shutdownRequested = false;
    private boolean guiRestartRequested = false;
    private final ContextFactory contextFactory = new ContextFactory();
    private File workingDirectory = null;
    private MainWindow mainWindow;
    public Memento clipboard;

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
        for (PluginInfo<? extends Settings> info : pluginManager.getPlugins(Settings.class)) {
            info.getSingleton().load(config);
        }
    }

    private void savePluginsSettings() {
        for (PluginInfo<? extends Settings> info : pluginManager.getPlugins(Settings.class)) {
            info.getSingleton().save(config);
        }
    }

    public void resetConfig() {
        config = new Config();
        loadPluginsSettings();
    }

    public void loadConfig() {
        File file = new File(CONFIG_FILE_PATH);
        LogUtils.logMessage("Loading global preferences from " + file.getAbsolutePath());
        config.load(file);
        loadPluginsSettings();
    }

    public void saveConfig() {
        savePluginsSettings();
        File file = new File(CONFIG_FILE_PATH);
        LogUtils.logMessage("Saving global preferences to " + file.getAbsolutePath());
        config.save(file);
    }

    public void setConfigCoreVar(String key, String value) {
        // Set a core variable, that does not require updating plugin settings.
        config.set(key, value);
    }

    /**
     * Used in functions.js JavaScript wrapper.
     */
    public void setConfigVar(String key, String value) {
        setConfigCoreVar(key, value);
        // For consistency, update plugin settings.
        loadPluginsSettings();
    }

    public String getConfigCoreVar(String key) {
        // Get a core variable, that does not require flushing plugin settings.
        return config.get(key);
    }

    /**
     * Used in functions.js JavaScript wrapper.
     */
    public String getConfigVar(String key) {
        // For consistency, flush plugin settings.
        savePluginsSettings();
        return getConfigCoreVar(key);
    }

    public void initJavaScript() {
        LogUtils.logMessage("Initialising JavaScript...");
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                ImporterTopLevel importer = new ImporterTopLevel();
                importer.initStandardObjects(cx, false);
                systemScope = importer;

                Object frameworkScriptable = Context.javaToJS(Framework.this, systemScope);
                ScriptableObject.putProperty(systemScope, FRAMEWORK_VARIABLE, frameworkScriptable);
                systemScope.setAttributes(FRAMEWORK_VARIABLE, ScriptableObject.READONLY);

                globalScope = (ScriptableObject) cx.newObject(systemScope);
                globalScope.setPrototype(systemScope);
                globalScope.setParentScope(null);

                return null;
            }
        });
    }

    public void updateJavaScript(WorkspaceEntry we) {
        ScriptableObject jsGlobalScope = getJavaScriptGlobalScope();
        ModelEntry modelEntry = we.getModelEntry();

        VisualModel visualModel = modelEntry.getVisualModel();
        setJavaScriptProperty(VISUAL_MODEL_VARIABLE, visualModel, jsGlobalScope, true);

        MathModel mathModel = modelEntry.getMathModel();
        setJavaScriptProperty(MATH_MODEL_VARIABLE, mathModel, jsGlobalScope, true);
    }

    public ScriptableObject getJavaScriptGlobalScope() {
        return globalScope;
    }

    public void setJavaScriptProperty(final String name, final Object object,
            final ScriptableObject scope, final boolean readOnly) {

        deleteJavaScriptProperty(name, scope);

        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context arg0) {
                Object scriptable = Context.javaToJS(object, scope);
                ScriptableObject.putProperty(scope, name, scriptable);
                if (readOnly) {
                    scope.setAttributes(name, ScriptableObject.READONLY);
                }
                return scriptable;
            }
        });
    }

    public void deleteJavaScriptProperty(final String name, final ScriptableObject scope) {
        contextFactory.call(new ContextAction() {
            public Object run(Context arg0) {
                return ScriptableObject.deleteProperty(scope, name);
            }
        });
    }

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

    private Object doContextAction(ContextAction action) {
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

    /**
     * Used in functions.js JavaScript wrapper.
     */
    public void execJavaScriptResource(String resourceName) throws IOException {
        execJavaScript(FileUtils.readAllTextFromSystemResource(resourceName));
    }

    /**
     * Used in functions.js JavaScript wrapper.
     */
    public void execJavaScriptFile(String filePath) throws IOException {
        execJavaScript(FileUtils.readAllText(new File(filePath)), globalScope);
    }

    public Script compileJavaScript(String source, String sourceName) {
        return (Script) doContextAction(new CompileScriptAction(source, sourceName));
    }

    public Script compileJavaScript(BufferedReader source, String sourceName) {
        return (Script) doContextAction(new CompileScriptFromReaderAction(source, sourceName));
    }

    public void startGUI() {
        if (inGuiMode) {
            System.out.println("Already in GUI mode");
            return;
        }
        guiRestartRequested = false;
        System.out.println("Switching to GUI mode...");

        if (SwingUtilities.isEventDispatchThread()) {
            mainWindow = new MainWindow();
            mainWindow.startup();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        mainWindow = new MainWindow();
                        mainWindow.startup();
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                Object guiScriptable = Context.javaToJS(mainWindow, systemScope);
                ScriptableObject.putProperty(systemScope, "mainWindow", guiScriptable);
                systemScope.setAttributes("mainWindow", ScriptableObject.READONLY);
                return null;
            }
        });

        inGuiMode = true;
    }

    public void shutdownGUI() throws OperationCancelledException {
        if (isInGuiMode()) {
            mainWindow.shutdown();
            mainWindow.dispose();
            mainWindow = null;
            inGuiMode = false;

            contextFactory.call(new ContextAction() {
                @Override
                public Object run(Context cx) {
                    ScriptableObject.deleteProperty(systemScope, "mainWindow");
                    return null;
                }
            });
        }
    }

    public void shutdown() {
        shutdownRequested = true;
    }

    public boolean shutdownRequested() {
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

    public void setArgs(List<String> args) {
        SetArgs setargs = new SetArgs();
        setargs.setArgs(args.toArray());
        contextFactory.call(setargs);
    }

    /**
     * Used in functions.js JavaScript wrapper.
     */
    public void runCommand(WorkspaceEntry we, String className) {
        if (className != null) {
            for (Command command: Commands.getApplicableCommands(we)) {
                String commandClassName = command.getClass().getSimpleName();
                if (className.equals(commandClassName)) {
                    Commands.run(we, command);
                    break;
                }
            }
        }
    }

    /**
     * Used in functions.js JavaScript wrapper.
     */
    public <T> T executeCommand(WorkspaceEntry we, String className) {
        if ((className == null) || className.isEmpty()) {
            LogUtils.logError("Undefined command name.");
        } else {
            boolean found = false;
            boolean scriptable = false;
            for (Command command: Commands.getCommands()) {
                String commandClassName = command.getClass().getSimpleName();
                if (!className.equals(commandClassName)) continue;
                found = true;
                if (command instanceof ScriptableCommand) {
                    scriptable = true;
                    ScriptableCommand<T> scriptableCommand = (ScriptableCommand<T>) command;
                    return Commands.execute(we, scriptableCommand);
                }
            }
            if (!found) {
                LogUtils.logError("Command '" + className + "' is not found.");
            } else if (!scriptable) {
                LogUtils.logError("Command '" + className + "' cannot be used in scripts.");
            } else {
                LogUtils.logError("Command '" + className + "' is incompatible"
                        + " with workspace entry '" + we.getWorkspacePath() + "'.");
            }
        }
        return null;
    }

    public WorkspaceEntry createWork(ModelEntry me, Path<String> desiredPath) {
        final Path<String> directory = desiredPath.getParent();
        final String desiredName = desiredPath.getNode();
        return createWork(me, directory, desiredName);
    }

    public WorkspaceEntry createWork(ModelEntry me, Path<String> directory, String desiredName) {
        final Path<String> path = getWorkspace().createWorkPath(directory, desiredName);
        boolean open = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
        return createWork(me, path, open, true);
    }

    private WorkspaceEntry createWork(ModelEntry me, Path<String> path, boolean open, boolean changed) {
        WorkspaceEntry we = new WorkspaceEntry(getWorkspace());
        we.setChanged(changed);
        we.setModelEntry(createVisual(me));
        getWorkspace().addWork(path, we);
        if (open && isInGuiMode()) {
            getMainWindow().createEditorWindow(we);
        }
        return we;
    }

    private ModelEntry createVisual(ModelEntry me) {
        ModelEntry result = me;
        VisualModel visualModel = me.getVisualModel();
        if (visualModel != null) {
            visualModel.selectNone();
        } else {
            ModelDescriptor descriptor = me.getDescriptor();
            VisualModelDescriptor vmd = descriptor.getVisualModelDescriptor();
            if (vmd == null) {
                DialogUtils.showError("A visual model could not be created because '"
                        + descriptor.getDisplayName() + "' does not have visual model support.");
            }
            try {
                visualModel = vmd.create(me.getMathModel());
                result = new ModelEntry(descriptor, visualModel);
            } catch (VisualModelInstantiationException e) {
                DialogUtils.showError("A visual model could not be created for the selected model.");
                e.printStackTrace();
            }
            // FIXME: Send notification to components, so their dimensions are updated before layout.
            for (VisualComponent component: Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualComponent.class)) {
                if (component instanceof StateObserver) {
                    ((StateObserver) component).notify(new ModelModifiedEvent(visualModel));
                }
            }
            AbstractLayoutCommand layoutCommand = visualModel.getBestLayouter();
            if (layoutCommand == null) {
                layoutCommand = new DotLayoutCommand();
            }
            try {
                layoutCommand.layout(visualModel);
            } catch (LayoutException e) {
                layoutCommand = new RandomLayoutCommand();
                layoutCommand.layout(visualModel);
            }
        }
        return result;
    }

    /**
     * Used in functions.js JavaScript wrapper.
     */
    public WorkspaceEntry loadWork(String path) throws DeserialisationException {
        File file = getFileByAbsoluteOrRelativePath(path);
        if (checkFileMessageLog(file, null)) {
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
        ModelEntry me = loadModel(file);
        if (me != null) {
            // Load (from *.work) or import (other extensions) work
            if (file.getName().endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                if (path == null) {
                    path = getWorkspace().tempMountExternalFile(file);
                }
            } else {
                Path<String> parent;
                if (path == null) {
                    parent = Path.empty();
                } else {
                    parent = path.getParent();
                }
                String desiredName = FileUtils.getFileNameWithoutExtension(file);
                path = getWorkspace().createWorkPath(parent, desiredName);
            }
            we = createWork(me, path, false, false);
        }
        return we;
    }

    public WorkspaceEntry mergeWork(WorkspaceEntry we, File file) throws DeserialisationException {
        if ((we != null) && file.getName().endsWith(FileFilters.DOCUMENT_EXTENSION)) {
            ModelEntry me = loadModel(file);
            if (me != null) {
                we.insert(me);
            }
        }
        return we;
    }

    public void closeWork(WorkspaceEntry we) {
        getWorkspace().removeWork(we);
    }

    public ModelEntry loadModel(File file) throws DeserialisationException {
        ModelEntry me = null;
        if (checkFileMessageLog(file, null)) {
            // Load (from *.work) or import (other extensions) work.
            if (file.getName().endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                ByteArrayInputStream bis = compatibilityManager.process(file);
                me = loadModel(bis);
            } else {
                try {
                    final PluginManager pm = getPluginManager();
                    final Importer importer = Import.chooseBestImporter(pm, file);
                    me = Import.importFromFile(importer, file);
                } catch (IOException e) {
                    throw new DeserialisationException(e);
                }
            }
        }
        return me;
    }

    public ModelEntry loadModel(InputStream is) throws DeserialisationException {
        try {
            // load meta data
            byte[] bi = DataAccumulator.loadStream(is);
            Document metaDoc = FrameworkUtils.loadMetaDoc(bi);
            ModelDescriptor descriptor = FrameworkUtils.loadMetaDescriptor(metaDoc);
            Stamp stamp = FrameworkUtils.loadMetaStamp(metaDoc);
            Version version = FrameworkUtils.loadMetaVersion(metaDoc);

            // load math model
            InputStream mathData = FrameworkUtils.getMathData(bi, metaDoc);
            XMLModelDeserialiser mathDeserialiser = new XMLModelDeserialiser(getPluginManager());
            DeserialisationResult mathResult = mathDeserialiser.deserialise(mathData, null, null);
            mathData.close();

            // load visual model (if present)
            InputStream visualData = FrameworkUtils.getVisualData(bi, metaDoc);
            if (visualData == null) {
                return new ModelEntry(descriptor, mathResult.model);
            }
            XMLModelDeserialiser visualDeserialiser = new XMLModelDeserialiser(getPluginManager());
            DeserialisationResult visualResult = visualDeserialiser.deserialise(visualData,
                    mathResult.references, mathResult.model);

            // load current level and selection
            if (visualResult.model instanceof VisualModel) {
                FrameworkUtils.loadVisualModelState(bi, (VisualModel) visualResult.model, visualResult.references);
            }
            ModelEntry modelEntry = new ModelEntry(descriptor, visualResult.model);
            modelEntry.setStamp(stamp);
            modelEntry.setVersion(version);
            return modelEntry;
        } catch (IOException | ParserConfigurationException | SAXException |
                InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new DeserialisationException(e);
        }
    }

    public ModelEntry loadModel(Memento memento) {
        try {
            return loadModel(memento.getStream());
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    public ModelEntry loadModel(InputStream is1, InputStream is2) throws DeserialisationException {
        ModelEntry me1 = loadModel(is1);
        ModelEntry me2 = loadModel(is2);

        VisualModel vmodel1 = me1.getVisualModel();
        VisualModel vmodel2 = me2.getVisualModel();

        String displayName1 = me1.getDescriptor().getDisplayName();
        String displayName2 = me2.getDescriptor().getDisplayName();
        if (!displayName1.equals(displayName2)) {
            throw new DeserialisationException(
                    "Incompatible " + displayName1 + " and " + displayName2 + " model cannot be merged.");
        }

        Collection<Node> children = new HashSet<>(vmodel2.getRoot().getChildren());

        vmodel1.selectNone();
        vmodel1.reparent(vmodel1.getCurrentLevel(), vmodel2, vmodel2.getRoot(), null);
        vmodel1.select(children);

        // FIXME: Dirty hack to avoid any hanging observers (serialise and deserialise the model).
        Memento memo = saveModel(me1);
        return loadModel(memo);
    }

    /**
     * Used in functions.js JavaScript wrapper.
     */
    public void saveWork(WorkspaceEntry we, String path) throws SerialisationException {
        if (we == null) return;
        saveModel(we.getModelEntry(), path);
    }

    public void saveModel(ModelEntry modelEntry, String path) throws SerialisationException {
        if (modelEntry == null) return;
        File file = getFileByAbsoluteOrRelativePath(path);
        saveModel(modelEntry, file);
    }

    public void saveModel(ModelEntry modelEntry, File file) throws SerialisationException {
        if (modelEntry == null) return;
        try {
            FileOutputStream stream = new FileOutputStream(file);
            saveModel(modelEntry, stream);
            stream.close();
        } catch (IOException e) {
            throw new SerialisationException(e);
        }
    }

    public void saveModel(ModelEntry modelEntry, OutputStream out) throws SerialisationException {
        Model model = modelEntry.getModel();
        VisualModel visualModel = (model instanceof VisualModel) ? (VisualModel) model : null;
        Model mathModel = (visualModel == null) ? model : visualModel.getMathModel();
        ZipOutputStream zos = new ZipOutputStream(out);
        try {
            ModelSerialiser mathSerialiser = new XMLModelSerialiser(getPluginManager());
            // serialise math model
            zos.putNextEntry(new ZipEntry(MATH_MODEL_WORK_ENTRY));
            ReferenceProducer refResolver = mathSerialiser.serialise(mathModel, zos, null);
            zos.closeEntry();
            // serialise visual model
            ModelSerialiser visualSerialiser = null;
            if (visualModel != null) {
                visualSerialiser = new XMLModelSerialiser(getPluginManager());

                zos.putNextEntry(new ZipEntry(VISUAL_MODEL_WORK_ENTRY));
                ReferenceProducer visualRefs = visualSerialiser.serialise(visualModel, zos, refResolver);
                zos.closeEntry();
                // serialise visual model selection state
                zos.putNextEntry(new ZipEntry(STATE_WORK_ENTRY));
                FrameworkUtils.saveSelectionState(visualModel, zos, visualRefs);
                zos.closeEntry();
            }
            // serialise meta data
            zos.putNextEntry(new ZipEntry(META_WORK_ENTRY));
            Document metaDoc = XmlUtils.createDocument();
            Element metaRoot = metaDoc.createElement(META_WORK_ELEMENT);
            metaDoc.appendChild(metaRoot);

            Element metaVersion = metaDoc.createElement(META_VERSION_WORK_ELEMENT);
            metaVersion.setAttribute(META_VERSION_MAJOR_WORK_ATTRIBUTE, Info.getVersionMajor());
            metaVersion.setAttribute(META_VERSION_MINOR_WORK_ATTRIBUTE, Info.getVersionMinor());
            metaVersion.setAttribute(META_VERSION_REVISION_WORK_ATTRIBUTE, Info.getVersionRevision());
            metaVersion.setAttribute(META_VERSION_STATUS_WORK_ATTRIBUTE, Info.getVersionStatus());
            metaRoot.appendChild(metaVersion);

            Element metaStamp = metaDoc.createElement(META_STAMP_WORK_ELEMENT);
            Stamp stamp = modelEntry.getStamp();
            metaStamp.setAttribute(META_STAMP_TIME_WORK_ATTRIBUTE, stamp.time);
            metaStamp.setAttribute(META_STAMP_UUID_WORK_ATTRIBUTE, stamp.uuid);
            metaRoot.appendChild(metaStamp);

            Element metaDescriptor = metaDoc.createElement(META_DESCRIPTOR_WORK_ELEMENT);
            metaDescriptor.setAttribute(META_DESCRIPTOR_CLASS_WORK_ATTRIBUTE, modelEntry.getDescriptor().getClass().getCanonicalName());
            metaRoot.appendChild(metaDescriptor);

            Element mathElement = metaDoc.createElement(META_MATH_MODEL_WORK_ELEMENT);
            mathElement.setAttribute(META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE, MATH_MODEL_WORK_ENTRY);
            mathElement.setAttribute(META_MODEL_FORMAT_UUID_WORK_ATTRIBUTE, mathSerialiser.getFormatUUID().toString());
            metaRoot.appendChild(mathElement);

            if (visualModel != null) {
                Element visualElement = metaDoc.createElement(META_VISUAL_MODEL_WORK_ELEMENT);
                visualElement.setAttribute(META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE, VISUAL_MODEL_WORK_ENTRY);
                visualElement.setAttribute(META_MODEL_FORMAT_UUID_WORK_ATTRIBUTE, visualSerialiser.getFormatUUID().toString());
                metaRoot.appendChild(visualElement);
            }

            XmlUtils.writeDocument(metaDoc, zos);
            zos.closeEntry();
            zos.close();
        } catch (ParserConfigurationException | IOException e) {
            throw new SerialisationException(e);
        }
    }

    public Memento saveModel(ModelEntry modelEntry) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            saveModel(modelEntry, os);
        } catch (SerialisationException e) {
            throw new RuntimeException(e);
        }
        return new Memento(os.toByteArray());
    }

    public ModelEntry importModel(String path) throws DeserialisationException {
        File file = getFileByAbsoluteOrRelativePath(path);
        return importModel(file);
    }

    public ModelEntry importModel(File file) throws DeserialisationException {
        try {
            final Importer importer = Import.chooseBestImporter(getPluginManager(), file);
            return Import.importFromFile(importer, file);
        } catch (IOException e) {
            throw new DeserialisationException(e);
        }
    }

    /**
     * Used in functions.js JavaScript wrapper.
     */
    public void exportWork(WorkspaceEntry we, String path, String formatName) throws SerialisationException {
        File file = getFileByAbsoluteOrRelativePath(path);
        exportModel(we.getModelEntry(), file, formatName, null);
    }

    public void exportModel(ModelEntry me, File file, Format format) throws SerialisationException {
        exportModel(me, file, format.getName(), format.getUuid());
    }

    private void exportModel(ModelEntry me, File file, String formatName, UUID formatUuid) throws SerialisationException {
        if (me == null) return;
        Exporter exporter = Export.chooseBestExporter(getPluginManager(), me.getMathModel(), formatName, formatUuid);
        if (exporter == null) {
            String modelName = me.getMathModel().getDisplayName();
            LogUtils.logError("Cannot find exporter to " + formatName + " for " + modelName + ".");
        } else {
            try {
                Export.exportToFile(exporter, me.getModel(), file);
            } catch (IOException | ModelValidationException e) {
                throw new SerialisationException(e);
            }
        }
    }

    public void initPlugins() {
        try {
            pluginManager.initPlugins();
        } catch (PluginInstantiationException e) {
            e.printStackTrace();
        }
    }

    public void restartGUI() throws OperationCancelledException {
        guiRestartRequested = true;
        shutdownGUI();
    }

    public boolean isGUIRestartRequested() {
        return guiRestartRequested;
    }

    public void loadWorkspace(File file) throws DeserialisationException {
        workspace.load(file);
    }

    public Config getConfig() {
        return config;
    }

    public boolean checkFileMessageLog(File file, String title) {
        boolean result = true;
        if (title == null) {
            title = "File access error";
        }
        if (!file.exists()) {
            LogUtils.logError(title + ": The path  \"" + file.getPath() + "\" does not exisit.");
            result = false;
        } else if (!file.isFile()) {
            LogUtils.logError(title + ": The path  \"" + file.getPath() + "\" is not a file.");
            result = false;
        } else if (!file.canRead()) {
            LogUtils.logError(title + ": The file  \"" + file.getPath() + "\" cannot be read.");
            result = false;
        }
        return result;
    }

    public File getFileByAbsoluteOrRelativePath(String path) {
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(getWorkingDirectory(), path);
        }
        return file;
    }

    public void setWorkingDirectory(String path) {
        workingDirectory = new File(path);
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

}
