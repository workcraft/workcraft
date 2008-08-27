package org.workcraft.framework;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.gui.MainWindow;
import org.xml.sax.SAXException;

public class Framework {
	public static final String FRAMEWORK_VERSION_MAJOR = "2";
	public static final String FRAMEWORK_VERSION_MINOR = "dev";

	class JavaScriptExecution implements ContextAction {
		Script script;
		Scriptable scope;
		String strScript;

		public void setScope (Scriptable scope) {
			this.scope = scope;
		}

		public void setScript (Script script) {
			this.script = script;
		}

		public void setScript (String strScript) {
			this.strScript = strScript;
		}

		public Object run(Context cx) {
			Object ret;
			if (script != null)
				ret = script.exec(cx, scope);
			else
				ret = cx.evaluateString(scope, strScript, "<string>", 1, null);
			script = null;
			scope = null;
			strScript = null;
			return ret;
		}
	}
	class JavaScriptCompilation implements ContextAction {
		String source, sourceName;
		BufferedReader reader;

		public void setSource (String source) {
			this.source = source;
		}

		public void setSource (BufferedReader reader) {
			this.reader = reader;
		}

		public void setSourceName (String sourceName) {
			this.sourceName = sourceName;

		}

		public Object run(Context cx) {
			Object ret;
			if (source!=null)
				ret = cx.compileString(source, sourceName, 1, null);
			else
				try {
					ret = cx.compileReader(reader, sourceName, 1, null);
				} catch (IOException e) {
					e.printStackTrace();
					ret = null;
				}
				source = null;
				sourceName = null;
				return ret;
		}
	}

	class SetArgs implements ContextAction {
		Object[] args;

		public void setArgs (Object[] args) {
			this.args = args;
		}

		public Object run(Context cx) {
			Object scriptable = Context.javaToJS(args, systemScope);
			ScriptableObject.putProperty(systemScope, "args", scriptable);
			systemScope.setAttributes("args", ScriptableObject.READONLY);
			return null;

		}
	}

	private PluginManager pluginManager = new PluginManager();
	private ModelManager modelManager = new ModelManager();
	private Config config = new Config();
	private Workspace workspace = new Workspace(this);
	private HashMap <String, Tool> toolInstances = new HashMap <String, Tool>();

	private ScriptableObject systemScope;
	private Scriptable globalScope;

	private JavaScriptExecution javaScriptExecution = new JavaScriptExecution();
	private JavaScriptCompilation javaScriptCompilation = new JavaScriptCompilation();

	private boolean inGUIMode = false;
	private boolean shutdownRequested = false;
	private boolean silent = false;

	private Stack <HistoryEvent> undoStack = new Stack<HistoryEvent>();
	private Stack <HistoryEvent> redoStack = new Stack<HistoryEvent>();
	private MainWindow mainFrame;

	/*	public void loadPlugins(String directory) {
		System.out.println("Loading plugin class manifest from \""+directory+"\"\t ...");

		pluginManager.loadManifest(directory);

		System.out.println("Verifying plugin classes\t ...");
		System.out.println("Models:");

		LinkedList<Class> models = pluginManager.getClassesBySuperclass(Document.class);
		for (Class cls : models) {
			modelManager.addModel(cls);
		}

		System.out.println("Components:");

		LinkedList<Class> components = pluginManager.getClassesBySuperclass(Component.class);
		for (Class cls : components) {
			modelManager.addComponent(cls);
		}

		LinkedList<Class> tools = pluginManager.getClassesByInterface(Tool.class);
		System.out.println("Tools:");
		for (Class cls : tools) {
			modelManager.addTool(cls, this);
		}
		System.out.println ("Load complete.\n");
	}*/

	public void loadConfig(String fileName) {
		config.load(fileName);
	}

	public void saveConfig(String fileName) {
		config.save(fileName);
	}

	public void setConfigVar (String key, String value) {
		config.set(key, value);
	}

	public void setConfigVar (String key, int value) {
		config.set(key, Integer.toString(value));
	}

	public void setConfigVar (String key, boolean value) {
		config.set(key, Boolean.toString(value));
	}

	public String getConfigVar (String key) {
		return config.get(key);
	}

