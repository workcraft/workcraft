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
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainWindow;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;

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

        //LogUtils.logMessageLine("Initialising framework...");
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

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        // NOTE: JavaScript needs to be initilised before GUI
        framework.initJavaScript();
        // NOTE: Plugins need to be loaded before GUI (because of assigning PropertyProviders)
        framework.initPlugins(true);
        // NOTE: Config needs to be loaded before GUI
        framework.loadConfig();
        if (startGUI) {
            framework.startGUI();
        }

        //LogUtils.logMessageLine("Running startup scripts...");
        try {
            framework.execJavaScript(FileUtils.readAllTextFromSystemResource("scripts/functions.js"));
            framework.execJavaScript(FileUtils.readAllTextFromSystemResource("scripts/startup.js"));
        } catch (FileNotFoundException e) {
            LogUtils.logWarningLine("System script file not found: " + e.getMessage());
        } catch (IOException e) {
            LogUtils.logErrorLine("Error reading system script file: " + e.getMessage());
        } catch (WrappedException | org.mozilla.javascript.EcmaError e) {
            LogUtils.logErrorLine("Startup script failed: " + e.getMessage());
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
                    LogUtils.logMessageLine("Executing " + scriptName + "...");
                    framework.execJavaScript(new File(scriptName));
                } catch (FileNotFoundException e) {
                    LogUtils.logErrorLine("Script specified from command line not found: " + arg);
                } catch (WrappedException e) {
                    e.getWrappedException().printStackTrace();
                    System.exit(1);
                } catch (org.mozilla.javascript.RhinoException e) {
                    LogUtils.logErrorLine(e.getMessage());
                    System.exit(1);
                }
            }
        }

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

                try {
                    LogUtils.logMessageLine("Shutting down...");
                    framework.execJavaScript(FileUtils.readAllTextFromSystemResource("scripts/shutdown.js"));
                } catch (FileNotFoundException e) {
                    LogUtils.logErrorLine("System script file not found: " + e.getMessage());
                } catch (IOException e) {
                    LogUtils.logErrorLine("IO Exception: " + e.getMessage());
                } catch (WrappedException | org.mozilla.javascript.EcmaError e) {
                    LogUtils.logErrorLine("Shutdown script failed: " + e.getMessage());
                }
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
