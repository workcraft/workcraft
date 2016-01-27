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
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainWindow;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;


public class Console {
	static {
		// Workaround for Java 7 bug http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7075600
		// TODO: Remove again when switching to Java 8
		if(System.getProperty("java.version").startsWith("1.7")) {
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		}
	}

	public static void main(String[] args) {
		LinkedList<String> arglist = new LinkedList<String>();
		for (String s: args) {
			arglist.push(s);
		}

		boolean startGUI = true;
		String dir = null;
		for (String arg: args) {
			if (arg.equals("-nogui")) {
				startGUI = false;
				arglist.remove(arg);
			}
			if (arg.startsWith("-dir:")) {
				dir = arg.substring(5);
				arglist.remove(arg);
			}
		}

		System.out.println(Info.getFullTitle());
		System.out.println(Info.getCopyright());
		System.out.println();

		LogUtils.logMessageLine("Initialising framework...");
		final Framework framework  = Framework.getInstance();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		framework.initJavaScript();

		if (startGUI) {
			framework.startGUI();
		}

		framework.loadConfig();

		if (framework.isInGUIMode()) {
			MainWindow mainWindow = framework.getMainWindow();
			mainWindow.loadRecentFilesFromConfig();
			mainWindow.loadWindowGeometryFromConfig();
			mainWindow.loadDockingLayout();
		}

		framework.initPlugins();

		LogUtils.logMessageLine("Running startup scripts...");
		try {
			framework.execJavaScript(FileUtils.readAllTextFromSystemResource("scripts/functions.js"));
			framework.execJavaScript(FileUtils.readAllTextFromSystemResource("scripts/startup.js"));
		} catch (FileNotFoundException e) {
			LogUtils.logWarningLine("System script file not found: " + e.getMessage());
		} catch (IOException e) {
			LogUtils.logErrorLine("Error reading system script file: " + e.getMessage());
		} catch (WrappedException e) {
			LogUtils.logErrorLine("Startup script failed: " + e.getMessage());
		} catch (org.mozilla.javascript.EcmaError e) {
			LogUtils.logErrorLine("Startup script failed: " + e.getMessage());
		}

		for (String arg: args) {
			if (arg.startsWith("-exec:")) {
				arglist.remove(arg);
				framework.setArgs(arglist);
				try {
					LogUtils.logMessageLine("Executing "+ arg.substring(6) + "...");
					framework.execJavaScript(new File (arg.substring(6)));
				} catch (FileNotFoundException e) {
					LogUtils.logErrorLine("Script specified from command line not found: " + arg);
				} catch (org.mozilla.javascript.WrappedException e) {
					e.getWrappedException().printStackTrace();
					System.exit(1);
				} catch (org.mozilla.javascript.RhinoException e) {
					LogUtils.logErrorLine(e.getMessage());
					System.exit(1);
				}
			}
		}

		if (framework.isInGUIMode()) {
			for (String arg: arglist) {
				if (arg.endsWith(FileFilters.DOCUMENT_EXTENSION)) {
					File file = new File(dir, arg);
					MainWindow mainWindow = framework.getMainWindow();
					mainWindow.openWork(file);
				}
			}
		}

		while (true) {
			if (framework.shutdownRequested()) {
				try {
					MainWindow mainWindow = framework.getMainWindow();
					mainWindow.saveDockingLayout();
					mainWindow.saveWindowGeometryToConfig();
					mainWindow.saveRecentFilesToConfig();
					framework.saveConfig();
					framework.shutdownGUI();
				} catch (OperationCancelledException e) {
					framework.abortShutdown();
				}

				if ( !framework.shutdownRequested() ) {
					continue;
				}

				try {
					LogUtils.logMessageLine("Shutting down...");
					framework.execJavaScript(FileUtils.readAllTextFromSystemResource("scripts/shutdown.js"));
				} catch (FileNotFoundException e) {
					LogUtils.logErrorLine("System script file not found: " + e.getMessage());
				} catch (IOException e)	{
					LogUtils.logErrorLine("IO Exception: " + e.getMessage());
				} catch (org.mozilla.javascript.EcmaError e) {
					LogUtils.logErrorLine("Shutdown script failed: " + e.getMessage());
				} catch (WrappedException e) {
					LogUtils.logErrorLine("Shutdown script failed: " + e.getMessage());
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
					if ( !out.equals("undefined") ) {
						System.out.println(out);
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
