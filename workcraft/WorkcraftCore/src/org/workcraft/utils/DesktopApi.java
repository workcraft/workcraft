package org.workcraft.utils;

import org.workcraft.interop.ExternalProcess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DesktopApi {

    public static boolean mail(String email, String subject, String body) {
        try {
            String ssp = email + "?subject=" + replaceMailto(subject) + "&body=" + replaceMailto(body);
            URI uri = new URI("mailto", ssp, null);
            return mail(uri);
        } catch (URISyntaxException e) {
            LogUtils.logError(e.getMessage());
        }
        return false;
    }

    private static String replaceMailto(String s) {
        return s == null ? "" : s.replace("?", "%3F").replace("&", "%26");
    }

    public static boolean mail(URI uri) {
        if (mailDesktop(uri)) return true;
        if (openSystemSpecific(uri.toString())) return true;
        LogUtils.logError("Cannot open a mailer for '" + uri + "'");
        return false;
    }

    public static boolean browse(URI uri) {
        if (browseDesktop(uri)) return true;
        if (openSystemSpecific(uri.toString())) return true;
        LogUtils.logError("Cannot open a browser for '" + uri + "'");
        return false;
    }

    public static boolean open(File file) {
        if (openDesktop(file)) return true;
        if (openSystemSpecific(file.getPath())) return true;
        LogUtils.logError("Cannot open a viewer for  '" + file.getPath() + "'");
        return false;
    }

    public static boolean edit(File file) {
        if (editDesktop(file)) return true;
        if (openSystemSpecific(file.getPath())) return true;
        LogUtils.logError("Cannot open an editor for '" + file.getPath() + "'");
        return false;
    }

    private static boolean mailDesktop(URI uri) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                return false;
            }
            Desktop.getDesktop().mail(uri);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean browseDesktop(URI uri) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                return false;
            }
            Desktop.getDesktop().browse(uri);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean openDesktop(File file) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                return false;
            }
            Desktop.getDesktop().open(file);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean editDesktop(File file) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
                return false;
            }
            Desktop.getDesktop().edit(file);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean openSystemSpecific(String what) {
        OsType os = getOs();

        if (os.isLinux()) {
            if (runCommand("xdg-open", "%s", what)) return true;
            if (runCommand("kde-open", "%s", what)) return true;
            if (runCommand("gnome-open", "%s", what)) return true;
        }

        if (os.isMac()) {
            if (runCommand("open", "%s", what)) return true;
        }

        if (os.isWindows()) {
            if (runCommand("explorer", "%s", what)) return true;
        }
        return false;
    }

    private static boolean runCommand(String command, String args, String file) {
        String[] parts = prepareCommand(command, args, file);
        ExternalProcess process = new ExternalProcess(parts);
        try {
            process.start();
            ExternalProcess.printCommandLine(parts);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static String[] prepareCommand(String command, String args, String file) {
        List<String> parts = new ArrayList<>();
        parts.add(command);
        if (args != null) {
            for (String s : TextUtils.splitWords(args)) {
                String part = String.format(s, file);
                parts.add(part.trim());
            }
        }
        return parts.toArray(new String[0]);
    }

    public enum OsType {
        LINUX,
        MACOS,
        SOLARIS,
        WINDOWS,
        UNKNOWN;

        public boolean isLinux() {
            return (this == LINUX) || (this == SOLARIS);
        }

        public boolean isMac() {
            return this == MACOS;
        }

        public boolean isWindows() {
            return this == WINDOWS;
        }
    }

    public static OsType getOs() {
        String s = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        OsType result = OsType.UNKNOWN;
        if (s.contains("win")) {
            result = OsType.WINDOWS;
        } else if (s.contains("mac")) {
            result = OsType.MACOS;
        } else if (s.contains("solaris")) {
            result = OsType.SOLARIS;
        } else if (s.contains("sunos")) {
            result = OsType.SOLARIS;
        } else if (s.contains("linux")) {
            result = OsType.LINUX;
        } else if (s.contains("unix")) {
            result = OsType.LINUX;
        }
        return result;
    }

    public static String getConfigPath() {
        String result = null;
        OsType os = getOs();
        if (os.isLinux()) {
            result = System.getenv("XDG_CONFIG_HOME");
            if (result == null) {
                result = System.getProperty("user.home") + File.separator
                        + ".config";
            }
        } else if (os.isMac()) {
            result = System.getProperty("user.home") + File.separator +
                    "Library" + File.separator + "Application Support";
        } else if (os.isWindows()) {
            result = System.getenv("APPDATA");
            if (result == null) {
                result = System.getProperty("user.home");
            }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    public static int getMenuKeyMask() {
        return getOs().isMac() ? ActionEvent.META_MASK : ActionEvent.CTRL_MASK;
    }

    public static String getMenuKeyName() {
        return getMenuKeyMask() == ActionEvent.META_MASK ? "Cmd" : "Ctrl";
    }

    public static int getMenuKeyMouseMask() {
        return getOs().isMac() ? MouseEvent.META_DOWN_MASK : MouseEvent.CTRL_DOWN_MASK;
    }

    public static boolean isMenuKeyDown(InputEvent e) {
        return getOs().isMac() ? e.isMetaDown() : e.isControlDown();
    }

    public static KeyStroke getUndoKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_Z, getMenuKeyMask());
    }

    public static KeyStroke getRedoKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_Z, getMenuKeyMask() | ActionEvent.SHIFT_MASK);
    }

    public static KeyStroke getIncreaseKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, getMenuKeyMask());
    }

    public static KeyStroke getDecreaseKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, getMenuKeyMask());
    }

    public static KeyStroke getRestoreKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_0, getMenuKeyMask());
    }

}
