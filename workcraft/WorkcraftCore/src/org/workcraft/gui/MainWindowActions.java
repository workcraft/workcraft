package org.workcraft.gui;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.dialogs.AboutDialog;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
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

    public static final Action CREATE_WORK_ACTION = new Action("Create work...",
            () -> Framework.getInstance().getMainWindow().createWork(),
            KeyStroke.getKeyStroke(KeyEvent.VK_N, DesktopApi.getMenuKeyMask()));

    public static final Action OPEN_WORK_ACTION = new Action("Open work...",
            () -> Framework.getInstance().getMainWindow().openWork(),
            KeyStroke.getKeyStroke(KeyEvent.VK_O, DesktopApi.getMenuKeyMask()));

    public static final Action MERGE_WORK_ACTION = new Action("Merge work...",
            () -> Framework.getInstance().getMainWindow().mergeWork());

    public static final Action SAVE_WORK_ACTION = new Action("Save work",
            () -> Framework.getInstance().getMainWindow().saveWork(),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, DesktopApi.getMenuKeyMask()));

    public static final Action SAVE_WORK_AS_ACTION = new Action("Save work as...",
            () -> Framework.getInstance().getMainWindow().saveWorkAs());

    public static final Action CLOSE_ACTIVE_EDITOR_ACTION = new Action("Close active work",
            () -> Framework.getInstance().getMainWindow().closeActiveEditor(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F4, DesktopApi.getMenuKeyMask()));

    public static final Action CLOSE_ALL_EDITORS_ACTION = new Action("Close all works",
            () -> Framework.getInstance().getMainWindow().closeEditorWindows());

    public static final Action SHUTDOWN_GUI_ACTION = new Action("Switch to console mode",
            () -> {
                try {
                    Framework.getInstance().shutdownGUI();
                } catch (OperationCancelledException ignored) {
                    // Operation cancelled by the user
                }
            });

    public static final Action EXIT_ACTION = new Action("Exit",
            () -> Framework.getInstance().shutdown(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));

    public static final Action EDIT_UNDO_ACTION = new Action("Undo",
            () -> Framework.getInstance().getMainWindow().undo(),
            DesktopApi.getUndoKeyStroke());

    public static final Action EDIT_REDO_ACTION = new Action("Redo",
            () -> Framework.getInstance().getMainWindow().redo(),
            DesktopApi.getRedoKeyStroke());

    public static final Action EDIT_CUT_ACTION = new Action("Cut",
            () -> Framework.getInstance().getMainWindow().cut(),
            KeyStroke.getKeyStroke(KeyEvent.VK_X, DesktopApi.getMenuKeyMask()));

    public static final Action EDIT_COPY_ACTION = new Action("Copy",
            () -> Framework.getInstance().getMainWindow().copy(),
            KeyStroke.getKeyStroke(KeyEvent.VK_C, DesktopApi.getMenuKeyMask()));

    public static final Action EDIT_PASTE_ACTION = new Action("Paste",
            () -> {
                GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
                if (!editor.hasFocus()) {
                    editor.getWorkspaceEntry().setPastePosition(null);
                }
                Framework.getInstance().getMainWindow().paste();
            }, KeyStroke.getKeyStroke(KeyEvent.VK_V, DesktopApi.getMenuKeyMask()));

    public static final Action EDIT_DELETE_ACTION = new Action("Delete",
            () -> Framework.getInstance().getMainWindow().delete(),
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

    public static final Action EDIT_SELECT_ALL_ACTION = new Action("Select all",
            () -> Framework.getInstance().getMainWindow().selectAll(),
            KeyStroke.getKeyStroke(KeyEvent.VK_A, DesktopApi.getMenuKeyMask()));

    public static final Action EDIT_SELECT_INVERSE_ACTION = new Action("Inverse selection",
            () -> Framework.getInstance().getMainWindow().selectInverse(),
            KeyStroke.getKeyStroke(KeyEvent.VK_I, DesktopApi.getMenuKeyMask()));

    public static final Action EDIT_SELECT_NONE_ACTION = new Action("Deselect",
            () -> Framework.getInstance().getMainWindow().selectNone());

    public static final Action EDIT_SETTINGS_ACTION = new Action("Preferences...",
            () -> Framework.getInstance().getMainWindow().editSettings());

    public static final Action VIEW_ZOOM_IN = new Action("Zoom in",
            () -> {
                final GraphEditor editor = getCurrentEditor();
                if (editor != null) {
                    editor.zoomIn();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, DesktopApi.getMenuKeyMask()));

    public static final Action VIEW_ZOOM_OUT = new Action("Zoom out",
            () -> {
                final GraphEditor editor = getCurrentEditor();
                if (editor != null) {
                    editor.zoomOut();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, DesktopApi.getMenuKeyMask()));

    public static final Action VIEW_ZOOM_DEFAULT = new Action("Default zoom",
            () -> {
                final GraphEditor editor = getCurrentEditor();
                if (editor != null) {
                    editor.zoomDefault();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_0, DesktopApi.getMenuKeyMask()));

    public static final Action VIEW_PAN_CENTER = new Action("Center selection",
            () -> {
                final GraphEditor editor = getCurrentEditor();
                if (editor != null) {
                    editor.panCenter();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_T, DesktopApi.getMenuKeyMask()));

    public static final Action VIEW_ZOOM_FIT = new Action("Fit selection to screen",
            () -> {
                final GraphEditor editor = getCurrentEditor();
                if (editor != null) {
                    editor.zoomFit();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_F, DesktopApi.getMenuKeyMask()));

    public static final Action VIEW_PAN_LEFT = new Action("Pan left",
            () -> {
                final GraphEditor editor = getCurrentEditor();
                if (editor != null) {
                    editor.panLeft(false);
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, DesktopApi.getMenuKeyMask()));

    public static final Action VIEW_PAN_UP = new Action("Pan up",
            () -> {
                final GraphEditor editor = getCurrentEditor();
                if (editor != null) {
                    editor.panUp(false);
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_UP, DesktopApi.getMenuKeyMask()));

    public static final Action VIEW_PAN_RIGHT = new Action("Pan right",
            () -> {
                final GraphEditor editor = getCurrentEditor();
                if (editor != null) {
                    editor.panRight(false);
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, DesktopApi.getMenuKeyMask()));

    public static final Action VIEW_PAN_DOWN = new Action("Pan down",
            () -> {
                final GraphEditor editor = getCurrentEditor();
                if (editor != null) {
                    editor.panDown(false);
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, DesktopApi.getMenuKeyMask()));

    public static final Action TOGGLE_GRID = new Action("Toggle grid visibility",
            () -> {
                EditorCommonSettings.setGridVisibility(!EditorCommonSettings.getGridVisibility());
                repaintAndFocusCurrentEditor();
            });

    public static final Action TOGGLE_RULER = new Action("Toggle ruler visibility",
            () -> {
                EditorCommonSettings.setRulerVisibility(!EditorCommonSettings.getRulerVisibility());
                repaintAndFocusCurrentEditor();
            });

    public static final Action TOGGLE_NAME = new Action("Toggle name visibility",
            () -> {
                VisualCommonSettings.setNameVisibility(!VisualCommonSettings.getNameVisibility());
                repaintAndFocusCurrentEditor();
            });

    public static final Action TOGGLE_LABEL = new Action("Toggle label visibility",
            () -> {
                VisualCommonSettings.setLabelVisibility(!VisualCommonSettings.getLabelVisibility());
                repaintAndFocusCurrentEditor();
            });

    public static final Action RESET_GUI_ACTION = new Action("Reset UI layout",
            () -> Framework.getInstance().getMainWindow().resetLayout());

    public static final Action HELP_OVERVIEW_ACTION = new Action("Overview",
            () -> FileUtils.openExternally("overview/start.html", "Overview access error"));

    public static final Action HELP_CONTENTS_ACTION = new Action("Help contents",
            () -> FileUtils.openExternally("help/start.html", "Help access error"),
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

    public static final Action HELP_TUTORIALS_ACTION = new Action("Tutorials",
            () -> FileUtils.openExternally("tutorial/start.html", "Tutorials access error"));

    public static final Action HELP_BUGREPORT_ACTION = new Action("Report issue at GitHub",
            () -> {
                try {
                    URI uri = new URI("https://github.com/tuura/workcraft/issues/new");
                    DesktopApi.browse(uri);
                } catch (URISyntaxException e) {
                    LogUtils.logError(e.getMessage());
                }
            });

    public static final Action HELP_ABOUT_ACTION = new Action("About Workcraft",
            () -> new AboutDialog(Framework.getInstance().getMainWindow()).reveal());

}
