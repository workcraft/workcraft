package org.workcraft.plugins;

import org.workcraft.annotations.DisplayName;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class LegacyPluginInfo implements Initialiser<Object> {
    private String displayName;
    private final String className;
    private final String[] interfaceNames;

    private void addInterfaces(Class<?> cls, Set<String> set) {
        if (cls == null || cls.equals(Object.class)) {
            return;
        }

        for (Class<?> interf : cls.getInterfaces()) {
            set.add(interf.getName());
            addInterfaces(interf, set);
        }

        addInterfaces(cls.getSuperclass(), set);
    }

    public LegacyPluginInfo(final Class<?> cls) {
        className = cls.getName();

        DisplayName name = cls.getAnnotation(DisplayName.class);

        if (name == null) {
            displayName = className.substring(className.lastIndexOf('.') + 1);
        } else {
            displayName = name.value();
        }

        HashSet<String> interfaces = new HashSet<>();
        addInterfaces(cls, interfaces);
        interfaceNames = interfaces.toArray(new String[0]);
    }

    @Override
    public Object create() {
        try {
            return loadClass().getConstructor().newInstance();
        } catch (IllegalArgumentException
                | SecurityException | InstantiationException
                | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> loadClass() throws ClassNotFoundException {
        return Class.forName(className);
    }

    public String[] getInterfaces() {
        return interfaceNames.clone();
    }


    public String getDisplayName() {
        return displayName;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
