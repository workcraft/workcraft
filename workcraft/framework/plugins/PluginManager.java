package org.workcraft.framework.plugins;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.framework.exceptions.DocumentFormatException;
import org.workcraft.framework.exceptions.InvalidPluginException;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

// TODO check for all documents to be closed before loadManifest or reconfigure

public class PluginManager {
	public static final String DEFAULT_MANIFEST = "plugins.xml";
	public static final String INTERNAL_PLUGINS_PATH = "org" + File.separator + "workcraft"
			+ File.separator + "plugins";
	public static final String EXTERNAL_PLUGINS_PATH = "plugins";

	private class ClassFileFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			File f = new File(dir.getPath() + File.separator + name);
			if(f.isDirectory()) {
				return true;
			}
			if(f.getPath().endsWith(".class"))
				return true;
			return false;
		}
	}

	private class PluginClassLoader extends ClassLoader {
		protected Class<?> findClass(File f) throws ClassNotFoundException, ClassFormatError,
				IOException {
			InputStream in = null;
			ByteArrayOutputStream data = new ByteArrayOutputStream();

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
	}

	private ClassFileFilter classFilter = new ClassFileFilter();
	private LinkedList<PluginInfo> plugins = new LinkedList<PluginInfo>();
	private PluginClassLoader activeLoader = null;

	public PluginManager() {
		try {
			loadManifest();
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void printPluginList() {
		System.out.println("Registered plugins:");
		for(PluginInfo info : plugins) {
			System.out.println(" "+info.getClassName());
		}
		System.out.println(""+plugins.size()+" plugin(s) total");
	}

	public void loadManifest() throws IOException, DocumentFormatException {
		loadManifest(DEFAULT_MANIFEST);
	}

	public void loadManifest(String path) throws IOException, DocumentFormatException {
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
		if(!xmlroot.getNodeName().equals("workcraft")) {
			throw(new DocumentFormatException());
		}
		NodeList nl = xmlroot.getElementsByTagName("pluginManifest");
		Element d = (Element) nl.item(0);
		if(d == null)
			throw(new DocumentFormatException());

		nl = d.getElementsByTagName("plugin");
		plugins.clear();
		for(int i = 0; i < nl.getLength(); i++) {
			try {
				PluginInfo info = new PluginInfo((Element) nl.item(i));
				plugins.add(info);
			} catch(InvalidPluginException e) {
				throw new DocumentFormatException();
			}
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

		Element root = doc.createElement("workcraft");
		doc.appendChild(root);
		root = doc.getDocumentElement();

		Element man = doc.createElement("pluginManifest");
		root.appendChild(man);
		for(PluginInfo info : plugins) {
			Element e = doc.createElement("plugin");
			info.toXml(e);
			man.appendChild(e);
		}

		XmlUtil.saveDocument(doc, path);
	}

	@SuppressWarnings("unchecked")
	private void search(File root) {
		if(!root.exists())
			return;

		if(root.isDirectory()) {
			File[] list = root.listFiles(classFilter);

			for(File f : list) {
				if(f.isDirectory())
					search(f);
				else {
					System.out.println("Class file found: " + f.getPath());
					Class<?> cls;
					try {
						cls = activeLoader.findClass(f);

						if(Plugin.class.isAssignableFrom(cls)) {
							try {
								PluginInfo info = new PluginInfo(cls);
								plugins.add(info);
								System.out.println("  plugin found: " + cls.getName());
							} catch(InvalidPluginException e) {
								System.out.println("  invalid plugin: " + e.getMessage());
							}
						} else
							System.out.println("  not a plugin class, ignored");

					} catch(ClassFormatError e) {
						System.out.println("  error loading class \"" + f.getName() + "\": "
								+ e.getMessage());
					} catch(ClassNotFoundException e) {
						System.out.println("  error loading class \"" + f.getName() + "\": "
								+ e.getMessage());
					} catch(IOException e) {
						System.out.println("  error loading class \"" + f.getName() + "\": "
								+ e.getMessage());
					}

				}
			}
		}
	}

	public void reconfigure() {
		System.out.println("Reconfiguring plugins...");
		plugins.clear();
		activeLoader = new PluginClassLoader();
		search(new File(INTERNAL_PLUGINS_PATH));
		search(new File(EXTERNAL_PLUGINS_PATH));
		activeLoader = null;

		System.out.println("" + plugins.size() + " plugin(s) found");

		try {
			saveManifest();
			System.out.println("Reconfiguration complete.");
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public PluginInfo[] getPlugins(PluginInfo.Type type, String appliedTo) {
		LinkedList<PluginInfo> list = new LinkedList<PluginInfo>();
		for(PluginInfo info : plugins) {
			if(info.getType() != type)
				continue;
			if(appliedTo == null) {
				list.add(info);
			} else if(info.getAppliedTo() != null) {
				for(String s : info.getAppliedTo()) {
					if(appliedTo.equals(s)) {
						list.add(info);
						break;
					}
				}
			}
		}
		return (PluginInfo[]) list.toArray();
	}

	@SuppressWarnings("unchecked")
	public Plugin getPluginInstance(Class<?> cls) {
		try {
			Class<Plugin> plugin = (Class<Plugin>) cls;
			Method getInstance = plugin.getMethod("getInstance");
			return (Plugin) getInstance.invoke(null);
		} catch(ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
