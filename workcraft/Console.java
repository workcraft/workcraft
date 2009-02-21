package org.workcraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import javax.swing.SwingUtilities;

import org.mozilla.javascript.Context;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.OperationCancelledException;


public class Console {

	public static void main(String[] args) {
		LinkedList<String> arglist = new LinkedList<String>();

		for (String s: args)
			arglist.push(s);

		boolean silent = false;

		for (String s: args)
			if (s.equals("-silent")) {
				silent = true;
				arglist.remove(s);
			}

		if (!silent)
			System.out.println ("Workcraft 2 (Metastability strikes back) dev version\n");

		if (!silent)
			System.out.println ("Initialising framework...");

		final Framework framework  = new Framework();

		framework.setSilent(silent);

		BufferedReader in = new BufferedReader (new InputStreamReader (System.in));

		framework.initJavaScript();
		framework.initPlugins();

		if (!silent)
			System.out.println ("Running startup scripts...");

		try {
			framework.execJavaScript(new File ("scripts/functions.js"));
			framework.execJavaScript(new File ("scripts/startup.js"));

		} catch (FileNotFoundException e2) {
			System.err.println ("System script file not found: "+e2.getMessage());
		}

		if (!silent)
			System.out.println("Startup complete.\n\n");

		for (String arg: args) {
			if (arg.equals("-gui")) {
				framework.startGUI();
				arglist.remove(arg);
			}
			if (arg.startsWith("-exec:")) {
				arglist.remove(arg);

				framework.setArgs(arglist);

				try {
					if (!silent)
						System.out.println ("Executing "+ arg.substring(6) + "...");
					framework.execJavaScript(new File (arg.substring(6)));
				} catch (FileNotFoundException e) {
					System.err.println ("Script specified from command line not found: "+arg);
				} catch (org.mozilla.javascript.WrappedException e) {
					e.getWrappedException().printStackTrace();
					System.exit(1);
				} catch (org.mozilla.javascript.RhinoException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
			}
		}

		while (true) {
			if (framework.shutdownRequested()) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							try {
								framework.shutdownGUI();
							} catch (OperationCancelledException e) {
								framework.abortShutdown();

							}
						}
					});
				} catch (InterruptedException e1) {
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}

				if (!framework.shutdownRequested())
					continue;

				try {
					if (!silent)
						System.out.println ("Shutting down...");
					framework.execJavaScript(new File ("scripts/shutdown.js"));

				} catch (FileNotFoundException e) {
					System.err.println ("System script file not found: "+e.getMessage());
				}



				System.exit(0);
			}

			if (framework.isInGUIMode())
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
				}
				else if (framework.isGUIRestartRequested())
					framework.startGUI();
				else {
					System.out.print ("js>");
					try {
						String line = in.readLine();
						Object result = framework.execJavaScript(line);

						Context.enter();
						String out = Context.toString(result);
						Context.exit();
						if (!out.equals("undefined"))
							System.out.println (out);
					}
					catch (org.mozilla.javascript.WrappedException e) {

						System.err.println(e.getWrappedException().getMessage());
					}
					catch (org.mozilla.javascript.RhinoException e) {
						System.err.println(e.getMessage());
					}
					catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
		}
	}
}