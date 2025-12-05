package org.workcraft.plugins;

import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginFinder {

    private static final class ClassFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            File f = new File(dir.getPath() + File.separator + name);
            if (f.isDirectory()) {
                return true;
            }
            return f.getPath().endsWith(".class");
        }
    }

    private static final ClassFileFilter classFilter = new ClassFileFilter();

    public static List<Class<?>> search(String filePath, String requiredPrefix)
            throws PluginInstantiationException {

        File file = new File(filePath);
        return new ArrayList<>(search(file, file, requiredPrefix));
    }


    private static List<Class<?>> search(File startingFile, File currentFile, String requiredPrefix)
            throws PluginInstantiationException {

        List<Class<?>> result = new ArrayList<>();
        if (currentFile.exists()) {
            if (currentFile.isDirectory()) {
                File[] list = currentFile.listFiles(classFilter);
                for (File nextFile : list) {
                    if (nextFile.isDirectory()) {
                        result.addAll(search(startingFile, nextFile, requiredPrefix));
                    } else {
                        String path = nextFile.getPath().substring(startingFile.getPath().length());
                        result.addAll(processPathEntry(path, requiredPrefix));
                    }
                }
            } else if (currentFile.isFile() && currentFile.getPath().endsWith(".jar")) {
                try {
                    JarFile jf = new JarFile(currentFile);
                    Enumeration<JarEntry> entries = jf.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        result.addAll(processPathEntry(entry.getName(), requiredPrefix));
                    }

                } catch (IOException e) {
                    throw new PluginInstantiationException(e);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("PMD.UselessPureMethodCall")
    private static List<Class<?>> processPathEntry(String path, String requiredPrefix)
            throws PluginInstantiationException {

        List<Class<?>> result = new ArrayList<>();
        if (path.endsWith(".class")) {
            String className;
            if (path.startsWith(File.separator)) {
                className = path.substring(File.separator.length());
            } else {
                className = path;
            }

            className = className.replace(File.separatorChar, '.').replace('/', '.');
            if (className.startsWith(requiredPrefix)) {
                className = className.substring(0, className.length() - ".class".length());
                try {
                    Class<?> cls = Class.forName(className);
                    if (!Modifier.isAbstract(cls.getModifiers()) && Plugin.class.isAssignableFrom(cls)) {
                        try {
                            cls.getConstructor();
                            result.add(cls);
                        } catch (NoSuchMethodException ex) {
                            LogUtils.logWarning("Plugin '" + cls.getName() + "' does not have a default constructor. Skipping.");
                        }
                    }
                } catch (LinkageError e) {
                    LogUtils.logError("Bad class: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    throw new PluginInstantiationException(e);
                }
            }
        }
        return result;
    }

}
