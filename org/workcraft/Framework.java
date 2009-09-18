package org.workcraft;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.DocumentFormatException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.plugins.serialisation.XMLDeserialiser;
import org.workcraft.plugins.serialisation.XMLSerialiser;
import org.workcraft.serialisation.DeserialisationResult;
import org.workcraft.serialisation.ModelDeserialiser;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.util.DataAccumulator;
import org.workcraft.util.XmlUtil;
import org.workcraft.workspace.Workspace;
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

		PluginInfo[] infos = pluginManager.getPluginsImplementing(PersistentPropertyEditable.class.getName());

		for (PluginInfo info : infos) {
			try {
				PersistentPropertyEditable e = (PersistentPropertyEditable)pluginManager.getSingleton(info);
				e.loadPersistentProperties(config);
			} catch (PluginInstantiationException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveConfig(String fileName) {
		PluginInfo[] infos = pluginManager.getPluginsImplementing(PersistentPropertyEditable.class.getName());

		for (PluginInfo info : infos) {
			try {
				PersistentPropertyEditable e = (PersistentPropertyEditable)pluginManager.getSingleton(info);
				e.storePersistentProperties(config);
			} catch (PluginInstantiationException e) {
				e.printStackTrace();
			}
		}

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
			mainWindow.dispose();
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

	public Model load(String path) throws DeserialisationException {
		try {
			FileInputStream fis = new FileInputStream(path);
			return load(fis);
		} catch (FileNotFoundException e) {
			throw new DeserialisationException(e);
		}
	}

	private InputStream getUncompressedEntry(String name, InputStream zippedData) throws IOException {
		ZipInputStream zis = new ZipInputStream(zippedData);

		ZipEntry ze;

		do {
			ze = zis.getNextEntry();

			if (ze.getName().equals(name))
				return zis;

			zis.closeEntry();
		} while (ze != null);

		zis.close();

		return null;
	}

	public Model load(InputStream is) throws DeserialisationException   {
		try {
			byte[] bufferedInput = DataAccumulator.loadStream(is);

			InputStream metadata = getUncompressedEntry("meta", new ByteArrayInputStream(bufferedInput));

			if (metadata == null)
				throw new DeserialisationException("meta entry is missing in the ZIP file");

			Document metaDoc = XmlUtil.loadDocument(metadata);

			metadata.close();

			// load math model

			Element mathElement = XmlUtil.getChildElement("math", metaDoc.getDocumentElement());
			//UUID mathFormatUUID = UUID.fromString(mathElement.getAttribute("format-uuid"));

			InputStream mathData = getUncompressedEntry(mathElement.getAttribute("entry-name"), new ByteArrayInputStream(bufferedInput));
			// TODO: get proper deserialiser for format

			ModelDeserialiser mathDeserialiser = (ModelDeserialiser) pluginManager.getSingletonByName(XMLDeserialiser.class.getName());

			DeserialisationResult mathResult = mathDeserialiser.deserialise(mathData, null);

			mathData.close();

			// load visual model if present

			Element visualElement = XmlUtil.getChildElement("visual", metaDoc.getDocumentElement());

			if (visualElement == null)
				return mathResult.model;

			//UUID visualFormatUUID = UUID.fromString(visualElement.getAttribute("format-uuid"));
			InputStream visualData = getUncompressedEntry (visualElement.getAttribute("entry-name"), new ByteArrayInputStream(bufferedInput));

			//TODO:get proper deserialiser
			ModelDeserialiser visualDeserialiser = (ModelDeserialiser) pluginManager.getSingletonByName(XMLDeserialiser.class.getName());

			DeserialisationResult visualResult = visualDeserialiser.deserialise(visualData, mathResult.referenceResolver);
			//visualResult.model.getVisualModel().setMathModel(mathResult.model.getMathModel());
			return visualResult.model;

		} catch (IOException e) {
			throw new DeserialisationException(e);
		} catch (ParserConfigurationException e) {
			throw new DeserialisationException(e);
		} catch (SAXException e) {
			throw new DeserialisationException(e);
		} catch (PluginInstantiationException e) {
			throw new DeserialisationException(e);
		}
	}

	public void save(Model model, String path) throws SerialisationException {
		File file = new File(path);
		try {
			FileOutputStream stream = new FileOutputStream(file);
			save (model, stream);
			stream.close();
		} catch (FileNotFoundException e) {
			throw new SerialisationException(e);
		} catch (IOException e) {
			throw new SerialisationException(e);
		}
	}

	public void save(Model model, OutputStream out) throws SerialisationException {
		VisualModel visualModel = (model instanceof VisualModel)? (VisualModel)model : null ;
		Model mathModel = (visualModel == null) ? model : visualModel.getMathModel();

		ZipOutputStream zos = new ZipOutputStream(out);

		// TODO: get appropiate serialiser from config
		ModelSerialiser mathSerialiser = null;

		try {
			mathSerialiser = (ModelSerialiser) pluginManager.getSingletonByName(XMLSerialiser.class.getName());


		String mathEntryName = "model" + mathSerialiser.getExtension();
		ZipEntry ze = new ZipEntry(mathEntryName);
		zos.putNextEntry(ze);
		ReferenceProducer refResolver = mathSerialiser.export(mathModel, zos, null);
		zos.closeEntry();

		String visualEntryName = null;
		ModelSerialiser visualSerialiser = null;

		if (visualModel != null) {
			// TODO: get appropiate serialiser from config

				visualSerialiser = (ModelSerialiser) pluginManager.getSingletonByName(XMLSerialiser.class.getName());

			visualEntryName = "visualModel" + visualSerialiser.getExtension();
			ze = new ZipEntry(visualEntryName);
			zos.putNextEntry(ze);
			visualSerialiser.export(visualModel, zos, refResolver);
			zos.closeEntry();
		}

		ze = new ZipEntry("meta");
		zos.putNextEntry(ze);

			Document doc;
			doc = XmlUtil.createDocument();

			Element root = doc.createElement("workcraft-meta");
			doc.appendChild(root);

			Element math = doc.createElement("math");
			math.setAttribute("entry-name", mathEntryName);
			math.setAttribute("format-uuid", mathSerialiser.getFormatUUID().toString());
			root.appendChild(math);

			if (visualModel != null) {
				Element visual = doc.createElement("visual");
				visual.setAttribute("entry-name", visualEntryName);
				visual.setAttribute("format-uuid", visualSerialiser.getFormatUUID().toString());
				root.appendChild(visual);
			}

			XmlUtil.writeDocument(doc, zos);

			zos.closeEntry();
			zos.close();
		} catch (ParserConfigurationException e) {
			throw new SerialisationException(e);
		} catch (IOException e) {
			throw new SerialisationException(e);
		} catch (PluginInstantiationException e) {
			throw new SerialisationException(e);
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
		} catch (PluginInstantiationException e) {
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

	public void loadWorkspace(File file) throws DeserialisationException {
		workspace.load(file.getPath());
	}

	public Config getConfig() {
		return config;
	}
}