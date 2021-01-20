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
import java.util.*;
import java.util.stream.Collectors;

public class PluginManager implements PluginProvider {

    private final ListMap<Class<?>, PluginInfo<?>> plugins = new ListMap<>();

    public void initPlugins() throws PluginInstantiationException {
        LogUtils.logMessage("Initialising plugins...");
        plugins.clear();

        for (Class<?> cls : getPluginClasses()) {
            LegacyPluginInfo info = new LegacyPluginInfo(cls);
            for (String interfaceName : info.getInterfaces()) {
                try {
                    plugins.put(Class.forName(interfaceName), new PluginInfo<>(info, true));
                } catch (ClassNotFoundException e) {
                    LogUtils.logWarning("Skipping class '" + info.getClassName() + "' that"
                            + " implements unknown interface '" + interfaceName + "'.");
                }
            }
        }

        for (PluginInfo<? extends Plugin> info : getPlugins(Plugin.class)) {
            try {
                final Plugin plugin = info.getInstance();
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> Collection<PluginInfo<? extends T>> getPlugins(Class<T> cls) {
        return (Collection<PluginInfo<? extends T>>) (Collection<?>)
                Collections.unmodifiableCollection(plugins.get(cls));
    }

    public List<ModelDescriptor> getSortedModelDescriptors() {
        return getPlugins(ModelDescriptor.class).stream()
                .map(PluginInfo::getInstance)
                .sorted(Comparator.comparing(ModelDescriptor::getDisplayName))
                .collect(Collectors.toList());
    }

    public List<Settings> getSortedSettings() {
        return getPlugins(Settings.class).stream()
                .map(PluginInfo::getInstance)
                .sorted((o1, o2) -> {
                    if (o1 == o2) return 0;
                    if (o1 == null) return -1;
                    if (o2 == null) return 1;
                    String s1 = o1.getSection();
                    if (s1 == null) return -1;
                    String s2 = o2.getSection();
                    if (s2 == null) return 1;
                    if (s1.equals(s2)) {
                        String n1 = o1.getName();
                        if (n1 == null) return -1;
                        String n2 = o2.getName();
                        if (n2 == null) return 1;
                        return n1.compareTo(n2);
                    }
                    return s1.compareTo(s2);
                })
                .collect(Collectors.toList());
    }

    public List<Importer> getSortedImporters() {
        return getPlugins(Importer.class).stream()
                .map(PluginInfo::getInstance)
                .sorted(Comparator.comparing(o -> o.getFormat().getDescription()))
                .collect(Collectors.toList());
    }

    public List<Exporter> getSortedExporters() {
        return getPlugins(Exporter.class).stream()
                .map(PluginInfo::getInstance)
                .sorted(Comparator.comparing(o -> o.getFormat().getDescription()))
                .collect(Collectors.toList());
    }

    public List<FileHandler> getSortedFileHandlers() {
        return getPlugins(FileHandler.class).stream()
                .map(PluginInfo::getInstance)
                .sorted(Comparator.comparing(FileHandler::getDisplayName))
                .collect(Collectors.toList());
    }

    public List<PropertyClassProvider> getPropertyProviders() {
        return getPlugins(PropertyClassProvider.class).stream()
                .map(PluginInfo::getInstance)
                .collect(Collectors.toList());
    }

    public List<Command> getCommands() {
        return getPlugins(Command.class).stream()
                .map(PluginInfo::getInstance)
                .collect(Collectors.toList());
    }

    public List<GraphEditorTool> getGraphEditorTools() {
        return getPlugins(GraphEditorTool.class).stream()
                .map(PluginInfo::getInstance)
                .collect(Collectors.toList());
    }

    public void registerModelDescriptor(Class<? extends ModelDescriptor> cls) {
        registerClass(ModelDescriptor.class, cls, true);
    }

    public void registerSettings(Class<? extends Settings> cls) {
        registerClass(Settings.class, cls, true);
    }

    public void registerXmlSerialiser(Class<? extends XMLSerialiser> cls) {
        registerClass(XMLSerialiser.class, cls, true);
    }

    public void registerXmlDeserialiser(Class<? extends XMLDeserialiser> cls) {
        registerClass(XMLDeserialiser.class, cls, true);
    }

    public void registerImporter(Class<? extends Importer> cls) {
        registerClass(Importer.class, cls, true);
    }

    public void registerExporter(Class<? extends Exporter> cls) {
        registerClass(Exporter.class, cls, true);
    }

    public void registerFileHandler(Class<? extends FileHandler> cls) {
        registerClass(FileHandler.class, cls, true);
    }

    public void registerProperty(Class<? extends PropertyClassProvider> cls) {
        registerClass(PropertyClassProvider.class, cls, true);
    }

    public void registerCommand(Class<? extends Command> cls) {
        registerClass(Command.class, cls, true);
    }

    public void registerGraphEditorTool(Class<? extends GraphEditorTool> cls, boolean singleton) {
        registerClass(GraphEditorTool.class, cls, singleton);
    }

    private <T> void registerClass(Class<T> type, final Class<? extends T> cls, boolean singleton) {
        registerClass(type, () -> {
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
        }, singleton);
    }

    public void registerModelSerialiser(Initialiser<? extends ModelSerialiser> initialiser) {
        registerClass(ModelSerialiser.class, initialiser, true);
    }

    public void registerModelDeserialiser(Initialiser<? extends ModelDeserialiser> initialiser) {
        registerClass(ModelDeserialiser.class, initialiser, true);
    }

    public void registerFileHandler(Initialiser<? extends FileHandler> initialiser) {
        registerClass(FileHandler.class, initialiser, true);
    }

    private <T> void registerClass(Class<T> cls, Initialiser<? extends T> initialiser, boolean singleton) {
        if (!cls.isInterface()) {
            throw new RuntimeException("'cls' argument must be an interface");
        }
        final PluginInfo<T> pluginInfo = new PluginInfo<>(initialiser, singleton);
        plugins.put(cls, pluginInfo);
    }

}
