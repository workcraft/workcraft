package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;

import javax.swing.*;

public class DialogUtils {

    private static final int TRUNCATE_LENGTH = 100;
    private static final String MESSAGE_TITLE = "Message";
    private static final String INFO_TITLE = "Information";
    private static final String ERROR_TITLE = "Error";
    private static final String WARNING_TITLE = "Warning";
    private static final String INPUT_TITLE = "Input";

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
            String text = TextUtils.truncateText(msg, TRUNCATE_LENGTH);
            JOptionPane.showMessageDialog(mainWindow, text, title, messageType);
        }
    }

    public static void showMessage(String msg) {
        showMessage(msg, MESSAGE_TITLE);
    }

    public static void showMessage(String msg, String title) {
        showMessage(msg, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showInfo(String msg) {
        showInfo(msg, INFO_TITLE);
    }

    public static void showInfo(String msg, String title) {
        showMessage(msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(String msg) {
        showWarning(msg, WARNING_TITLE);
    }

    public static void showWarning(String msg, String title) {
        showMessage(msg, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(String msg) {
        showError(msg, ERROR_TITLE);
    }

    public static void showError(String msg, String title) {
        showMessage(msg, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(String msg, String title, boolean defaultChoice) {
        return showConfirm(msg, title, JOptionPane.QUESTION_MESSAGE, defaultChoice);
    }

    public static boolean showConfirmInfo(String msg) {
        return showConfirmInfo(msg, INFO_TITLE, true);
    }

    public static boolean showConfirmInfo(String msg, String title, boolean defaultChoice) {
        return showConfirm(msg, title, JOptionPane.INFORMATION_MESSAGE, defaultChoice);
    }

    public static boolean showConfirmWarning(String msg) {
        return showConfirmWarning(msg, WARNING_TITLE, true);
    }

    public static boolean showConfirmWarning(String msg, String title, boolean defaultChoice) {
        return showConfirm(msg, title, JOptionPane.WARNING_MESSAGE, defaultChoice);
    }

    public static boolean showConfirmError(String msg) {
        return showConfirmError(msg, ERROR_TITLE,  true);
    }

    public static boolean showConfirmError(String msg,  String title, boolean defaultChoice) {
        return showConfirm(msg, title, JOptionPane.ERROR_MESSAGE, defaultChoice);
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
        return showInput(msg, INPUT_TITLE, initial);
    }

    public static String showInput(String msg, String title, String initial) {
        return showInput(msg, title, JOptionPane.QUESTION_MESSAGE, initial);
    }

    private static String showInput(String msg, String title, int messageType, String initial) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            return (String) JOptionPane.showInputDialog(mainWindow, msg, title,
                    messageType, null, null, initial);
        }
        return initial;
    }

    public static int showYesNoCancel(String msg, String title, int defaultChoice) {
        return showYesNoCancel(msg, title, JOptionPane.QUESTION_MESSAGE, defaultChoice);
    }

    private static int showYesNoCancel(String msg, String title, int messageType, int defaultChoice) {
        String yesText = UIManager.getString("OptionPane.yesButtonText");
        String noText = UIManager.getString("OptionPane.noButtonText");
        String cancelText = UIManager.getString("OptionPane.cancelButtonText");
        return showChoice(msg, title, messageType, yesText, noText, cancelText, defaultChoice);
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
