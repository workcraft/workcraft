package org.workcraft.framework.plugins;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.Component;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.visual.VisualComponentGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.DocumentFormatException;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

// TODO check for all documents to be closed before loadManifest or reconfigure

public class PluginManager {
	public static final String DEFAULT_MANIFEST = "config"+File.separator+"plugins.xml";
	public static final String INTERNAL_PLUGINS_PATH[] = {
		"bin"+ File.separator + "org" + File.separator + "workcraft" + File.separator + "plugins",
		"org" + File.separator + "workcraft" + File.separator + "plugins",
	};

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
		if (!xmlroot.getNodeName().equals("workcraft-plugins"))
			throw(new DocumentFormatException());

		NodeList nl = xmlroot.getElementsByTagName("plugin");
		plugins.clear();
		for(int i = 0; i < nl.getLength(); i++) {
			PluginInfo info = new PluginInfo((Element) nl.item(i));
			plugins.add(info);
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

		XmlUtil.saveDocument(doc, path);
	}

	private void search(File root) {
		if(!root.exists())
			return;

		if(root.isDirectory()) {
			File[] list = root.listFiles(classFilter);

			for(File f : list)
				if(f.isDirectory())
					search(f);
				else {
					System.out.println("Class file found: " + f.getPath());
					Class<?> cls;
					try {
						cls = activeLoader.findClass(f);

						if(Plugin.class.isAssignableFrom(cls)) {
							PluginInfo info = new PluginInfo(cls);
							plugins.add(info);
							System.out.println("  plugin found: " + cls.getName());
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

	public void reconfigure() {
		System.out.println("Reconfiguring plugins...");
		plugins.clear();
		activeLoader = new PluginClassLoader();
		for (String path : INTERNAL_PLUGINS_PATH)
			search(new File(path));
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

	public PluginInfo[] getModels() {
		return getPlugins (MathModel.class);
	}

	public PluginInfo[] getPluginsByInterface(String interfaceName) {
		LinkedList<PluginInfo> list = new LinkedList<PluginInfo>();
		for(PluginInfo info : plugins)
			for (String s: info.getInterfaces())
				if (s.equals(interfaceName))
					list.add(info);
		return list.toArray(new PluginInfo[0]);
	}

	public PluginInfo[] getPluginsBySuperclass(String superclassName) {
		LinkedList<PluginInfo> list = new LinkedList<PluginInfo>();
		for(PluginInfo info : plugins)
			for (String s: info.getSuperclasses())
				if (s.equals(superclassName))
					list.add(info);
		return list.toArray(new PluginInfo[0]);
	}

	public PluginInfo[] getPlugins (Class<?> parent) {
		if (parent.isInterface())
			return getPluginsByInterface(parent.getName());
		else
			return getPluginsBySuperclass(parent.getName());

	}

	public Object getInstance(PluginInfo info, Class<?>expectedClass) throws PluginInstantiationException {
		boolean useFramework = true;
		Class<?> cls;

		try {
			cls = info.loadClass();
		} catch (ClassNotFoundException e) {
			throw new PluginInstantiationException ("Class not found: " + info.getClassName() + "(" + e.getMessage()+ ")");
		}

		if (!expectedClass.isAssignableFrom(cls))
			throw new PluginInstantiationException ("plugin class " + cls.getName() + ", is not inherited from expected class "
					+ expectedClass.getName());

		Constructor<?> ctor = null;

		try {
			ctor = cls.getConstructor(new Class<?>[] { Framework.class });
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}

		if (ctor == null)
			try {
				useFramework = false;
				ctor = cls.getConstructor(new Class<?>[] { });
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			}


			if (ctor == null)
				throw new PluginInstantiationException ("Plugin class \"" + cls.getName() + "\" does not define an appropriate constructor or the constructor is inaccessible. " +
				"A constructor which takes a Framework argument, or a constructor without agruments must be accessible.");


			try {
				Object ret;

				if (useFramework)
					ret = ctor.newInstance(framework);
				else
					ret = ctor.newInstance();

				return ret;
			} catch (IllegalArgumentException e) {
				throw new PluginInstantiationException ("Plugin class \"" + cls.getName() + "\" could not be instantiated: \n" + e.getMessage());
			} catch (InstantiationException e) {
				throw new PluginInstantiationException ("Plugin class \"" + cls.getName() + "\" could not be instantiated: \n" + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new PluginInstantiationException ("Plugin class \"" + cls.getName() + "\" could not be instantiated: \n" + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new PluginInstantiationException ("Plugin class \"" + cls.getName() + "\" could not be instantiated: \n" + e.getMessage());
			}
	}

	public Object getSingleton(PluginInfo info, Class<?> expectedClass) throws PluginInstantiationException {
		Object ret = singletons.get(info.getClassName());
		if (ret == null) {
			ret = getInstance(info, expectedClass);
			singletons.put(info.getClassName(), ret);
		}
		return ret;
	}

	public static int getHotKeyCodeForClass (Class <? extends Component> cls) {
		// Find the corresponding visual class
		VisualClass vcat = cls.getAnnotation(VisualClass.class);
		// The component/connection does not define a visual representation
		if (vcat == null)
			return -1;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			HotKeyDeclaration hkd = visualClass.getAnnotation(HotKeyDeclaration.class);
			if (hkd == null)
				return -1;
			else
				return hkd.value();

		} catch (ClassNotFoundException e) {
			return -1;
		}

	}

	public static VisualModel createVisualModel (MathModel model) throws VisualModelConstructionException {
		// Find the corresponding visual class
		VisualClass vcat = model.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(model.getClass());
			Object visual = ctor.newInstance(model);

			if (!VisualModel.class.isAssignableFrom(visual.getClass()))
				throw new VisualModelConstructionException ("visual class " + visual.getClass().getName() +
						", created for object of class " + model.getClass().getName() + ", is not inherited from "
						+ VisualModel.class.getName());

			return (VisualModel)visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be loaded for class " + model.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception:\n" + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelConstructionException("visual class " + vcat.value() +
					" does not declare the required constructor: \n" + vcat.value() +
					"(" + model.getClass().getName() +")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n" + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}

	public static VisualModel createVisualModel (MathModel model, Element xmlElement) throws VisualModelConstructionException {
		// Find the corresponding visual class
		VisualClass vcat = model.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(model.getClass(), Element.class);
			Object visual = ctor.newInstance(model, xmlElement);

			if (!VisualModel.class.isAssignableFrom(visual.getClass()))
				throw new VisualModelConstructionException ("visual class " + visual.getClass().getName() +
						", created for object of class " + model.getClass().getName() + ", is not inherited from "
						+ VisualModel.class.getName());

			return (VisualModel)visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be loaded for class " + model.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception: \n " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelConstructionException("visual class " + vcat.value() +
					" does not declare the required constructor \n" + vcat.value() +
					"(" + model.getClass().getName() + ", " + Element.class.getName()+")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getTargetException().getMessage());
		}
	}

	public static Object createVisualComponent (Object component, VisualComponentGroup parent  ) throws VisualModelConstructionException {
		// Find the corresponding visual class
		VisualClass vcat = component.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(component.getClass(), VisualComponentGroup.class);
			Object visual = ctor.newInstance(component, parent);

			if (!VisualNode.class.isAssignableFrom(visual.getClass()))
				throw new VisualModelConstructionException ("visual class " + visual.getClass().getName() +
						", created for object of class " + component.getClass().getName() + ", is not inherited from "
						+ VisualNode.class.getName());

			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be loaded for class " + component.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception:\n" + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelConstructionException("visual class " + vcat.value() +
					" does not declare the required constructor: \n" + vcat.value() +
					"(" + component.getClass().getName() +"," + VisualComponentGroup.class.getName()+")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n" + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}



	public static Object createVisualComponent (Object component, Element element, VisualComponentGroup parent  ) throws VisualModelConstructionException {
		// Find the corresponding visual class
		VisualClass vcat = component.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(component.getClass(), Element.class, VisualComponentGroup.class);
			Object visual = ctor.newInstance(component, element, parent);

			if (!VisualNode.class.isAssignableFrom(visual.getClass()))
				throw new VisualModelConstructionException ("visual class " + visual.getClass().getName() +
						", created for object of class " + component.getClass().getName() + ", is not inherited from "
						+ VisualNode.class.getName());

			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be loaded for class " + component.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception:\n" + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelConstructionException("visual class " + vcat.value() +
					" does not declare the required constructor: \n" + vcat.value() +
					"(" + component.getClass().getName() +"," + Element.class.getName() + ","+ VisualComponentGroup.class.getName()+")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n" + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}
}
