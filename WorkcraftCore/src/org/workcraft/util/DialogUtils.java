package org.workcraft.util;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;

public class DialogUtils {

    private static final String TITLE_MESSAGE = "Message";
    private static final String TITLE_INFO = "Information";
    private static final String TITLE_ERROR = "Error";
    private static final String TITLE_WARNING = "Warning";

    private static void showMessage(String msg, String title, int messageType) {
        switch (messageType) {
        case JOptionPane.INFORMATION_MESSAGE:
            LogUtils.logInfo(msg);
            break;
        case JOptionPane.WARNING_MESSAGE:
            LogUtils.logWarning(msg);
            break;
        case JOptionPane.ERROR_MESSAGE:
            LogUtils.logError(msg);
            break;
        default:
            LogUtils.logMessage(msg);
            break;
        }
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            JOptionPane.showMessageDialog(mainWindow, msg, title, messageType);
        }
    }

    public static void showMessage(String msg) {
        showMessage(msg, TITLE_MESSAGE);
    }

    public static void showMessage(String msg, String title) {
        showMessage(msg, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showInfo(String msg) {
        showInfo(msg, TITLE_INFO);
    }

    public static void showInfo(String msg, String title) {
        showMessage(msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(String msg) {
        showWarning(msg, TITLE_WARNING);
    }

    public static void showWarning(String msg, String title) {
        showMessage(msg, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(String msg) {
        showError(msg, TITLE_ERROR);
    }

    public static void showError(String msg, String title) {
        showMessage(msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private static boolean showConfirm(String msg, String title, int messageType) {
        boolean result = false;
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            int answer = JOptionPane.showConfirmDialog(mainWindow, msg, title, JOptionPane.YES_NO_OPTION, messageType);
            result = answer == JOptionPane.YES_OPTION;
        }
        return result;
    }

    public static boolean showConfirm(String msg, String title) {
        return showConfirm(msg, title, JOptionPane.QUESTION_MESSAGE);
    }

    public static boolean showConfirmInfo(String msg, String title) {
        return showConfirm(msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean showConfirmWarning(String msg, String title) {
        return showConfirm(msg, title, JOptionPane.WARNING_MESSAGE);
    }

    public static boolean showConfirmError(String msg, String title) {
        return showConfirm(msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private static String showInput(String msg, String initial, int messageType) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            return JOptionPane.showInputDialog(mainWindow, msg, initial, messageType);
        }
        return initial;
    }

    public static String showInput(String msg, String initial) {
        return showInput(msg, initial, JOptionPane.QUESTION_MESSAGE);
    }

}
