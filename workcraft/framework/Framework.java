package org.workcraft.framework;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

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
import org.workcraft.dom.MathModel;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.DocumentFormatException;
import org.workcraft.framework.exceptions.ModelInstantiationException;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.exceptions.OperationCancelledException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.framework.workspace.Workspace;
import org.workcraft.gui.MainWindow;
import org.workcraft.util.XmlUtil;
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

	private PluginManager pluginManager;
	private ModelManager modelManager;
	private Config config ;
	private Workspace workspace;

	private ScriptableObject systemScope;
	private ScriptableObject globalScope;

	private JavaScriptExecution javaScriptExecution = new JavaScriptExecution();
	private JavaScriptCompilation javaScriptCompilation = new JavaScriptCompilation();

	private boolean inGUIMode = false;
	private boolean shutdownRequested = false;
	private boolean GUIRestartRequested = false;


	private boolean silent = false;

	private MainWindow mainWindow;

	public Framework() {
		pluginManager = new PluginManager(this);
		modelManager = new ModelManager();
		config = new Config();
		workspace = new Workspace(this);
		javaScriptExecution = new JavaScriptExecution();
		javaScriptCompilation = new JavaScriptCompilation();
	}


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

	public String[] getModelNames() {
		LinkedList<Class<?>> list = modelManager.getModelList();
		String a[] = new String[list.size()];
		int i=0;
		for (Class<?> cls : list)
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

				globalScope =(ScriptableObject) cx.newObject(systemScope);
				globalScope.setPrototype(systemScope);
				globalScope.setParentScope(null);

				return null;

			}
		});
	}

	public ScriptableObject getJavaScriptGlobalScope() {
		return globalScope;
	}

	public void setJavaScriptProperty (final String name, final Object object, final ScriptableObject scope, final boolean readOnly) {
		Context.call(new ContextAction(){
			public Object run(Context arg0) {
				Object scriptable = Context.javaToJS(object, scope);
				ScriptableObject.putProperty(scope, name, scriptable);

				if (readOnly)
					scope.setAttributes(name, ScriptableObject.READONLY);

				return scriptable;
			}
		});
	}

	public void deleteJavaScriptProperty (String name, ScriptableObject scope) {
		ScriptableObject.deleteProperty(scope, name);
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

		GUIRestartRequested = false;

		System.out.println ("Switching to GUI mode...");


		if (SwingUtilities.isEventDispatchThread()) {
			mainWindow = new MainWindow(Framework.this);
			mainWindow.startup();
		} else
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						mainWindow = new MainWindow(Framework.this);
						mainWindow.startup();
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			Context.call(new ContextAction() {
				public Object run(Context cx) {
					Object guiScriptable = Context.javaToJS(mainWindow, systemScope);
					ScriptableObject.putProperty(systemScope, "mainWindow", guiScriptable);
					systemScope.setAttributes("mainWindow", ScriptableObject.READONLY);
					return null;

				}
			});

			System.out.println ("Now in GUI mode.");
			inGUIMode = true;

	}

	public void shutdownGUI() throws OperationCancelledException {
		if (inGUIMode) {

			mainWindow.shutdown();
			mainWindow = null;
			inGUIMode = false;

			Context.call(new ContextAction() {
				public Object run(Context cx) {
					ScriptableObject.deleteProperty(systemScope, "mainWindow");
					return null;
				}
			});

		}
		System.out.println ("Now in console mode.");
	}

	public void shutdown() {
		shutdownRequested = true;
	}

	public boolean shutdownRequested() {
		return shutdownRequested;
	}

	public void abortShutdown() {
		shutdownRequested = false;
	}

	public MainWindow getMainWindow() {
		return mainWindow;
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

	public Model load(String path) throws ModelLoadFailedException {
		InputStream stream;
		try {
			stream = new FileInputStream(path);
			try	{
				return load(stream);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			throw new ModelLoadFailedException ("IO Exception: " + e.getMessage());
		}
	}

	public Model load(InputStream stream) throws ModelLoadFailedException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			org.w3c.dom.Document xmldoc;
			MathModel model;
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();
			xmldoc = db.parse(stream);

			Element xmlroot = xmldoc.getDocumentElement();

			if (xmlroot.getNodeName()!="workcraft")
				throw new ModelLoadFailedException("not a Workcraft document");

			String[] ver = xmlroot.getAttribute("version").split("\\.", 2);

			if (ver.length<2 || !ver[0].equals(FRAMEWORK_VERSION_MAJOR))
				throw new ModelLoadFailedException("Document was created by an incompatible version of Workcraft.");

			Element modelElement = XmlUtil.getChildElement("model", xmlroot);

			if (modelElement == null)
				throw new ModelLoadFailedException("<model> section is missing.");

			model = ModelFactory.createModel(modelElement);

			Element visualModelElement = XmlUtil.getChildElement("visual-model", xmlroot);
			if (visualModelElement == null)
				return model;

			return ModelFactory.createVisualModel(model, visualModelElement);
		} catch (ParserConfigurationException e) {
			throw new ModelLoadFailedException ("Parser configuration exception: " + e.getMessage());
		} catch (SAXException e) {
			throw new ModelLoadFailedException ("SAX exception: " + e.getMessage());
		} catch (IOException e) {
			throw new ModelLoadFailedException ("IO exception: " + e.getMessage());
		} catch (VisualModelInstantiationException e) {
			throw new ModelLoadFailedException ("Visual model could not be instantiated: " + e.getMessage());
		} catch (ModelInstantiationException e) {
			throw new ModelLoadFailedException ("Model could not be instantiated: " + e.getMessage());
		}
	}


	public void save(Model model, String path) throws ModelSaveFailedException {
		try {
			FileOutputStream stream = new FileOutputStream(path);
			try
			{
				save(model, stream);
			}
			finally
			{
				stream.close();
			}
		} catch (FileNotFoundException e) {
			throw new ModelSaveFailedException("File not found: "+ e.getMessage());
		} catch (IOException e) {
			throw new ModelSaveFailedException("IO error: "+ e.getMessage());
		}
	}

	public void save(Model model, OutputStream output) throws ModelSaveFailedException {
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
			modelElement.setAttribute("class", model.getMathModel().getClass().getName());
			model.getMathModel().serialiseToXML(modelElement);
			root.appendChild(modelElement);

			VisualModel visualModel = model.getVisualModel();

			if (visualModel != null) {
				Element visualModelElement = doc.createElement("visual-model");
				visualModelElement.setAttribute("class", visualModel.getClass().getName());
				visualModel.serialiseToXML(visualModelElement);
				root.appendChild(visualModelElement);
			}

			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setAttribute("indent-number", new Integer(2));
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			OutputStreamWriter writer = new OutputStreamWriter(output);
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			writer.close();
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

	public void initPlugins() {
		if (!silent)
			System.out.println ("Loading plugins configuration...");

		try {
			pluginManager.loadManifest();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentFormatException e) {
			e.printStackTrace();
		}

	}

	public void restartGUI() throws OperationCancelledException {
		GUIRestartRequested = true;
		shutdownGUI();
	}

	public boolean isGUIRestartRequested() {
		return GUIRestartRequested;
	}
}