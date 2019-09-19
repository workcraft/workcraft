package org.workcraft;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.WrappedException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.ResourceUtils;
import org.workcraft.workspace.FileFilters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class Console {

    static {
        // Enable font anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        //Allows menu bar of OS X to be used instead of being in the Workcraft main window.
        if (DesktopApi.getOs().isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        }
    }

    public static void main(String[] args) {
        // Process -version and -help options
        for (String arg : args) {
            if (arg.equals(Info.OPTION_VERSION)) {
                System.out.println(Info.getVersion());
                return;
            }
            if (arg.equals(Info.OPTION_HELP)) {
                System.out.println(Info.getHelp());
                return;
            }
        }

        // Process -nogui, -noconfig and -dir: options
        final Framework framework = Framework.getInstance();
        LinkedList<String> arglist = new LinkedList<>(Arrays.asList(args));
        boolean startGui = true;
        boolean useConfig = true;
        for (String arg : args) {
            if (arg.equals(Info.OPTION_NOGUI)) {
                startGui = false;
                arglist.remove(arg);
            }
            if (arg.equals(Info.OPTION_NOCONFIG)) {
                useConfig = false;
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
            for (String scriptName : ResourceUtils.getResources("scripts/")) {
                LogUtils.logMessage("  Executing script: " + scriptName);
                framework.execJavaScriptResource(scriptName);
            }
        } catch (IOException | URISyntaxException e) {
            LogUtils.logError("Cannot read script files: " + e.getMessage());
        }
        // NOTE: Config needs to be loaded before GUI.
        if (useConfig) {
            framework.loadConfig();
        }
        if (startGui) {
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

        // Process -exec: option
        for (String arg: args) {
            if (arg.startsWith(Info.OPTION_EXEC)) {
                arglist.remove(arg);
                framework.setArgs(arglist);
                try {
                    String execParameter = arg.substring(Info.OPTION_EXEC.length());
                    File execFile = framework.getFileByAbsoluteOrRelativePath(execParameter);
                    if ((execFile != null) && execFile.exists() && execFile.isFile() && execFile.canRead()) {
                        LogUtils.logMessage("Executing script file " + execParameter + "...");
                        framework.execJavaScriptFile(execFile);
                    } else {
                        LogUtils.logMessage("Executing raw script:\n" + execParameter);
                        framework.execJavaScript(execParameter);
                    }
                } catch (WrappedException e) {
                    e.getWrappedException().printStackTrace();
                    System.exit(1);
                } catch (IOException | RhinoException e) {
                    LogUtils.logError(e.getMessage());
                    System.exit(1);
                }
            }
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (true) {
            if (framework.isShutdownRequested()) {
                try {
                    framework.shutdownGUI();
                    if (useConfig) {
                        framework.saveConfig();
                    }
                } catch (OperationCancelledException e) {
                    framework.abortShutdown();
                }
                if (!framework.isShutdownRequested()) {
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
            } else {
                System.out.print("js>");
                try {
                    String line = reader.readLine();
                    Object result = framework.execJavaScript(line);
                    Context.enter();
                    String out = Context.toString(result);
                    Context.exit();
                    if (!"undefined".equals(out)) {
                        System.out.println(out);
                    }
                } catch (WrappedException e) {
                    System.err.println(e.getWrappedException().getMessage());
                } catch (IOException | RhinoException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

}
