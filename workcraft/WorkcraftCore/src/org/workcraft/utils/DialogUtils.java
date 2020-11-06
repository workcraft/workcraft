package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.Format;
import org.workcraft.interop.FormatFileFilter;
import org.workcraft.workspace.FileFilters;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class DialogUtils {

    private static final int TRUNCATE_LENGTH = 100;
    private static final String MESSAGE_TITLE = "Message";
    private static final String INFO_TITLE = "Information";
    private static final String ERROR_TITLE = "Error";
    private static final String WARNING_TITLE = "Warning";
    private static final String INPUT_TITLE = "Input";

    private static final String CONFIG_FILE_CHOOSER_WIDTH = "filechooser.width";
    private static final String CONFIG_FILE_CHOOSER_HEIGHT = "filechooser.height";

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
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            return (String) JOptionPane.showInputDialog(mainWindow, msg, title,
                    JOptionPane.QUESTION_MESSAGE, null, null, initial);
        }
        return initial;
    }

    public static int showYesNoCancel(String msg, String title, int defaultChoice) {
        String yesText = UIManager.getString("OptionPane.yesButtonText");
        String noText = UIManager.getString("OptionPane.noButtonText");
        String cancelText = UIManager.getString("OptionPane.cancelButtonText");
        return showChoice(msg, title,  JOptionPane.QUESTION_MESSAGE, yesText, noText, cancelText, defaultChoice);
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

    public static JFileChooser createFileOpener(String title, boolean allowWorkFiles, Format format) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle(title);
        boolean allowAllFileFilter = true;
        if (allowWorkFiles) {
            fc.setFileFilter(FileFilters.DOCUMENT_FILES);
            allowAllFileFilter = false;
        }
        if (format != null) {
            fc.addChoosableFileFilter(new FormatFileFilter(format));
            allowAllFileFilter = false;
        }
        fc.setCurrentDirectory(Framework.getInstance().getLastDirectory());
        fc.setAcceptAllFileFilterUsed(allowAllFileFilter);
        return fc;
    }

    public static JFileChooser createFileSaver(String title, File file, Format format) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setDialogTitle(title);
        // Set file name
        fc.setSelectedFile(file);
        // Set working directory
        if (file.exists()) {
            fc.setCurrentDirectory(file.getParentFile());
        } else {
            fc.setCurrentDirectory(Framework.getInstance().getLastDirectory());
        }
        // Set file filters
        fc.setAcceptAllFileFilterUsed(false);
        if (format == null) {
            fc.setFileFilter(FileFilters.DOCUMENT_FILES);
        } else {
            fc.setFileFilter(new FormatFileFilter(format));
        }
        return fc;
    }

    public static File chooseValidSaveFileOrCancel(JFileChooser fc, Format format) throws OperationCancelledException {
        while (DialogUtils.showFileSaver(fc)) {
            String path = fc.getSelectedFile().getPath();
            if (format == null) {
                if (!FileFilters.isWorkPath(path)) {
                    path += FileFilters.DOCUMENT_EXTENSION;
                }
            } else {
                String extension = format.getExtension();
                if (!path.endsWith(extension)) {
                    path += extension;
                }
            }

            File file = new File(path);
            if (!file.exists()) {
                return file;
            }

            String msg = "The file '" + file.getName() + "' already exists.\n" + "Overwrite it?";
            if (DialogUtils.showConfirmWarning(msg, "Save work", false)) {
                return file;
            }
        }
        throw new OperationCancelledException();
    }

    public static boolean showFileOpener(JFileChooser fc) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            loadFileChooserSize(fc);
            int returnValue = fc.showOpenDialog(mainWindow);
            saveFileChooserSize(fc);
            return returnValue == JFileChooser.APPROVE_OPTION;
        }
        return false;
    }

    public static boolean showFileSaver(JFileChooser fc) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if ((mainWindow != null) && framework.isInGuiMode()) {
            loadFileChooserSize(fc);
            int returnValue = fc.showSaveDialog(mainWindow);
            saveFileChooserSize(fc);
            return returnValue == JFileChooser.APPROVE_OPTION;
        }
        return false;
    }

    private static void loadFileChooserSize(JFileChooser fc) {
        Framework framework = Framework.getInstance();

        String widthStr = framework.getConfigVar(CONFIG_FILE_CHOOSER_WIDTH, false);
        int width = ParseUtils.parseInt(widthStr, -1);

        String heightStr = framework.getConfigVar(CONFIG_FILE_CHOOSER_HEIGHT, false);
        int height = ParseUtils.parseInt(heightStr, -1);

        if ((width > 0) && (height > 0)) {
            Dimension size = new Dimension(width, height);
            fc.setPreferredSize(size);
        }
    }

    private static void saveFileChooserSize(JFileChooser fc) {
        Framework framework = Framework.getInstance();
        framework.setConfigVar(CONFIG_FILE_CHOOSER_WIDTH, Integer.toString(fc.getWidth()), false);
        framework.setConfigVar(CONFIG_FILE_CHOOSER_HEIGHT, Integer.toString(fc.getHeight()), false);
    }

}
