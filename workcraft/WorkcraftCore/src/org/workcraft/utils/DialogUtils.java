package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.Format;
import org.workcraft.interop.FormatFileFilter;
import org.workcraft.workspace.FileFilters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class DialogUtils {

    private static final String MESSAGE_TITLE = "Message";
    private static final String INFO_TITLE = "Information";
    private static final String ERROR_TITLE = "Error";
    private static final String WARNING_TITLE = "Warning";
    private static final String INPUT_TITLE = "Input";

    private static final String CONFIG_FILE_CHOOSER_WIDTH = "filechooser.width";
    private static final String CONFIG_FILE_CHOOSER_HEIGHT = "filechooser.height";
    private static final String YES_BUTTON_LABEL = "Yes";
    private static final String NO_BUTTON_LABEL = "No";
    private static final String CANCEL_BUTTON_LABEL = "Cancel";

    private static void logMessage(String message, int messageType) {
        if ((message != null) && !message.isEmpty()) {
            switch (messageType) {
                case JOptionPane.INFORMATION_MESSAGE -> LogUtils.logInfo(message);
                case JOptionPane.WARNING_MESSAGE -> LogUtils.logWarning(message);
                case JOptionPane.ERROR_MESSAGE -> LogUtils.logError(message);
                default -> LogUtils.logMessage(message);
            }
        }
    }

    public static void showMessage(String message, String title, int messageType, boolean log) {
        if (log) {
            logMessage(message, messageType);
        }
        Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            MainWindow mainWindow = framework.getMainWindow();
            String text = TextUtils.truncateLines(message);
            JOptionPane.showMessageDialog(mainWindow, text, title, messageType);
        }
    }

    public static void showMessage(String message) {
        showMessage(message, MESSAGE_TITLE);
    }

    public static void showMessage(String message, String title) {
        showMessage(message, title, JOptionPane.PLAIN_MESSAGE, true);
    }

    public static void showInfo(String message) {
        showInfo(message, INFO_TITLE);
    }

    public static void showInfo(String message, String title) {
        showMessage(message, title, JOptionPane.INFORMATION_MESSAGE, true);
    }

    public static void showWarning(String message) {
        showWarning(message, WARNING_TITLE);
    }

    public static void showWarning(String message, String title) {
        showMessage(message, title, JOptionPane.WARNING_MESSAGE, true);
    }

    public static void showError(String message) {
        showError(message, ERROR_TITLE);
    }

    public static void showError(String message, String title) {
        showMessage(message, title, JOptionPane.ERROR_MESSAGE, true);
    }

    public static boolean showConfirm(String message, String question, String title, boolean defaultChoice) {
        return showConfirm(message, question, title, defaultChoice, JOptionPane.QUESTION_MESSAGE, true);
    }

    public static boolean showConfirmInfo(String message, String question) {
        return showConfirmInfo(message, question, INFO_TITLE, true);
    }

    public static boolean showConfirmInfo(String message, String question, String title, boolean defaultChoice) {
        return showConfirm(message, question, title, defaultChoice, JOptionPane.INFORMATION_MESSAGE, true);
    }

    public static boolean showConfirmWarning(String message, String question) {
        return showConfirmWarning(message, question, WARNING_TITLE, true);
    }

    public static boolean showConfirmWarning(String message, String question, String title, boolean defaultChoice) {
        return showConfirm(message, question, title, defaultChoice, JOptionPane.WARNING_MESSAGE, true);
    }

    public static boolean showConfirmError(String message, String question) {
        return showConfirmError(message, question, ERROR_TITLE,  true);
    }

    public static boolean showConfirmError(String message, String question, String title, boolean defaultChoice) {
        return showConfirm(message, question, title, defaultChoice, JOptionPane.ERROR_MESSAGE, true);
    }

    public static boolean showConfirm(String message, String question, String title, boolean defaultChoice,
            int messageType, boolean log) {

        boolean result = defaultChoice;
        if (log) {
            logMessage(message, messageType);
        }
        Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            JButton yesButton = new JButton();
            JButton noButton = new JButton();
            JButton[] options = {yesButton, noButton};
            JOptionPane pane = new JOptionPane(TextUtils.truncateLines(message) + question,
                    messageType, JOptionPane.YES_NO_OPTION, null, options, defaultChoice ? yesButton : noButton);

            JDialog dialog = pane.createDialog(framework.getMainWindow(), title);
            dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            setupDialogPaneButton(dialog, pane, yesButton, YES_BUTTON_LABEL);
            setupDialogPaneButton(dialog, pane, noButton, NO_BUTTON_LABEL);

            dialog.pack();
            dialog.setVisible(true);
            dialog.dispose();
            result = pane.getValue() == yesButton;
        }
        return result;
    }

    public static int showYesNoCancel(String message, String title) {
        return showYesNoCancel(message, title, JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showYesNoCancelWarning(String message, String title) {
        return showYesNoCancel(message, title, JOptionPane.YES_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    public static int showYesNoCancel(String message, String title, int defaultChoice, int messageType) {
        int result = JOptionPane.CANCEL_OPTION;
        Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            JButton yesButton = new JButton();
            JButton noButton = new JButton();
            JButton cancelButton = new JButton();

            JButton[] options = {yesButton, noButton, cancelButton};
            JButton defaultOption = cancelButton;
            if (defaultChoice == JOptionPane.YES_OPTION) {
                defaultOption = yesButton;
            } else if (defaultChoice == JOptionPane.NO_OPTION) {
                defaultOption = noButton;
            }
            JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.YES_NO_CANCEL_OPTION,
                    null, options, defaultOption);

            JDialog dialog = pane.createDialog(framework.getMainWindow(), title);
            dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            setupDialogPaneButton(dialog, pane, yesButton, YES_BUTTON_LABEL);
            setupDialogPaneButton(dialog, pane, noButton, NO_BUTTON_LABEL);
            setupDialogPaneButton(dialog, pane, cancelButton, CANCEL_BUTTON_LABEL);

            dialog.pack();
            dialog.setVisible(true);
            dialog.dispose();
            if (pane.getValue() == yesButton) {
                result = JOptionPane.YES_OPTION;
            } else if (pane.getValue() == noButton) {
                result = JOptionPane.NO_OPTION;
            }
        }
        return result;
    }

    public static boolean showOkCancel(String message, String title) {
        return showOkCancel(message, title, true, JOptionPane.PLAIN_MESSAGE);
    }


    public static boolean showOkCancelWarning(String message, String title) {
        return showOkCancel(message, title, true, JOptionPane.WARNING_MESSAGE);
    }

    private static boolean showOkCancel(String message, String title, boolean defaultChoice, int messageType) {
        Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            MainWindow mainWindow = framework.getMainWindow();
            int result = JOptionPane.showConfirmDialog(mainWindow, message, title, JOptionPane.OK_CANCEL_OPTION, messageType);
            return result == JOptionPane.OK_OPTION;
        }
        return defaultChoice;
    }

    private static void setupDialogPaneButton(JDialog dialog, JOptionPane pane, JButton button, String label) {
        button.setText(label);
        char mnemonic = label.charAt(0);
        button.setMnemonic(mnemonic);
        int key = KeyEvent.getExtendedKeyCodeForChar(mnemonic);
        button.addActionListener(l -> buttonAction(dialog, pane, button));
        dialog.getRootPane().registerKeyboardAction(event -> buttonAction(dialog, pane, button),
                KeyStroke.getKeyStroke(key, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private static void buttonAction(JDialog dialog, JOptionPane pane, JButton button) {
        pane.setValue(button);
        dialog.setVisible(false);
    }

    public static String showInput(String message, String initial) {
        return showInput(message, INPUT_TITLE, initial);
    }

    public static String showInput(String message, String title, String initial) {
        Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            MainWindow mainWindow = framework.getMainWindow();
            return (String) JOptionPane.showInputDialog(mainWindow, message, title,
                    JOptionPane.QUESTION_MESSAGE, null, null, initial);
        }
        return initial;
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

            String message = "The file '" + file.getName() + "' already exists";
            String question = ".\nOverwrite it?";
            if (DialogUtils.showConfirmWarning(message, question, "Save work", false)) {
                return file;
            }
        }
        throw new OperationCancelledException();
    }

    public static boolean showFileOpener(JFileChooser fc) {
        Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            MainWindow mainWindow = framework.getMainWindow();
            loadFileChooserSize(fc);
            int returnValue = fc.showOpenDialog(mainWindow);
            saveFileChooserSize(fc);
            return returnValue == JFileChooser.APPROVE_OPTION;
        }
        return false;
    }

    public static boolean showFileSaver(JFileChooser fc) {
        Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            loadFileChooserSize(fc);
            MainWindow mainWindow = framework.getMainWindow();
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
