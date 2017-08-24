package org.workcraft;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.util.ConstructorParametersMatcher;
import org.workcraft.util.ListMap;
import org.workcraft.util.LogUtils;

public class PluginManager implements PluginProvider {

    public static final String VERSION_STAMP = "d971444cbd86148695f3427118632aca";

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

    private boolean initModules() {
        boolean result = true;
        for (PluginInfo<? extends Module> info : getPlugins(Module.class)) {
            try {
                final Module module = info.newInstance();
                try {
                    LogUtils.logMessage("  Loading module: " + module.getDescription());
                    module.init();
                } catch (Throwable th) {
                    LogUtils.logWarning("Failed to initialise module '" + module.toString() + "'.");
                    result = false;
                }
            } catch (Exception e) {
                LogUtils.logWarning("Failed to load module implementation: " + e.getMessage());
                result = false;
            }
        }
        return result;
    }

    private void processLegacyPlugin(Class<?> cls, LegacyPluginInfo info) throws PluginInstantiationException {
        for (String interfaceName : info.getInterfaces()) {
            try {
                PluginInstanceHolder<Object> pih = new PluginInstanceHolder<Object>(info);
                plugins.put(Class.forName(interfaceName), pih);
            } catch (ClassNotFoundException e) {
                String className = info.getClassName();
                LogUtils.logWarning("Class '" + className + "' implements unknown interface '" + interfaceName + "'. Skipping.");
            }
        }
    }

    public void initPlugins() throws PluginInstantiationException {
        LogUtils.logMessage("Initialising plugins...");
        plugins.clear();

        String classPass = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");
        String[] classPathLocations = classPass.split(pathSeparator);

        List<Class<?>> classes = new ArrayList<>();
        ArrayList<LegacyPluginInfo> pluginInfos = new ArrayList<>();

        for (String s: classPathLocations) {
            LogUtils.logMessage("  Processing class path entry: " + s);
            classes.addAll(PluginFinder.search(new File(s)));
        }
        LogUtils.logMessage("" + classes.size() + " plugin(s) found.");

        for (Class<?> cls : classes) {
            final LegacyPluginInfo info = new LegacyPluginInfo(cls);
            pluginInfos.add(info);
            processLegacyPlugin(cls, info);
        }

        initModules();
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<PluginInfo<? extends T>> getPlugins(Class<T> interf) {
        return (Collection<PluginInfo<? extends T>>) (Collection<?>) Collections.unmodifiableCollection(plugins.get(interf));
    }

    public <T> void registerClass(Class<T> interf, final Class<? extends T> cls) {
        registerClass(interf, new Initialiser<T>() {
            @Override
            public T create() {
                try {
                    return cls.newInstance();
                } catch (InstantiationException e) {
                    Throwable q = e;
                    System.err.println(cls.getCanonicalName());
                    while (q != null) {
                        q.printStackTrace();
                        q = q.getCause();
                    }
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public <T> void registerClass(Class<T> interf, final Class<? extends T> cls, final Object ... constructorArgs) {
        registerClass(interf, new Initialiser<T>() {
            @Override
            public T create() {
                try {
                    Class<?>[] classes = new Class<?>[constructorArgs.length];
                    for (int i = 0; i < constructorArgs.length; i++) {
                        classes[i] = constructorArgs[i].getClass();
                    }
                    return new ConstructorParametersMatcher().match(cls, classes).newInstance(constructorArgs);
                } catch (InstantiationException | IllegalAccessException | SecurityException |
                        NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public <T> void registerClass(Class<T> interf, Initialiser<? extends T> initialiser) {
        if (!interf.isInterface()) {
            throw new RuntimeException("'interf' argument must be an interface");
        }
        final PluginInfo<T> pluginInfo = new PluginInstanceHolder<>(initialiser);
        plugins.put(interf, pluginInfo);
    }

}
