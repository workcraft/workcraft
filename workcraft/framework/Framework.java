package org.workcraft.framework;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.workcraft.framework.exceptions.DocumentOpenFailedException;
import org.workcraft.gui.LAF;
import org.workcraft.gui.MainFrame;
import org.workcraft.gui.workspace.WorkspaceView;


public class Framework {
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
	private MainFrame mainFrame;

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
				systemScope = cx.initStandardObjects();

				Object frameworkScriptable = Context.javaToJS(Framework.this, systemScope);
				ScriptableObject.putProperty(systemScope, "framework", frameworkScriptable);
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
				mainFrame = new MainFrame(Framework.this);
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

	public MainFrame getMainFrame() {
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
}