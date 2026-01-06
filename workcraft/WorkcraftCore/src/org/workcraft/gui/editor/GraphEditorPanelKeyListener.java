package org.workcraft.gui.editor;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.tabs.PanelDockable;
import org.workcraft.gui.tabs.DockingUtils;
import org.workcraft.gui.tools.GraphEditorKeyListener;
import org.workcraft.utils.DesktopApi;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class GraphEditorPanelKeyListener implements KeyListener {
    private final GraphEditorPanel editor;
    private final GraphEditorKeyListener forwardListener;

    GraphEditorPanelKeyListener(GraphEditorPanel editor, GraphEditorKeyListener forwardListener) {
        this.editor = editor;
        this.forwardListener = forwardListener;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (DesktopApi.isMenuKeyDown(e)) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> editor.panLeft(e.isShiftDown());
                case KeyEvent.VK_UP -> editor.panUp(e.isShiftDown());
                case KeyEvent.VK_RIGHT -> editor.panRight(e.isShiftDown());
                case KeyEvent.VK_DOWN -> editor.panDown(e.isShiftDown());
                case KeyEvent.VK_TAB -> {
                    MainWindow mainWindow = Framework.getInstance().getMainWindow();
                    PanelDockable editorWindow = mainWindow.getEditorPanelDockable(editor);
                    DockingUtils.activateNextTab(editorWindow, e.isShiftDown() ? -1 : 1);
                }
                default -> { }
            }
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_EQUALS, KeyEvent.VK_PLUS, KeyEvent.VK_ADD -> editor.zoomIn();
                case KeyEvent.VK_MINUS, KeyEvent.VK_UNDERSCORE, KeyEvent.VK_SUBTRACT -> editor.zoomOut();
                case KeyEvent.VK_MULTIPLY -> editor.zoomFit();
                case KeyEvent.VK_DIVIDE -> editor.panCenter();
                default -> { }
            }
        }
        GraphEditorKeyEvent geke = new GraphEditorKeyEvent(editor, e);
        forwardListener.keyPressed(geke);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        GraphEditorKeyEvent geke = new GraphEditorKeyEvent(editor, e);
        forwardListener.keyReleased(geke);

    }

    @Override
    public void keyTyped(KeyEvent e) {
        GraphEditorKeyEvent geke = new GraphEditorKeyEvent(editor, e);
        forwardListener.keyTyped(geke);
    }

}
