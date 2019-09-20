package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;

import javax.swing.*;

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

    public static boolean showConfirm(String msg, String title, boolean defaultChoice) {
        return showConfirm(msg, title, JOptionPane.QUESTION_MESSAGE, defaultChoice);
    }

    public static boolean showConfirmInfo(String msg) {
        return showConfirm(msg, TITLE_INFO, true);
    }

    public static boolean showConfirmInfo(String msg, String title, boolean defaultChoice) {
        return showConfirm(msg, title, JOptionPane.INFORMATION_MESSAGE, defaultChoice);
    }

    public static boolean showConfirmWarning(String msg) {
        return showConfirm(msg, TITLE_WARNING, JOptionPane.WARNING_MESSAGE, true);
    }

    public static boolean showConfirmWarning(String msg, String title, boolean defaultChoice) {
        return showConfirm(msg, title, JOptionPane.WARNING_MESSAGE, defaultChoice);
    }

    public static boolean showConfirmError(String msg) {
        return showConfirm(msg, TITLE_ERROR, JOptionPane.ERROR_MESSAGE, true);
    }

    public static boolean showConfirmError(String msg, String title, boolean defaultChoice) {
        return showConfirm(msg, TITLE_ERROR, JOptionPane.ERROR_MESSAGE, defaultChoice);
    }

    private static boolean showConfirm(String msg, String title, int messageType, boolean defaultChoice) {
        boolean result = defaultChoice;
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            String yesText = UIManager.getString("OptionPane.yesButtonText");
            String noText = UIManager.getString("OptionPane.noButtonText");
            String[] options = {yesText, noText};
            int answer = JOptionPane.showOptionDialog(mainWindow, msg, title, JOptionPane.YES_NO_OPTION,
                    messageType, null, options, defaultChoice ? yesText : noText);
            result = answer == JOptionPane.YES_OPTION;
        }
        return result;
    }

    public static String showInput(String msg, String initial) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            return JOptionPane.showInputDialog(mainWindow, msg, initial);
        }
        return initial;
    }

    private static int showYesNoCancel(String msg, String title, int messageType, int defaultChoice) {
        String yesText = UIManager.getString("OptionPane.yesButtonText");
        String noText = UIManager.getString("OptionPane.noButtonText");
        String cancelText = UIManager.getString("OptionPane.cancelButtonText");
        return showChoice(msg, title, messageType, yesText, noText, cancelText, defaultChoice);
    }

    public static int showYesNoCancel(String msg, String title, int defaultChoice) {
        return showYesNoCancel(msg, title, JOptionPane.QUESTION_MESSAGE, defaultChoice);
    }

    private static int showChoice(String msg, String title, int messageType,
            String yesText, String noText, String cancelText, int defaultChoice) {
        int result = JOptionPane.CANCEL_OPTION;
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            String[] options = {yesText, noText, cancelText};
            result = JOptionPane.showOptionDialog(mainWindow, msg, title, JOptionPane.YES_NO_CANCEL_OPTION,
                    messageType, null, options, defaultChoice);
        }
        return result;
    }

}
