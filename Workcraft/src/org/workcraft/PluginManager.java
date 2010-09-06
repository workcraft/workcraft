/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class PluginManager implements PluginProvider {
	public static final String DEFAULT_MANIFEST = "config"+File.separator+"plugins.xml";

	public static final String EXTERNAL_PLUGINS_PATH = "plugins";
	private Framework framework;
	private HashMap <String, Object> singletons;

	private class ClassFileFilter implements FilenameFilter {

		public boolean accept(File dir, String name) {
			File f = new File(dir.getPath() + File.separator + name);
			if(f.isDirectory())
				return true;
			if(f.getPath().endsWith(".class"))
				return true;
			return false;
		}
	}

	private ClassFileFilter classFilter = new ClassFileFilter();
	private LinkedList<PluginInfo> plugins = new LinkedList<PluginInfo>();
	private HashMap<String, PluginInfo> nameToInfoMap = new HashMap<String, PluginInfo>();

	public PluginManager(Framework framework) {
		this.framework = framework;
		singletons = new HashMap<String, Object>();
	}

	public void printPluginList() {
		System.out.println("Registered plugins:");
		for(PluginInfo info : plugins)
			System.out.println(" "+info.getClassName());
		System.out.println(""+plugins.size()+" plugin(s) total");
	}

	public void loadManifest() throws IOException, FormatException, PluginInstantiationException {
		loadManifest(DEFAULT_MANIFEST);
	}

	public void loadManifest(String path) throws IOException, FormatException, PluginInstantiationException {
		File f = new File(path);
		if(!f.exists()) {
			System.out.println("Plugin manifest \"" + f.getPath() + "\" does not exist.");
			reconfigure();
			return;
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(f);
		} catch(ParserConfigurationException e) {
			throw new FormatException();
		} catch(IOException e) {
			throw new IOException(e.getMessage());
		} catch(SAXException e) {
			throw new IOException(e.getMessage());
		}

		Element xmlroot = doc.getDocumentElement();
		if (!xmlroot.getNodeName().equals("workcraft-plugins"))
			throw(new FormatException());

		NodeList nl = xmlroot.getElementsByTagName("plugin");
		plugins.clear();
		for(int i = 0; i < nl.getLength(); i++) {
			PluginInfo info = new PluginInfo((Element) nl.item(i));
			plugins.add(info);
			nameToInfoMap.put(info.getClassName(), info);
		}

		initPlugins();
	}

	private void initPlugins() {
		final PluginInfo[] plugins = getPluginsImplementing(Plugin.class.getCanonicalName());
		for(PluginInfo info : plugins)
			((Plugin)info.createInstance()).init(framework);
	}

	public void saveManifest() throws IOException {
		saveManifest(DEFAULT_MANIFEST);
	}

	public void saveManifest(String path) throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc;
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch (ParserConfigurationException e) {
			System.err.println(e.getMessage());
			return;
		}

		Element root = doc.createElement("workcraft-plugins");
		doc.appendChild(root);
		root = doc.getDocumentElement();

		for(PluginInfo info : plugins) {
			Element e = doc.createElement("plugin");
			info.toXml(e);
			root.appendChild(e);
		}

		XmlUtil.saveDocument(doc, new File(path));
	}

	private void addPluginClass(Class<?> cls)
	{
		PluginInfo info = new PluginInfo(cls);
		plugins.add(info);
		nameToInfoMap.put(cls.getCanonicalName(), info);
	}

	private void processPathEntry (String path) throws PluginInstantiationException {
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

		//		System.out.print(".class file found: " + path + ", ");

		className = className.substring(0, className.length() - ".class".length());

		try {
			Class<?> cls = Class.forName(className);

			if (!Modifier.isAbstract(cls.getModifiers()))
			{
				if(LegacyPlugin.class.isAssignableFrom(cls)) {
					try
					{
						cls.getConstructor();
						addPluginClass(cls);
						System.out.println("legacy plugin " + cls.getName());
					}
					catch(NoSuchMethodException ex)
					{
						System.err.println("legacy plugin " + cls.getName() + " does not have a default constructor. skipping.");
					}
				}
				if (Plugin.class.isAssignableFrom(cls))
				{

					try {
						final Constructor<?> constructor = cls.getConstructor();
						final Plugin instance = (Plugin)constructor.newInstance();
						Class<?> [] classes = instance.getPluginClasses();
						System.out.println("plugin " + cls.getName() + ":");
						addPluginClass(cls);
						for(Class<?> pluginClass : classes)
						{
							addPluginClass(pluginClass);
							System.out.println("\tclass " + pluginClass.getName());
						}
					} catch (SecurityException e) {
						throw new RuntimeException(e);
					} catch (NoSuchMethodException e) {
						throw new RuntimeException(e);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}

		} catch (ClassFormatError e) {
			System.out.println ("bad class: " + e.getMessage());
		} catch (LinkageError e) {
			System.out.println ("bad class: " + e.getMessage());
		} catch(ClassNotFoundException e) {
			throw new PluginInstantiationException(e);
		}
	}

	private void search(File starting, File current) throws PluginInstantiationException {
		if(!current.exists())
			return;

		if(current.isDirectory()) {
			File[] list = current.listFiles(classFilter);

			for(File f : list)
				if(f.isDirectory())
					search(starting, f);
				else
					processPathEntry(f.getPath().substring(starting.getPath().length()));
		} else if (current.isFile()) {
			if (current.getPath().endsWith(".jar"))
				try {
					JarFile jf = new JarFile(current);
					Enumeration<JarEntry> entries = jf.entries();

					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						processPathEntry (entry.getName());
					}

				} catch (IOException e) {
					throw new PluginInstantiationException(e);
				}
		}
	}

	public void reconfigure() throws PluginInstantiationException {
		System.out.println("Reconfiguring plugins...");
		plugins.clear();

		String[] classPathLocations = System.getProperty("java.class.path").split(File.pathSeparator);

		for (String s: classPathLocations) {
			System.out.println (s);
			search(new File(s), new File(s));
		}

		System.out.println("" + plugins.size() + " plugin(s) found. Initialising them...");

		initPlugins();

		System.out.println("Plugin initialisation done.");

		try {
			saveManifest();
			System.out.println("Reconfiguration complete.");
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public PluginInfo[] getModels() {
		return getPluginsImplementing (Model.class.getName());
	}

	public PluginInfo[] getPlugins(Class<?> interf) {
		return getPluginsImplementing(interf.getName());
	}

	/* (non-Javadoc)
	 * @see org.workcraft.framework.plugins.PluginProvider#getPlugins(java.lang.String)
	 */
	public PluginInfo[] getPluginsImplementing(String interfaceName) {
		LinkedList<PluginInfo> list = new LinkedList<PluginInfo>();
		for(PluginInfo info : plugins)
			for (String s: info.getInterfaces())
				if (s.equals(interfaceName))
					list.add(info);
		return list.toArray(new PluginInfo[0]);
	}

	/* (non-Javadoc)
	 * @see org.workcraft.framework.plugins.PluginProvider#getInstance(org.workcraft.framework.plugins.PluginInfo)
	 */
	public Object getInstance(PluginInfo info) throws PluginInstantiationException {
		try {
			Object instance = info.createInstance();

			if (instance instanceof ConfigurablePlugin)
				((ConfigurablePlugin)instance).readConfig(framework.getConfig());

			return instance;

		} catch (SecurityException e) {
			throw new PluginInstantiationException(e);
		} catch (IllegalArgumentException e) {
			throw new PluginInstantiationException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.workcraft.framework.plugins.PluginProvider#getSingleton(org.workcraft.framework.plugins.PluginInfo)
	 */
	public Object getSingleton(PluginInfo info) throws PluginInstantiationException {
		Object ret = singletons.get(info.getClassName());
		if (ret == null) {
			ret = getInstance(info);
			singletons.put(info.getClassName(), ret);
		}
		return ret;

	}

	@SuppressWarnings("unchecked")
	public <T> T getSingleton(Class<? extends T> type) throws PluginInstantiationException
	{
		return (T)getSingletonByName(type.getCanonicalName());
	}

	/* (non-Javadoc)
	 * @see org.workcraft.framework.plugins.PluginProvider#getSingletonByName(java.lang.String)
	 */
	public Object getSingletonByName(String className) throws PluginInstantiationException {
		PluginInfo info = nameToInfoMap.get(className);
		if (info == null)
			throw new PluginInstantiationException("Cannot create singleton, plugin " + className + " not found!");
		return getSingleton(info);
	}

	public void registerClass(Class<?> cls, Initialiser initialiser) {
		final PluginInfo pluginInfo = new PluginInfo(cls, initialiser);
		nameToInfoMap.put(cls.getCanonicalName(), pluginInfo);
		plugins.add(pluginInfo);
	}
}
