package org.workcraft.gui.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.*;

public class ConnectionTool extends AbstractGraphEditorTool {

    public enum ContinuousConnectionMode { FAN, FOLD }

    protected static final Color incompleteConnectionColor = Color.GREEN;
    protected static final Color validConnectionColor = Color.BLUE;
    protected static final Color invalidConnectionColor = Color.RED;

    protected boolean forbidSelfLoops;
    protected boolean directedArcs;
    protected boolean allLevels;

    protected VisualNode firstNode = null;
    protected VisualNode currentNode = null;

    private Point2D firstPoint = null;
    private Point2D currentPoint = null;
    private LinkedList<Point2D> controlPoints = new LinkedList<>();
    private boolean mouseLeftFirstNode = false;

    public ConnectionTool() {
        this(true, true, true);
    }

    public ConnectionTool(boolean forbidSelfLoops, boolean directedArcs, boolean allLevels) {
        this.forbidSelfLoops = forbidSelfLoops;
        this.directedArcs = directedArcs;
        this.allLevels = allLevels;
    }

    @Override
    public Icon getIcon() {
        return GuiUtils.createIconFromSVG("images/tool-connection.svg");
    }

    @Override
    public String getLabel() {
        return "Connect";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_C;
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    @Override
    public boolean requiresPropertyEditor() {
        return true;
    }

    private void resetState(GraphEditor editor) {
        currentPoint = null;
        currentNode = null;
        firstPoint = null;
        firstNode = null;
        mouseLeftFirstNode = false;
        editor.getModel().selectNone();
        setPermissions(editor);
    }

    protected void updateCurrentNode(GraphEditor editor) {
        if (allLevels) {
            currentNode = HitMan.hitDeepest(currentPoint, editor.getModel());
        } else {
            currentNode = HitMan.hitFirstInCurrentLevel(currentPoint, editor.getModel());
        }
        if ((currentNode == null) || isConnectable(currentNode)) {
            if (currentNode != firstNode) {
                mouseLeftFirstNode = true;
                hideIssue(editor);
            }
        } else {
            currentNode = null;
        }
    }

    public boolean isConnectable(Node node) {
        return (node != null)
                && !(node instanceof VisualGroup)
                && !(node instanceof VisualPage)
                && !(node instanceof VisualConnection)
                && !(node instanceof VisualComment);
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        resetState(editor);
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        resetState(editor);
    }

    @Override
    public void setPermissions(GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(true);
        we.setCanSelect(true);
        we.setCanCopy(false);
    }

    @Override
    public VisualConnection createTemplateNode() {
        return new VisualConnection();
    }

    @Override
    public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
        if ((firstNode != null) && (currentPoint != null)) {
            g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));
            Path2D path = new Path2D.Double();
            path.moveTo(firstPoint.getX(), firstPoint.getY());
            for (Point2D point : controlPoints) {
                path.lineTo(point.getX(), point.getY());
            }
            path.lineTo(currentPoint.getX(), currentPoint.getY());
            if (currentNode == null) {
                g.setColor(incompleteConnectionColor);
                g.draw(path);
            } else {
                try {
                    VisualModel model = editor.getModel();
                    if (directedArcs) {
                        model.validateConnection(firstNode, currentNode);
                    } else {
                        model.validateUndirectedConnection(firstNode, currentNode);
                    }
                    g.setColor(validConnectionColor);
                    g.draw(path);
                } catch (InvalidConnectionException e) {
                    showIssue(editor, e.getMessage());
                    g.setColor(invalidConnectionColor);
                    g.draw(path);
                }
            }
        }
    }

    @Override
    public void mouseMoved(GraphEditorMouseEvent e) {
        currentPoint = e.getPosition();
        GraphEditor editor = e.getEditor();
        updateCurrentNode(editor);
        editor.repaint();
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        currentPoint = e.getPosition();
        GraphEditor editor = e.getEditor();
        updateCurrentNode(editor);
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (currentNode == null) {
                if (firstNode != null) {
                    controlPoints.add(currentPoint);
                }
            } else {
                if (firstNode == null) {
                    startConnection(e);
                } else if ((firstNode == currentNode) && (forbidSelfLoops || !mouseLeftFirstNode)) {
                    if (forbidSelfLoops) {
                        showIssue(editor, "Self-loops are not allowed.");
                    } else {
                        showIssue(editor, "Move the mouse outside this node before creating a self-loop.");
                    }
                } else if ((firstNode instanceof VisualGroup) || (currentNode instanceof VisualGroup)) {
                    showIssue(editor, "Connection with group element is not allowed.");
                } else {
                    editor.getWorkspaceEntry().saveMemento();
                    finishConnection(e);
                    if (e.isMenuKeyDown()) {
                        startConnection(e);
                    } else {
                        resetState(editor);
                    }
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            resetState(editor);
        }
        editor.repaint();
    }

    public ContinuousConnectionMode getContinuousConnectionMode() {
        return ContinuousConnectionMode.FOLD;
    }

    public void startConnection(GraphEditorMouseEvent e) {
        if ((firstNode == null) || (getContinuousConnectionMode() == ContinuousConnectionMode.FOLD)) {
            firstNode = currentNode;
        }
        AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(firstNode);
        if (firstNode instanceof VisualConnection connection) {
            AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
            Point2D currentPointInLocalSpace = rootToLocalTransform.transform(currentPoint, null);
            Point2D nearestPointInLocalSpace = connection.getNearestPointOnConnection(currentPointInLocalSpace);
            firstPoint = localToRootTransform.transform(nearestPointInLocalSpace, null);
        } else {
            firstPoint = TransformHelper.transform(firstNode, localToRootTransform).getCenter();
        }
        currentNode = null;
        mouseLeftFirstNode = false;
        controlPoints = new LinkedList<>();
        GraphEditor editor = e.getEditor();
        hideIssue(editor);
        editor.getWorkspaceEntry().setCanModify(false);
    }

    public VisualConnection finishConnection(GraphEditorMouseEvent e) {
        VisualConnection connection = null;
        try {
            if (firstNode instanceof VisualConnection vc) {
                AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(vc);
                vc.setSplitPoint(rootToLocalTransform.transform(firstPoint, null));
            }
            if (currentNode instanceof VisualConnection vc) {
                AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(vc);
                vc.setSplitPoint(rootToLocalTransform.transform(currentPoint, null));
            }
            VisualModel model = e.getEditor().getModel();
            if (directedArcs) {
                connection = model.connect(firstNode, currentNode);
            } else {
                connection = model.connectUndirected(firstNode, currentNode);
                if ((connection != null) && (connection.getSecond() != currentNode)) {
                    // Reverse the list of control points if the undirected connection is reverted.
                    Collections.reverse(controlPoints);
                }
            }
            if (connection != null) {
                connection.copyStyle(getTemplateNode());
                if (controlPoints.isEmpty() && (firstNode == currentNode)) {
                    // Self-loop without predefined control points.
                    connection.getGraphic().setDefaultControlPoints();
                } else {
                    ConnectionHelper.addControlPoints(connection, controlPoints);
                    snapControlPoints(e.getEditor(), connection);
                }
            }
        } catch (InvalidConnectionException exception) {
            Toolkit.getDefaultToolkit().beep();
        }
        return connection;
    }

    public static void snapControlPoints(GraphEditor editor, VisualConnection connection) {
        ConnectionGraphic graphic = connection.getGraphic();
        if (graphic instanceof Polyline polyline) {
            int count = polyline.getControlPointCount();

            Point2D headSnapPoint = connection.getFirstCenter();
            Point2D tailSnapPoint = connection.getSecondCenter();
            for (int headIndex = 0; headIndex < (count + 1) / 2; headIndex++) {
                int tailIndex = count - headIndex - 1;
                if (headIndex == tailIndex) {
                    ControlPoint controlPoint = polyline.getControlPoint(headIndex);
                    Set<Point2D> snaps = new HashSet<>(Arrays.asList(headSnapPoint, tailSnapPoint));
                    Point2D snapPos = editor.snap(controlPoint.getRootSpacePosition(), snaps);
                    controlPoint.setRootSpacePosition(snapPos);
                } else {
                    ControlPoint headControlPoint = polyline.getControlPoint(headIndex);
                    Set<Point2D> headSnaps = Collections.singleton(headSnapPoint);
                    Point2D headSnapPos = editor.snap(headControlPoint.getRootSpacePosition(), headSnaps);
                    headControlPoint.setRootSpacePosition(headSnapPos);

                    ControlPoint tailControlPoint = polyline.getControlPoint(tailIndex);
                    Set<Point2D> tailSnaps = Collections.singleton(tailSnapPoint);
                    Point2D tailSnapPos = editor.snap(tailControlPoint.getRootSpacePosition(), tailSnaps);
                    tailControlPoint.setRootSpacePosition(tailSnapPos);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent e) {
        if ((firstPoint != null) && (e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            resetState(e.getEditor());
            e.getEditor().repaint();
            return true;
        }
        if (!controlPoints.isEmpty() && ((e.getKeyCode() == KeyEvent.VK_DELETE) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE))) {
            controlPoints.removeLast();
            e.getEditor().repaint();
            return true;
        }
        return super.keyPressed(e);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return (firstNode == null) ? getFirstHintMessage() : getSecondHintMessage();
    }

    public String getFirstHintMessage() {
        return "Click on first component.";
    }

    public String getSecondHintMessage() {
        return "Click on second component or create polyline. " +
                "Hold " + DesktopApi.getMenuKeyName() + " to connect continuously.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            if (node == currentNode) {
                return Decoration.Highlighted.INSTANCE;
            }
            if (!allLevels) {
                VisualModel model = editor.getModel();
                if (node == model.getCurrentLevel()) {
                    return Decoration.Empty.INSTANCE;
                }
                if (node == model.getRoot()) {
                    return Decoration.Shaded.INSTANCE;
                }
            }
            return null;
        };
    }

}
