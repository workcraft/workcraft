package org.workcraft.gui.editor;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.utils.DesktopApi;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

class GraphEditorPanelMouseListener implements MouseMotionListener, MouseListener, MouseWheelListener {
    protected GraphEditorPanel editor;
    protected boolean panDrag = false;
    private final Toolbox toolbox;

    protected Point lastMouseCoords = new Point();
    private Point2D prevPosition = new Point2D.Double(0, 0);
    private Point2D startPosition = null;

    GraphEditorPanelMouseListener(GraphEditorPanel editor, Toolbox toolbox) {
        this.editor = editor;
        this.toolbox = toolbox;
    }

    private GraphEditorMouseEvent adaptEvent(MouseEvent e) {
        return new GraphEditorMouseEvent(editor, e, startPosition, prevPosition);
    }

    private boolean isPanCombo(MouseEvent e) {
        return (e.getButton() == MouseEvent.BUTTON2) || (DesktopApi.isMenuKeyDown(e) && e.getButton() == MouseEvent.BUTTON3);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point currentMouseCoords = e.getPoint();
        Viewport viewport = editor.getViewport();
        if (panDrag) {
            viewport.pan(currentMouseCoords.x - lastMouseCoords.x,
                    currentMouseCoords.y - lastMouseCoords.y);
            editor.repaint();
        } else {
            GraphEditorTool tool = toolbox.getSelectedTool();
            if (tool != null) {
                if (!tool.isDragging() && startPosition != null) {
                    tool.startDrag(adaptEvent(e));
                }
                tool.mouseMoved(adaptEvent(e));
            }
        }
        prevPosition = viewport.screenToUser(currentMouseCoords);
        lastMouseCoords = currentMouseCoords;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!editor.hasFocus()) {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            mainWindow.requestFocus(editor);
        }
        GraphEditorTool tool = toolbox.getSelectedTool();
        if (tool != null) {
            tool.mouseClicked(adaptEvent(e));
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!editor.hasFocus()) {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            mainWindow.requestFocus(editor);
        }
        if (isPanCombo(e)) {
            panDrag = true;
            editor.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else {
            GraphEditorTool tool = toolbox.getSelectedTool();
            Viewport viewport = editor.getViewport();
            Point point = e.getPoint();
            if (tool != null) {
                if (!tool.isDragging()) {
                    startPosition = viewport.screenToUser(point);
                }
                tool.mousePressed(adaptEvent(e));
            } else {
                startPosition = viewport.screenToUser(point);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isPanCombo(e) || (e.getButton() == MouseEvent.BUTTON3 && panDrag)) {
            panDrag = false;
            GraphEditorTool tool = toolbox.getSelectedTool();
            editor.setCursor(tool.getCursor(false, false, false));
        } else {
            GraphEditorTool tool = toolbox.getSelectedTool();
            if (tool != null) {
                if (tool.isDragging()) {
                    tool.finishDrag(adaptEvent(e));
                }
                tool.mouseReleased(adaptEvent(e));
            }
            startPosition = null;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (editor.hasFocus()) {
            editor.getViewport().zoom(-e.getWheelRotation(), e.getPoint());
            editor.repaint();
        }
    }

}
