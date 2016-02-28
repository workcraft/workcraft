/*
 * This code is a simplified version of DesktopApi class provided by MightyPork, all credits to him.
 *
 * http://stackoverflow.com/questions/18004150/desktop-api-is-not-supported-on-the-current-platform
 */

package org.workcraft.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DesktopApi {

    public static boolean browse(URI uri) {
        if (openSystemSpecific(uri.toString())) return true;
        if (browseDesktop(uri)) return true;
        return false;
    }

    public static boolean open(File file) {
        if (openDesktop(file)) return true;
        if (openSystemSpecific(file.getPath())) return true;
        return false;
    }

    public static boolean edit(File file) {
        if (editDesktop(file)) return true;
        if (openSystemSpecific(file.getPath())) return true;
        return false;
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
        try {
            Process p = Runtime.getRuntime().exec(parts);
            if (p == null) return false;

            try {
                int retval = p.exitValue();
                if (retval == 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (IllegalThreadStateException itse) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    private static String[] prepareCommand(String command, String args, String file) {
        List<String> parts = new ArrayList<>();
        parts.add(command);
        if (args != null) {
            for (String s : args.split("\\s")) {
                s = String.format(s, file);
                parts.add(s.trim());
            }
        }
        return parts.toArray(new String[parts.size()]);
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
        String s = System.getProperty("os.name").toLowerCase();
        OsType result = OsType.UNKNOWN;
        if (s.contains("win")) {
            result = OsType.WINDOWS;
        } else     if (s.contains("mac")) {
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

}
