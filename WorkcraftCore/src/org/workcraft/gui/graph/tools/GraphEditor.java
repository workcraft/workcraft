package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;
import java.util.Set;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.graph.EditorOverlay;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.workspace.WorkspaceEntry;

public interface GraphEditor {
    Viewport getViewport();
    EditorOverlay getOverlay();
    WorkspaceEntry getWorkspaceEntry();
    VisualModel getModel();
    int getWidth();
    int getHeight();
    Set<Point2D> getSnaps(VisualNode node);
    Point2D snap(Point2D pos, Set<Point2D> snaps);
    void repaint();
    void forceRedraw();
    boolean hasFocus();
    void requestFocus();

    void zoomIn();
    void zoomOut();
    void zoomDefault();
    void zoomFit();

    void panLeft();
    void panUp();
    void panRight();
    void panDown();
    void panCenter();

}
