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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.WrappedException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.util.FileUtils;


public class Console {

	public static void main(String[] args) {
		LinkedList<String> arglist = new LinkedList<String>();
		for (String s: args) {
			arglist.push(s);
		}

		boolean silent = false;
		for (String s: args) {
			if (s.equals("-silent")) {
				silent = true;
				arglist.remove(s);
			}
		}

		if (!silent) {
			System.out.println ("Workcraft 2 (Metastability strikes back) dev version\n");
			System.out.println ("Initialising framework...");
		}

		File f = new File("config");
		if (f.exists() && !f.isDirectory()) {
			System.out.println("\n!!! Error: Workcraft needs to create a directory named 'config' to store configuration files, but a file already exists with such name and is not a directory. Please delete the file and run Workcraft again.");
			return;
		}

		if (!f.exists()) {
			f.mkdirs();
		}

		final Framework framework  = new Framework();
		framework.setSilent(silent);

		BufferedReader in = new BufferedReader (new InputStreamReader (System.in));

		framework.initJavaScript();
		framework.initPlugins();

		if (!silent) {
			System.out.println ("Running startup scripts...");
		}

		try {
			framework.execJavaScript(FileUtils.readAllTextFromSystemResource("scripts/functions.js"));
			framework.execJavaScript(FileUtils.readAllTextFromSystemResource("scripts/startup.js"));
		} catch (FileNotFoundException e2) {
			System.err.println ("! Warning: System script file not found: "+e2.getMessage());
		} catch (IOException e) {
			System.err.println ("! Warning: Error reading system script file: "+e.getMessage());
		} catch (WrappedException e) {
			System.err.println ("! Startup script failed: " + e.getMessage());
		} catch (org.mozilla.javascript.EcmaError e) {
			System.err.println ("! Startup script failed: " + e.getMessage());
		}

		if (!silent) {
			System.out.println("Startup complete.\n\n");
		}
		boolean startGUI = true;
		for (String arg: args) {
			if (arg.equals("-gui")) {
				startGUI = true;
				arglist.remove(arg);
			}
			if (arg.equals("-nogui")) {
				startGUI = false;
				arglist.remove(arg);
			}
			if (arg.startsWith("-exec:")) {
				arglist.remove(arg);
				framework.setArgs(arglist);
				try {
					if (!silent) {
						System.out.println ("Executing "+ arg.substring(6) + "...");
					}
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

		if (startGUI) {
			framework.startGUI();
		}

		while (true) {
			if (framework.shutdownRequested()) {
				/* This way of dealing with shutdown request results in unclosable Workcraft window after UI reset.
				 * A replacement code is suggested without calling for inwokeAndWait method... but still does not work.
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
				*/
				try {
					framework.shutdownGUI();
				} catch (OperationCancelledException e) {
					framework.abortShutdown();
				}

				if (!framework.shutdownRequested()) {
					continue;
				}

				try {
					if (!silent) {
						System.out.println ("Shutting down...");
					}
					framework.execJavaScript(FileUtils.readAllTextFromSystemResource("scripts/shutdown.js"));
				} catch (FileNotFoundException e) {
					System.err.println ("System script file not found: " + e.getMessage());
				} catch (IOException e)	{
					System.err.println ("IO Exception: " + e.getMessage());
				} catch (org.mozilla.javascript.EcmaError e) {
					System.err.println ("! Shutdown script failed: " + e.getMessage());
				} catch (WrappedException e) {
					System.err.println ("! Shutdown script failed: " + e.getMessage());
				}
				System.exit(0);
			}

			if (framework.isInGUIMode()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			} else if (framework.isGUIRestartRequested()) {
				framework.startGUI();
			} else {
				System.out.print ("js>");
				try {
					String line = in.readLine();
					Object result = framework.execJavaScript(line);
					Context.enter();
					String out = Context.toString(result);
					Context.exit();
					if (!out.equals("undefined")) {
						System.out.println (out);
					}
				} catch (org.mozilla.javascript.WrappedException e) {
					System.err.println(e.getWrappedException().getMessage());
				} catch (org.mozilla.javascript.RhinoException e) {
					System.err.println(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}
}
