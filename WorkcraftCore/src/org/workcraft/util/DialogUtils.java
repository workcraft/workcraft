package org.workcraft.util;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;

public class DialogUtils {
    private static final String TITLE_INFO = "Information";
    private static final String TITLE_ERROR = "Error";
    private static final String TITLE_WARNING = "Warning";

    public static void showMessage(String msg) {
        LogUtils.logMessage(msg);
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (mainWindow != null) {
            JOptionPane.showMessageDialog(mainWindow, msg);
        }
    }

    public static void showInfo(String msg) {
        showInfo(msg, TITLE_INFO);
    }

    public static void showInfo(String msg, String title) {
        LogUtils.logInfo(msg);
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (mainWindow != null) {
            JOptionPane.showMessageDialog(mainWindow, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void showError(String msg) {
        showError(msg, TITLE_ERROR);
    }

    public static void showError(String msg, String title) {
        LogUtils.logError(msg);
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (mainWindow != null) {
            JOptionPane.showMessageDialog(mainWindow, msg, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showWarning(String msg) {
        showWarning(msg, TITLE_WARNING);
    }

    public static void showWarning(String msg, String title) {
        LogUtils.logWarning(msg);
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (mainWindow != null) {
            JOptionPane.showMessageDialog(mainWindow, msg, title, JOptionPane.WARNING_MESSAGE);
        }
    }

    public static String showInput(String msg, String initial) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (mainWindow != null) {
            return JOptionPane.showInputDialog(mainWindow, msg, initial);
        }
        return initial;
    }

    public static boolean showConfirm(String msg, String title) {
        boolean result = false;
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (mainWindow != null) {
            int answer = JOptionPane.showConfirmDialog(mainWindow, msg, title, JOptionPane.YES_NO_OPTION);
            result = answer == JOptionPane.YES_OPTION;
        }
        return result;
    }

}
