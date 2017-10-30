package org.workcraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.WrappedException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainWindow;
import org.workcraft.util.LogUtils;
import org.workcraft.util.ResourceUtils;

public class Console {
    static {
        // Workaround for Java 7 bug http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7075600
        // TODO: Remove again when switching to Java 8
        if (System.getProperty("java.version").startsWith("1.7")) {
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        }
        //Allows menu bar of OS X to be used instead of being in the Workcraft main window.
        if (DesktopApi.getOs().isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        }
    }

    public static void main(String[] args) {
        LinkedList<String> arglist = new LinkedList<>();
        for (String s: args) {
            arglist.push(s);
        }

        for (String arg: args) {
            if (arg.equals(Info.OPTION_VERSION)) {
                System.out.println(Info.getVersion());
                return;
            }
            if (arg.equals(Info.OPTION_HELP)) {
                System.out.println(Info.getHelp());
                return;
            }
        }

        final Framework framework = Framework.getInstance();
        boolean startGUI = true;
        for (String arg: args) {
            if (arg.equals(Info.OPTION_NOGUI)) {
                startGUI = false;
                arglist.remove(arg);
            }
            if (arg.startsWith(Info.OPTION_DIR)) {
                String path = arg.substring(Info.OPTION_DIR.length());
                framework.setWorkingDirectory(path);
                arglist.remove(arg);
            }
        }

        System.out.println(Info.getFullTitle());
        System.out.println(Info.getCopyright());
        System.out.println();

        // NOTE: JavaScript and Plugins needs to be initialised before GUI (because of assigning PropertyProviders)
        // and before config (because of plugin-specific settings).
        framework.init();
        // NOTE: Scripts should run after JavaScript, plugins, config (and possibly before GUI).
        try {
            for (String scriptName: ResourceUtils.getResources("scripts/")) {
                LogUtils.logMessage("  Executing script: " + scriptName);
                framework.execJavaScriptResource(scriptName);
            }
        } catch (IOException | URISyntaxException e) {
            LogUtils.logError("Cannot read script files: " + e.getMessage());
        }
        // NOTE: Config needs to be loaded before GUI.
        framework.loadConfig();
        if (startGUI) {
            framework.startGUI();
        }

        if (framework.isInGuiMode()) {
            for (String arg: arglist) {
                if (arg.endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                    MainWindow mainWindow = framework.getMainWindow();
                    File file = framework.getFileByAbsoluteOrRelativePath(arg);
                    mainWindow.openWork(file);
                }
            }
        }

        for (String arg: args) {
            if (arg.startsWith(Info.OPTION_EXEC)) {
                arglist.remove(arg);
                framework.setArgs(arglist);
                try {
                    String scriptName = arg.substring(Info.OPTION_EXEC.length());
                    LogUtils.logMessage("Executing " + scriptName + "...");
                    framework.execJavaScript(new File(scriptName));
                } catch (FileNotFoundException e) {
                    LogUtils.logError("Script specified from command line not found: " + arg);
                } catch (WrappedException e) {
                    e.getWrappedException().printStackTrace();
                    System.exit(1);
                } catch (org.mozilla.javascript.RhinoException e) {
                    LogUtils.logError(e.getMessage());
                    System.exit(1);
                }
            }
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            if (framework.shutdownRequested()) {
                try {
                    framework.shutdownGUI();
                    framework.saveConfig();
                } catch (OperationCancelledException e) {
                    framework.abortShutdown();
                }
                if (!framework.shutdownRequested()) {
                    continue;
                }
                LogUtils.logMessage("Shutting down...");
                System.exit(0);
            }

            if (framework.isInGuiMode()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                }
            } else if (framework.isGUIRestartRequested()) {
                framework.startGUI();
            } else {
                System.out.print("js>");
                try {
                    String line = in.readLine();
                    Object result = framework.execJavaScript(line);
                    Context.enter();
                    String out = Context.toString(result);
                    Context.exit();
                    if (!out.equals("undefined")) {
                        System.out.println(out);
                    }
                } catch (WrappedException e) {
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
