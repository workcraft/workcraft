package org.workcraft.plugins;

import org.workcraft.commands.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.gui.properties.PropertyClassProvider;
import org.workcraft.gui.properties.Settings;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.builtin.*;
import org.workcraft.serialisation.ModelDeserialiser;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.XMLDeserialiser;
import org.workcraft.serialisation.XMLSerialiser;
import org.workcraft.types.ListMap;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.FileHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

public class PluginManager implements PluginProvider {

    private final ListMap<Class<?>, PluginInfo<?>> plugins = new ListMap<>();

    public static class PluginInstanceHolder<T> implements PluginInfo<T> {
        private final Initialiser<? extends T> initialiser;
        private T instance;

        public PluginInstanceHolder(Initialiser<? extends T> initialiser) {
            this.initialiser = initialiser;
        }

        @Override
        public T newInstance() {
            return initialiser.create();
        }

        @Override
        public T getSingleton() {
            if (instance == null) {
                instance = newInstance();
            }
            return instance;
        }
    }

    public void initPlugins() throws PluginInstantiationException {
        LogUtils.logMessage("Initialising plugins...");
        plugins.clear();

        for (Class<?> cls : getPluginClasses()) {
            LegacyPluginInfo info = new LegacyPluginInfo(cls);
            for (String interfaceName : info.getInterfaces()) {
                try {
                    plugins.put(Class.forName(interfaceName), new PluginInstanceHolder<>(info));
                } catch (ClassNotFoundException e) {
                    LogUtils.logWarning("Skipping class '" + info.getClassName() + "' that"
                            + " implements unknown interface '" + interfaceName + "'.");
                }
            }
        }

        for (PluginInfo<? extends Plugin> info : getPlugins(Plugin.class)) {
            try {
                final Plugin plugin = info.newInstance();
                try {
                    LogUtils.logMessage("  Loading plugin: " + plugin.getDescription());
                    plugin.init();
                } catch (Throwable th) {
                    LogUtils.logWarning("Failed to initialise plugin '" + plugin.toString() + "'.");
                }
            } catch (Exception e) {
                LogUtils.logWarning("Failed to load module implementation: " + e.getMessage());
            }
        }
    }

    private LinkedHashSet<Class<?>> getPluginClasses() throws PluginInstantiationException {
        String classPass = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");
        String[] classPathLocations = classPass.split(pathSeparator);

        LinkedHashSet<Class<?>> classes = new LinkedHashSet<>();
        classes.add(BuiltinSerialisers.class);
        classes.add(BuiltinWorkspace.class);
        classes.add(BuiltinExporters.class);
        classes.add(BuiltinCommands.class);
        classes.add(BuiltinSettings.class);
        String requiredPrefix = Plugin.class.getPackage().getName();
        for (String s : classPathLocations) {
            LogUtils.logMessage("  Processing class path entry: " + s);
            classes.addAll(PluginFinder.search(s, requiredPrefix));
        }
        LogUtils.logMessage("" + classes.size() + " plugin(s) found.");
        return classes;
    }

    public Collection<PluginInfo<? extends ModelDescriptor>> getModelDescriptorPlugins() {
        return getPlugins(ModelDescriptor.class);
    }

    public Collection<PluginInfo<? extends Settings>> getSettingsPlugins() {
        return getPlugins(Settings.class);
    }

    public Collection<PluginInfo<? extends Importer>> getImporterPlugins() {
        return getPlugins(Importer.class);
    }

    public Collection<PluginInfo<? extends Exporter>> getExporterPlugins() {
        return getPlugins(Exporter.class);
    }

    public Collection<PluginInfo<? extends FileHandler>> getFileHandlerPlugins() {
        return getPlugins(FileHandler.class);
    }

    public Collection<PluginInfo<? extends PropertyClassProvider>> getPropertyPlugins() {
        return getPlugins(PropertyClassProvider.class);
    }

    public Collection<PluginInfo<? extends Command>> getCommandPlugins() {
        return getPlugins(Command.class);
    }

    public Collection<PluginInfo<? extends GraphEditorTool>> getGraphEditorToolPlugins() {
        return getPlugins(GraphEditorTool.class);
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<PluginInfo<? extends T>> getPlugins(Class<T> interf) {
        return (Collection<PluginInfo<? extends T>>) (Collection<?>) Collections.unmodifiableCollection(plugins.get(interf));
    }

    public void registerModelDescriptor(final Class<? extends ModelDescriptor> cls) {
        registerClass(ModelDescriptor.class, cls);
    }

    public void registerSettings(final Class<? extends Settings> cls) {
        registerClass(Settings.class, cls);
    }

    public void registerXmlSerialiser(final Class<? extends XMLSerialiser> cls) {
        registerClass(XMLSerialiser.class, cls);
    }

    public void registerXmlDeserialiser(final Class<? extends XMLDeserialiser> cls) {
        registerClass(XMLDeserialiser.class, cls);
    }

    public void registerImporter(final Class<? extends Importer> cls) {
        registerClass(Importer.class, cls);
    }

    public void registerExporter(final Class<? extends Exporter> cls) {
        registerClass(Exporter.class, cls);
    }

    public void registerFileHandler(final Class<? extends FileHandler> cls) {
        registerClass(FileHandler.class, cls);
    }

    public void registerProperty(final Class<? extends PropertyClassProvider> cls) {
        registerClass(PropertyClassProvider.class, cls);
    }

    public void registerCommand(final Class<? extends Command> cls) {
        registerClass(Command.class, cls);
    }

    public void registerGlobalTool(final Class<? extends GraphEditorTool> cls) {
        registerClass(GraphEditorTool.class, cls);
    }

    private <T> void registerClass(Class<T> interf, final Class<? extends T> cls) {
        registerClass(interf, () -> {
            try {
                return cls.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                Throwable q = e;
                System.err.println(cls.getName());
                while (q != null) {
                    q.printStackTrace();
                    q = q.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }

    public void registerModelSerialiser(Initialiser<? extends ModelSerialiser> initialiser) {
        registerClass(ModelSerialiser.class, initialiser);
    }

    public void registerModelDeserialiser(Initialiser<? extends ModelDeserialiser> initialiser) {
        registerClass(ModelDeserialiser.class, initialiser);
    }

    public void registerFileHandler(Initialiser<? extends FileHandler> initialiser) {
        registerClass(FileHandler.class, initialiser);
    }

    private <T> void registerClass(Class<T> interf, Initialiser<? extends T> initialiser) {
        if (!interf.isInterface()) {
            throw new RuntimeException("'interf' argument must be an interface");
        }
        final PluginInfo<T> pluginInfo = new PluginInstanceHolder<>(initialiser);
        plugins.put(interf, pluginInfo);
    }

}