	public int getConfigVarAsInt (String key, int defaultValue)  {
		String s = config.get(key);

		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public boolean getConfigVarAsBool (String key, boolean defaultValue)  {
		String s = config.get(key);

		if (s == null)
			return defaultValue;
		else
			return Boolean.parseBoolean(s);
	}


	public void registerToolInstance (Tool tool) {
		toolInstances.put(tool.getClass().getName(), tool);
	}

	public Tool getToolInstance (String className) {
		return (Tool) toolInstances.get(className);
	}

	public Tool getToolInstance (Class cls) {
		return getToolInstance (cls.getName());
	}


	public String[] getModelNames() {
		LinkedList<Class> list = modelManager.getModelList();
		String a[] = new String[list.size()];
		int i=0;
		for (Class cls : list)
			a[i++] = cls.getName();
		return a;
	}

	public void initJavaScript() {
		if (!silent)
			System.out.println ("Initialising javascript...");
		Context.call(new ContextAction() {
			public Object run(Context cx) {
				ImporterTopLevel importer = new ImporterTopLevel();
				importer.initStandardObjects(cx, false);
				systemScope = importer;

				//systemScope.initStandardObjects();
				//systemScope.setParentScope(

				Object frameworkScriptable = Context.javaToJS(Framework.this, systemScope);
				ScriptableObject.putProperty(systemScope, "framework", frameworkScriptable);
				//ScriptableObject.putProperty(systemScope, "importer", );
				systemScope.setAttributes("framework", ScriptableObject.READONLY);

				globalScope = cx.newObject(systemScope);
				globalScope.setPrototype(systemScope);
				globalScope.setParentScope(null);

				return null;

			}
		});
	}

	public Scriptable getJavaScriptGlobalScope() {
		return globalScope;
	}

	public Object execJavaScript(File file) throws FileNotFoundException {
		BufferedReader reader = new BufferedReader (new FileReader(file));
		return execJavaScript(compileJavaScript(reader, file.getPath()));
	}

	public Object execJavaScript(Script script) {
		return execJavaScript (script, globalScope);
	}

	public Object execJavaScript(Script script, Scriptable scope) {
		javaScriptExecution.setScript(script);
		javaScriptExecution.setScope(scope);
		return Context.call(javaScriptExecution);
	}

	public Object execJavaScript(String script, Scriptable scope) {
		javaScriptExecution.setScript(script);
		javaScriptExecution.setScope(scope);
		return Context.call(javaScriptExecution);
	}

	public Object execJavaScript (String script) {
		return execJavaScript(script, globalScope);
	}

	public Script compileJavaScript (String source, String sourceName) {
		javaScriptCompilation.setSource(source);
		javaScriptCompilation.setSourceName(sourceName);
		return (Script) Context.call(javaScriptCompilation);
	}

	public Script compileJavaScript (BufferedReader source, String sourceName) {
		javaScriptCompilation.setSource(source);
		javaScriptCompilation.setSourceName(sourceName);
		return (Script) Context.call(javaScriptCompilation);
	}

	public void startGUI() {
		if (inGUIMode) {
			System.out.println ("Already in GUI mode");
			return;
		}

		System.out.println ("Switching to GUI mode...");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainFrame = new MainWindow(Framework.this);
				mainFrame.startup();
				System.out.println ("Now in GUI mode.");
			}
		});

		inGUIMode = true;
	}

	public void shutdownGUI() {
		if (inGUIMode) {
			mainFrame.shutdown();
			mainFrame.dispose();
			mainFrame = null;
			inGUIMode = false;
			System.out.println ("Now in console mode.");
		}
	}

	public void shutdown() {
		shutdownRequested = true;
	}

	public boolean shutdownRequested() {
		return shutdownRequested;
	}

	public MainWindow getMainFrame() {
		return mainFrame;
	}

	public ModelManager getModelManager() {
		return modelManager;
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public boolean isInGUIMode() {
		return inGUIMode;
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public void setArgs(List<String> args) {
		SetArgs setargs = new SetArgs();
		setargs.setArgs(args.toArray());
		Context.call(setargs);
	}

	public AbstractGraphModel load(String path) throws ModelLoadFailedException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			org.w3c.dom.Document xmldoc;
			AbstractGraphModel model;
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();
			xmldoc = db.parse(new File(path));

			Element xmlroot = xmldoc.getDocumentElement();

			if (xmlroot.getNodeName()!="workcraft")
				throw new ModelLoadFailedException("not a Workcraft document");

			String[] ver = xmlroot.getAttribute("version").split("\\.", 2);

			if (ver.length<2 || !ver[0].equals(FRAMEWORK_VERSION_MAJOR))
				throw new ModelLoadFailedException("document was created by an incompatible version of Workcraft");

			NodeList elements;
			elements =  xmlroot.getElementsByTagName("model");
			if (elements.getLength() != 1)
				throw new ModelLoadFailedException("<model> section missing or duplicated");

			Element modelElement = (Element)elements.item(0);
			String modelClassName = modelElement.getAttribute("class");

			Class<?> modelClass = Class.forName(modelClassName);
			Constructor<?> ctor = modelClass.getConstructor(Framework.class, Element.class, String.class);
			model = (AbstractGraphModel)ctor.newInstance(this, modelElement, path);

			return model;

		} catch (ParserConfigurationException e) {
			throw new ModelLoadFailedException("XML Parser configuration error: "+ e.getMessage());
		} catch (IOException e) {
			throw new ModelLoadFailedException("IO error: "+ e.getMessage());
		} catch (SAXException e) {
			throw new ModelLoadFailedException("Parse error: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (SecurityException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (InstantiationException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getTargetException().getMessage());
		} catch (NoSuchMethodException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new ModelLoadFailedException("Cannot instatniate model: " + e.getMessage());
		}
	}



	public void save(AbstractGraphModel model, String path) throws ModelSaveFailedException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc;
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();

			Element root = doc.createElement("workcraft");
			root.setAttribute("version", FRAMEWORK_VERSION_MAJOR+"."+FRAMEWORK_VERSION_MINOR);
			doc.appendChild(root);

			Element modelElement = doc.createElement("model");
			modelElement.setAttribute("class", model.getClass().getName());

			model.toXML(modelElement);

			root.appendChild(modelElement);

			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setAttribute("indent-number", new Integer(2));
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			FileOutputStream fos = new FileOutputStream(path);

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new OutputStreamWriter(fos));

			transformer.transform(source, result);
			fos.close();
		} catch (ParserConfigurationException e) {
			throw new ModelSaveFailedException("XML Parser configuration error: "+ e.getMessage());
		} catch (TransformerConfigurationException e) {
			throw new ModelSaveFailedException("XML transformer configuration error: "+ e.getMessage());
		} catch (TransformerException e) {
			throw new ModelSaveFailedException("XML transformer error: "+ e.getMessage());
		} catch (FileNotFoundException e) {
			throw new ModelSaveFailedException("File not found: "+ e.getMessage());
		} catch (IOException e) {
			throw new ModelSaveFailedException("IO error: "+ e.getMessage());
		}
	}
}