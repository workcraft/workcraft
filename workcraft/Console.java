package org.workcraft;

import java.io.*;

import org.mozilla.javascript.*;
import org.workcraft.framework.*;


public class Console {

	public static void main(String[] args) {
		boolean silent = false;
		for (String s: args) {
			if (s.equals("-silent"))
				silent = true;
		}

		if (!silent)
			System.out.println ("Workcraft 2 (Metastability strikes back) dev version\n");

		if (!silent)
			System.out.println ("Initialising framework...");

		Framework framework  = new Framework();

		framework.setSilent(silent);

		BufferedReader in = new BufferedReader (new InputStreamReader (System.in));

		framework.initJavaScript();

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
			if (arg.equals("-gui"))
				framework.startGUI();
			if (arg.startsWith("-exec:"))
				try {
					if (!silent)
						System.out.println ("Executing "+ arg.substring(6) + "...");
					framework.execJavaScript(new File (arg.substring(6)));
				} catch (FileNotFoundException e) {
					System.err.println ("Script specified from command line not found: "+arg);
				}
		}



		while (true) {
			if (framework.shutdownRequested()) {
				framework.shutdownGUI();
				try {
					if (!silent)
						System.out.println ("Shutting down...");
					framework.execJavaScript(new File ("scripts/shutdown.js"));

				} catch (FileNotFoundException e) {
					System.err.println ("System script file not found: "+e.getMessage());
				}
				System.exit(0);
			}

			if (framework.isInGUIMode()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
				}
			}

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

						System.err.println(e.getWrappedException().getClass().getName()+" "+e.getWrappedException().getMessage());
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
