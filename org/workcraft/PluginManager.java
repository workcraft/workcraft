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
import org.workcraft.exceptions.DocumentFormatException;
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

	/*private class PluginClassLoader extends ClassLoader {
		protected Class<?> findClass(File f) throws ClassNotFoundException, ClassFormatError,
		IOException {
			InputStream in = null;
			ByteArrayOutputStream data = new ByteArrayOutputStream();

			ClassLoader.getSystemClassLoader().

			try {
				in = new BufferedInputStream(new FileInputStream(f));
				for(int avail = in.available(); avail > 0; avail = in.available()) {
					byte[] buf = new byte[avail];
					in.read(buf, 0, avail);
					data.write(buf);
				}
			} catch(IOException e) {
				if(in != null)
					in.close();
				throw e;
			}

			return defineClass(null, data.toByteArray(), 0, data.size());
		}
	}*/

	private ClassFileFilter classFilter = new ClassFileFilter();
	private LinkedList<PluginInfo> plugins = new LinkedList<PluginInfo>();
	private HashMap<String, PluginInfo> nameToInfoMap = new HashMap<String, PluginInfo>();

	//private PluginClassLoader activeLoader = null;

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

	public void loadManifest() throws IOException, DocumentFormatException, PluginInstantiationException {
		loadManifest(DEFAULT_MANIFEST);
	}

	public void loadManifest(String path) throws IOException, DocumentFormatException, PluginInstantiationException {
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
			throw new DocumentFormatException();
		} catch(IOException e) {
			throw new IOException(e.getMessage());
		} catch(SAXException e) {
			throw new IOException(e.getMessage());
		}

		Element xmlroot = doc.getDocumentElement();
		if (!xmlroot.getNodeName().equals("workcraft-plugins"))
			throw(new DocumentFormatException());

		NodeList nl = xmlroot.getElementsByTagName("plugin");
		plugins.clear();
		for(int i = 0; i < nl.getLength(); i++) {
			PluginInfo info = new PluginInfo((Element) nl.item(i));
			plugins.add(info);
			nameToInfoMap.put(info.getClassName(), info);
		}
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

	private void processPathEntry (String path) throws PluginInstantiationException {
		if (!path.endsWith(".class"))
			return;


		String className;

		if (path.startsWith(File.separator))
			className = path.substring(File.separator.length());
		else
			className = path;

		className = className.replace(File.separator, ".");

		if (!className.startsWith("org.workcraft.plugins"))
			return;

		System.out.print(".class file found: " + path + ", ");


		className = className.substring(0, className.length() - ".class".length());

		try {
			Class<?> cls = Class.forName(className);


			if(Plugin.class.isAssignableFrom(cls)) {
				PluginInfo info = new PluginInfo(cls);
				plugins.add(info);
				System.out.println("plugin " + cls.getName());
			} else
				System.out.println("not a plugin class, ignored");

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

		System.out.println("" + plugins.size() + " plugin(s) found");

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
			Class<?> cls = info.loadClass();
			Constructor<?> ctor = cls.getConstructor(new Class<?>[] { });

			if (ctor == null)
				throw new PluginInstantiationException ("A constructor without agruments must be accessible.");

			Plugin instance = (Plugin) ctor.newInstance();

			if (instance instanceof ConfigurablePlugin)
				((ConfigurablePlugin)instance).readConfig(framework.getConfig());

			if (instance instanceof PluginConsumer)
				((PluginConsumer)instance).processPlugins(this);

			return instance;

		} catch (ClassNotFoundException e) {
			throw new PluginInstantiationException ("Class not found: " + info.getClassName() + "(" + e.getMessage()+ ")", e);
		} catch (SecurityException e) {
			throw new PluginInstantiationException(e);
		} catch (NoSuchMethodException e) {
			throw new PluginInstantiationException(e);
		} catch (IllegalArgumentException e) {
			throw new PluginInstantiationException(e);
		} catch (InstantiationException e) {
			throw new PluginInstantiationException(e);
		} catch (IllegalAccessException e) {
			throw new PluginInstantiationException(e);
		} catch (InvocationTargetException e) {
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

	/* (non-Javadoc)
	 * @see org.workcraft.framework.plugins.PluginProvider#getSingletonByName(java.lang.String)
	 */
	public Object getSingletonByName(String className) throws PluginInstantiationException {
		PluginInfo info = nameToInfoMap.get(className);
		if (info == null)
			throw new PluginInstantiationException("Cannot create singleton, plugin " + className + " not found!");
		return getSingleton(info);
	}
}
