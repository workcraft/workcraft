package org.workcraft.gui;

import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.CommonEditorSettings;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;

public class MainWindowActions {

    private static GraphEditor getCurrentEditor() {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        return mainWindow.getCurrentEditor();
    }

    private static void repaintAndFocusCurrentEditor() {
        final GraphEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.repaint();
            editor.requestFocus();
        }
    }

    public static final Action CREATE_WORK_ACTION = new Action() {
        @Override
        public String getText() {
            return "Create work...";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_N, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            try {
                Framework.getInstance().getMainWindow().createWork();
            } catch (OperationCancelledException e) {
            }
        }
    };

    public static final Action OPEN_WORK_ACTION = new Action() {
        @Override
        public String getText() {
            return "Open work...";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            try {
                Framework.getInstance().getMainWindow().openWork();
            } catch (OperationCancelledException e) {
            }
        }
    };

    public static final Action MERGE_WORK_ACTION = new Action() {
        @Override
        public String getText() {
            return "Merge work...";
        }
        @Override
        public void run() {
            try {
                Framework.getInstance().getMainWindow().mergeWork();
            } catch (OperationCancelledException e) {
            }
        }
    };

    public static final Action SAVE_WORK_ACTION = new Action() {
        @Override
        public String getText() {
            return "Save work";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_S, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            try {
                Framework.getInstance().getMainWindow().saveWork();
            } catch (OperationCancelledException e) {
            }
        }
    };

    public static final Action SAVE_WORK_AS_ACTION = new Action() {
        @Override
        public String getText() {
            return "Save work as...";
        }
        @Override
        public void run() {
            try {
                Framework.getInstance().getMainWindow().saveWorkAs();
            } catch (OperationCancelledException e) {
            }
        }
    };

    public static final Action CLOSE_ACTIVE_EDITOR_ACTION = new Action() {
        @Override
        public String getText() {
            return "Close active work";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_F4, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            try {
                Framework.getInstance().getMainWindow().closeActiveEditor();
            } catch (OperationCancelledException e) {
            }
        }
    };

    public static final Action CLOSE_ALL_EDITORS_ACTION = new Action() {
        @Override
        public String getText() {
            return "Close all works";
        }
        @Override
        public void run() {
            try {
                Framework.getInstance().getMainWindow().closeEditorWindows();
            } catch (OperationCancelledException e) {
            }
        }
    };

    public static final Action SHUTDOWN_GUI_ACTION = new Action() {
        @Override
        public String getText() {
            return "Switch to console mode";
        }
        @Override
        public void run() {
            try {
                Framework.getInstance().shutdownGUI();
            } catch (OperationCancelledException e) { }
        }
    };

    public static final Action EXIT_ACTION = new Action() {
        @Override
        public String getText() {
            return "Exit";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK);
        }
        @Override
        public void run() {
            Framework.getInstance().shutdown();
        }
    };

    public static final Action IMPORT_ACTION = new Action() {
        @Override
        public String getText() {
            return "Import...";
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().importFrom();
        }
    };

    public static final Action EDIT_UNDO_ACTION = new Action() {
        @Override
        public String getText() {
            return "Undo";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_Z, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().undo();
        }
    };

    public static final Action EDIT_REDO_ACTION = new Action() {
        @Override
        public String getText() {
            return "Redo";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_Z, DesktopApi.getMenuKeyMask() | ActionEvent.SHIFT_MASK);
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().redo();
        }
    };

    public static final Action EDIT_CUT_ACTION = new Action() {
        @Override
        public String getText() {
            return "Cut";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_X, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().cut();
        }
    };

    public static final Action EDIT_COPY_ACTION = new Action() {
        @Override
        public String getText() {
            return "Copy";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().copy();
        }
    };

    public static final Action EDIT_PASTE_ACTION = new Action() {
        @Override
        public String getText() {
            return "Paste";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_V, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            GraphEditorPanel editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (!editor.hasFocus()) {
                editor.getWorkspaceEntry().setPastePosition(null);
            }
            Framework.getInstance().getMainWindow().paste();
        }
    };

    public static final Action EDIT_DELETE_ACTION = new Action() {
        @Override
        public String getText() {
            return "Delete";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().delete();
        }
    };

    public static final Action EDIT_SELECT_ALL_ACTION = new Action() {
        @Override
        public String getText() {
            return "Select all";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_A, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().selectAll();
        }
    };

    public static final Action EDIT_SELECT_INVERSE_ACTION = new Action() {
        @Override
        public String getText() {
            return "Inverse selection";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_I, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().selectInverse();
        }
    };

    public static final Action EDIT_SELECT_NONE_ACTION = new Action() {
        @Override
        public String getText() {
            return "Deselect";
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().selectNone();
        }
    };

    public static final Action EDIT_SETTINGS_ACTION = new Action() {
        @Override
        public String getText() {
            return "Preferences...";
        }
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().editSettings();
        }
    };

    public static final Action VIEW_ZOOM_IN = new Action() {
        @Override
        public String getText() {
            return "Zoom in";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            final GraphEditor editor = getCurrentEditor();
            if (editor != null) {
                editor.zoomIn();
            }
        }
    };

    public static final Action VIEW_ZOOM_OUT = new Action() {
        @Override
        public String getText() {
            return "Zoom out";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            final GraphEditor editor = getCurrentEditor();
            if (editor != null) {
                editor.zoomOut();
            }
        }
    };

    public static final Action VIEW_ZOOM_DEFAULT = new Action() {
        @Override
        public String getText() {
            return "Default zoom";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_0, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            final GraphEditor editor = getCurrentEditor();
            if (editor != null) {
                editor.zoomDefault();
            }
        }
    };

    public static final Action VIEW_PAN_CENTER = new Action() {
        @Override
        public String getText() {
            return "Center selection";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_T, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            final GraphEditor editor = getCurrentEditor();
            if (editor != null) {
                editor.panCenter();
            }
        }
    };

    public static final Action VIEW_ZOOM_FIT = new Action() {
        @Override
        public String getText() {
            return "Fit selection to screen";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_F, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            final GraphEditor editor = getCurrentEditor();
            if (editor != null) {
                editor.zoomFit();
            }
        }
    };

    public static final Action VIEW_PAN_LEFT = new Action() {
        @Override
        public String getText() {
            return "Pan left";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            final GraphEditor editor = getCurrentEditor();
            if (editor != null) {
                editor.panLeft();
            }
        }
    };

    public static final Action VIEW_PAN_UP = new Action() {
        @Override
        public String getText() {
            return "Pan up";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_UP, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            final GraphEditor editor = getCurrentEditor();
            if (editor != null) {
                editor.panUp();
            }
        }
    };

    public static final Action VIEW_PAN_RIGHT = new Action() {
        @Override
        public String getText() {
            return "Pan right";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            final GraphEditor editor = getCurrentEditor();
            if (editor != null) {
                editor.panRight();
            }
        }
    };

    public static final Action VIEW_PAN_DOWN = new Action() {
        @Override
        public String getText() {
            return "Pan down";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, DesktopApi.getMenuKeyMask());
        }
        @Override
        public void run() {
            final GraphEditor editor = getCurrentEditor();
            if (editor != null) {
                editor.panDown();
            }
        }
    };

    public static final Action TOGGLE_GRID = new Action() {
        @Override
        public String getText() {
            return "Toggle grid visibility";
        }
        @Override
        public void run() {
            CommonEditorSettings.setGridVisibility(!CommonEditorSettings.getGridVisibility());
            repaintAndFocusCurrentEditor();
        }
    };

    public static final Action TOGGLE_RULER = new Action() {
        @Override
        public String getText() {
            return "Toggle ruler visibility";
        }
        @Override
        public void run() {
            CommonEditorSettings.setRulerVisibility(!CommonEditorSettings.getRulerVisibility());
            repaintAndFocusCurrentEditor();
        }
    };

    public static final Action TOGGLE_NAME = new Action() {
        @Override
        public String getText() {
            return "Toggle name visibility";
        }
        @Override
        public void run() {
            CommonVisualSettings.setNameVisibility(!CommonVisualSettings.getNameVisibility());
            repaintAndFocusCurrentEditor();
        }
    };

    public static final Action TOGGLE_LABEL = new Action() {
        @Override
        public String getText() {
            return "Toggle label visibility";
        }
        @Override
        public void run() {
            CommonVisualSettings.setLabelVisibility(!CommonVisualSettings.getLabelVisibility());
            repaintAndFocusCurrentEditor();
        }
    };

    public static final Action RESET_GUI_ACTION = new Action() {
        @Override
        public String getText() {
            return "Reset UI layout";
        }
        @Override
        public void run() {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            mainWindow.resetLayout();
        }
    };

    public static final Action HELP_OVERVIEW_ACTION = new Action() {
        @Override
        public String getText() {
            return "Overview";
        }
        @Override
        public void run() {
            FileUtils.openExternally("overview/start.html", "Overview access error");
        }
    };

    public static final Action HELP_CONTENTS_ACTION = new Action() {
        @Override
        public String getText() {
            return "Help contents";
        }
        @Override
        public KeyStroke getKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        }
        @Override
        public void run() {
            FileUtils.openExternally("help/start.html", "Help access error");
        }
    };

    public static final Action HELP_TUTORIALS_ACTION = new Action() {
        @Override
        public String getText() {
            return "Tutorials";
        }
        @Override
        public void run() {
            FileUtils.openExternally("tutorial/start.html", "Tutorials access error");
        }
    };

    public static final Action HELP_BUGREPORT_ACTION = new Action() {
        @Override
        public String getText() {
            return "Report a bug at GitHub";
        }
        @Override
        public void run() {
            try {
                URI uri = new URI("https://github.com/tuura/workcraft/issues/new");
                DesktopApi.browse(uri);
            } catch (URISyntaxException e) {
                LogUtils.logError(e.getMessage());
            }
        }
    };

    public static final Action HELP_EMAIL_ACTION = new Action() {
        @Override
        public String getText() {
            return "Contact developers by e-mail";
        }
        @Override
        public void run() {
            try {
                URI uri = new URI("mailto", Info.getEmail(), null);
                DesktopApi.browse(uri);
            } catch (URISyntaxException e) {
                LogUtils.logError(e.getMessage());
            }
        }
    };

    public static final Action HELP_ABOUT_ACTION = new Action() {
        @Override
        public String getText() {
            return "About Workcraft";
        }
        @Override
        public void run() {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            new AboutDialog(mainWindow).reveal();
        }
    };

}
