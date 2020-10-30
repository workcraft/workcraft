package org.workcraft;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.WrappedException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.JarUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.FileFilters;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

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
        LogUtils.logMessage(Info.getFullTitle() + "\n" + Info.getCopyright() + "\n");
        Options options = new Options(args);

        if (options.hasVersionFlag()) {
            LogUtils.logMessage(Info.getVersion().toString());
            return;
        }

        if (options.hasHelpFlag()) {
            LogUtils.logMessage(Options.getHelpMessage());
            return;
        }

        Integer port = options.getPort();
        if ((port != null) && reuseRunningInstance(port, options.getDirectory(), options.getPaths())) {
            LogUtils.logInfo("Reusing Workcraft instance on port " + port);
            return;
        }

        Framework framework = Framework.getInstance();
        framework.setWorkingDirectory(options.getDirectory());

        // NOTE: Initialisation of plugins needs to be before config because of plugin-specific settings
        framework.init();

        // NOTE: Config needs to be loaded before scripts and saved on exit
        if (!options.hasNoConfigFlag()) {
            framework.loadConfig();
            Runtime.getRuntime().addShutdownHook(new Thread(framework::saveConfig));
        }

        // NOTE: Resource scripts should run after config (and possibly before GUI)
        execScriptResources();

        if (!options.hasNoGuiFlag()) {
            framework.startGUI();
            openWorkFiles(options.getPaths());
        }

        execScriptParameter(options.getScript(), options.getPaths());

        // Wait for external requests via socket port
        if (port != null) {
            new Thread(() -> processExternalRequests(port)).start();
        }

        // Process user input until shutdown
        while (true) {
            if (framework.isShutdownRequested()) {
                processShutdownRequest();
            } else if (framework.isInGuiMode()) {
                processGuiWait();
            } else {
                processNonguiInput();
            }
        }
    }

    private static boolean reuseRunningInstance(int port, File directory, Collection<String> paths) {
        try (Socket socket = new Socket(InetAddress.getByName(null), port)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            for (String path : paths) {
                File file = FileUtils.getFileByAbsoluteOrRelativePath(path, directory);
                writer.write(file.getAbsolutePath() + "\n");
                writer.flush();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void processExternalRequests(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                Collection<String> paths = reader.lines().collect(Collectors.toList());
                openWorkFiles(paths);
                socket.close();
            }
        } catch (IOException e) {
            LogUtils.logError("Cannot open Workcraft reuse service on port " + port + ": " + e.getMessage());
        }
    }

    private static void execScriptResources() {
        Framework framework = Framework.getInstance();
        try {
            for (String scriptName : JarUtils.getResourcePaths("scripts/")) {
                LogUtils.logMessage("  Executing script: " + scriptName);
                try {
                    framework.execJavaScriptResource(scriptName);
                } catch (IOException e) {
                    LogUtils.logError("Cannot execute script file '" + scriptName + "'");
                }
            }
        } catch (IOException e) {
            LogUtils.logError("Cannot read script resources: " + e.getMessage());
        }
    }

    private static void openWorkFiles(Collection<String> paths) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        for (String path : paths) {
            if (FileFilters.isWorkPath(path)) {
                File file = framework.getFileByAbsoluteOrRelativePath(path);
                mainWindow.openWork(file);
            }
        }
    }

    @SuppressWarnings("PMD.DoNotCallSystemExit")
    private static void execScriptParameter(String script, Collection<String> paths) {
        if (script != null) {
            Framework framework = Framework.getInstance();
            framework.setArgs(paths);
            try {
                File scriptFile = framework.getFileByAbsoluteOrRelativePath(script);
                if ((scriptFile != null) && scriptFile.exists() && scriptFile.isFile() && scriptFile.canRead()) {
                    LogUtils.logMessage("Executing script file " + script + "...");
                    framework.execJavaScriptFile(scriptFile);
                } else {
                    LogUtils.logMessage("Executing raw script:\n" + script);
                    framework.execJavaScript(script);
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

    @SuppressWarnings("PMD.DoNotCallSystemExit")
    private static void processShutdownRequest() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    Framework.getInstance().shutdownGUI();
                    LogUtils.logMessage("Shutting down...");
                    System.exit(0);
                } catch (OperationCancelledException e) {
                    Framework.getInstance().abortShutdown();
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void processGuiWait() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void processNonguiInput() {
        System.out.print("js>");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try {
            String line = reader.readLine();
            Object result = Framework.getInstance().execJavaScript(line);
            Context.enter();
            String out = Context.toString(result);
            Context.exit();
            if (!"undefined".equals(out)) {
                System.out.println(out);
            }
        } catch (org.mozilla.javascript.WrappedException e) {
            Throwable we = e.getWrappedException();
            System.err.println(we.getClass().getName() + " " + we.getMessage());
        } catch (IOException | org.mozilla.javascript.RhinoException e) {
            System.err.println(e.getMessage());
        }
    }

}
