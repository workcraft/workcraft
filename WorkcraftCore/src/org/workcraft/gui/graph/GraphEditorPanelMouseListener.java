package org.workcraft.gui.graph;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import org.workcraft.Framework;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.ToolProvider;

class GraphEditorPanelMouseListener implements MouseMotionListener, MouseListener, MouseWheelListener {
    protected GraphEditorPanel editor;
    protected boolean panDrag = false;
    private final ToolProvider toolProvider;

    protected Point lastMouseCoords = new Point();
    private Point2D prevPosition = new Point2D.Double(0, 0);
    private Point2D startPosition = null;

    GraphEditorPanelMouseListener(GraphEditorPanel editor, ToolProvider toolProvider) {
        this.editor = editor;
        this.toolProvider = toolProvider;
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
            GraphEditorTool tool = toolProvider.getSelectedTool();
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
        GraphEditorTool tool = toolProvider.getSelectedTool();
        if (tool != null) {
            tool.mouseClicked(adaptEvent(e));
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (editor.hasFocus()) {
            GraphEditorTool tool = toolProvider.getSelectedTool();
            if (tool != null) {
                tool.mouseEntered(adaptEvent(e));
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (editor.hasFocus()) {
            GraphEditorTool tool = toolProvider.getSelectedTool();
            if (tool != null) {
                tool.mouseExited(adaptEvent(e));
            }
        }
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
            GraphEditorTool tool = toolProvider.getSelectedTool();
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
            GraphEditorTool tool = toolProvider.getSelectedTool();
            editor.setCursor(tool.getCursor(false, false, false));
        } else {
            GraphEditorTool tool = toolProvider.getSelectedTool();
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
