package org.workcraft;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.workcraft.exceptions.PluginInstantiationException;

public class PluginFinder {

	static public List<Class<?>> search(File starting) throws PluginInstantiationException {
		List<Class<?>> result = new ArrayList<Class<?>>();

		search(starting, starting, result);

		return result;
	}

	private static class ClassFileFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			File f = new File(dir.getPath() + File.separator + name);
			if (f.isDirectory())
				return true;
			if (f.getPath().endsWith(".class"))
				return true;
			return false;
		}
	}

	private static ClassFileFilter classFilter = new ClassFileFilter();

	private static void search(File starting, File current, List<Class<?>> result) throws PluginInstantiationException {
		if (!current.exists())
			return;

		if (current.isDirectory()) {
			File[] list = current.listFiles(classFilter);

			for (File f : list)
				if (f.isDirectory())
					search(starting, f, result);
				else
					processPathEntry(f.getPath().substring(starting.getPath().length()), result);
		} else if (current.isFile()) {
			if (current.getPath().endsWith(".jar"))
				try {
					JarFile jf = new JarFile(current);
					Enumeration<JarEntry> entries = jf.entries();

					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						processPathEntry(entry.getName(), result);
					}

				} catch (IOException e) {
					throw new PluginInstantiationException(e);
				}
		}
	}

	private static void processPathEntry(String path, List<Class<?>> result) throws PluginInstantiationException {
		if (!path.endsWith(".class"))
			return;

		String className;

		if (path.startsWith(File.separator))
			className = path.substring(File.separator.length());
		else
			className = path;

		className = className.replace(File.separatorChar, '.').replace('/', '.');

		if (!className.startsWith("org.workcraft.plugins"))
			return;

		className = className.substring(0, className.length() - ".class".length());

		try {
			Class<?> cls = Class.forName(className);

			if (!Modifier.isAbstract(cls.getModifiers())) {
				if (Plugin.class.isAssignableFrom(cls)) {
					try {
						cls.getConstructor();
						result.add(cls);
						System.out.println("plugin " + cls.getName());
					} catch (NoSuchMethodException ex) {
						System.out.println("plugin " + cls.getName() + " does not have a default constructor. skipping.");
					}
				}
			}

		} catch (ClassFormatError e) {
			System.out.println("bad class: " + e.getMessage());
		} catch (LinkageError e) {
			System.out.println("bad class: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new PluginInstantiationException(e);
		}
	}
}
