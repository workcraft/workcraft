package org.workcraft.gui.graph.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.Icon;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.util.GUI;

public class ConnectionTool extends AbstractGraphEditorTool {

    public enum ContinuousConnectionMode { FAN, FOLD }

    protected static final Color incompleteConnectionColor = Color.GREEN;
    protected static final Color validConnectionColor = Color.BLUE;
    protected static final Color invalidConnectionColor = Color.RED;

    protected boolean forbidSelfLoops = true;
    protected boolean directedArcs = true;
    protected boolean useTemplate = true;
    protected boolean allLevels = true;

    private Point2D firstPoint = null;
    private VisualNode firstNode = null;
    private Point2D currentPoint = null;
    protected VisualNode currentNode = null;
    private boolean mouseLeftFirstNode = false;
    private LinkedList<Point2D> controlPoints = null;
    private VisualConnection templateNode = null;

    public ConnectionTool() {
        this(true, true, true, true);
    }

    public ConnectionTool(boolean forbidSelfLoops, boolean directedArcs, boolean useTemplate, boolean allLevels) {
        this.forbidSelfLoops = forbidSelfLoops;
        this.directedArcs = directedArcs;
        this.useTemplate = useTemplate;
        this.allLevels = allLevels;
    }

    @Override
    public Icon getIcon() {
        return GUI.createIconFromSVG("images/tool-connection.svg");
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
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    private void resetState(GraphEditor editor) {
        currentPoint = null;
        currentNode = null;
        firstPoint = null;
        firstNode = null;
        mouseLeftFirstNode = false;
        editor.getModel().selectNone();
        setup(editor);
    }

    protected void updateState(GraphEditor editor) {
        if (allLevels) {
            currentNode = (VisualNode) HitMan.hitTestRootDeepest(currentPoint, editor.getModel());
        } else {
            currentNode = (VisualNode) HitMan.hitTestCurrentLevelFirst(currentPoint, editor.getModel());
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
        if (useTemplate && (templateNode == null)) {
            templateNode = createDefaultTemplateNode();
        }
        editor.getModel().setTemplateNode(templateNode);
    }

    public VisualConnection createDefaultTemplateNode() {
        return new VisualConnection();
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        resetState(editor);
    }

    @Override
    public void reactivated(final GraphEditor editor) {
        templateNode = null;
    }

    @Override
    public void setup(final GraphEditor editor) {
        super.setup(editor);
        editor.getWorkspaceEntry().setCanModify(firstNode == null);
        editor.getWorkspaceEntry().setCanSelect(false);
        editor.getWorkspaceEntry().setCanCopy(false);
    }

    @Override
    public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
        if ((firstNode != null) && (currentPoint != null)) {
            g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));
            Path2D path = new Path2D.Double();
            path.moveTo(firstPoint.getX(), firstPoint.getY());
            if (controlPoints != null) {
                for (Point2D point: controlPoints) {
                    path.lineTo(point.getX(), point.getY());
                }
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
        updateState(e.getEditor());
        e.getEditor().repaint();
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        currentPoint = e.getPosition();
        GraphEditor editor = e.getEditor();
        updateState(editor);
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (currentNode == null) {
                if (firstNode != null) {
                    Set<Point2D> snaps = new HashSet<>();
                    if (controlPoints.isEmpty()) {
                        AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(firstNode);
                        Point2D p = TransformHelper.transform(firstNode, localToRootTransform).getCenter();
                        snaps.add(p);
                    } else {
                        snaps.add(controlPoints.getLast());
                    }
                    Point2D snapPos = editor.snap(currentPoint, snaps);
                    controlPoints.add(snapPos);
                }
            } else {
                if (firstNode == null) {
                    startConnection(e);
                } else if ((firstNode == currentNode) && (forbidSelfLoops || !mouseLeftFirstNode)) {
                    if (forbidSelfLoops) {
                        showIssue(editor, "Self-loops are not allowed.");
                    } else if (!mouseLeftFirstNode) {
                        showIssue(editor, "Move the mouse outside this node before creating a self-loop.");
                    }
                } else if ((firstNode instanceof VisualGroup) || (currentNode instanceof VisualGroup)) {
                    showIssue(editor, "Connection with group element is not allowed.");
                } else {
                    editor.getWorkspaceEntry().saveMemento();
                    finishConnection(e);
                    if ((e.getModifiers() & DesktopApi.getMenuKeyMouseMask()) != 0) {
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
        if (firstNode instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) firstNode;
            AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
            Point2D currentPointInLocalSpace = rootToLocalTransform.transform(currentPoint, null);
            Point2D nearestPointInLocalSpace = connection.getNearestPointOnConnection(currentPointInLocalSpace);
            firstPoint = localToRootTransform.transform(nearestPointInLocalSpace, null);
        } else {
            firstPoint = TransformHelper.transform(firstNode, localToRootTransform).getCenter();
        }
        currentNode = null;
        mouseLeftFirstNode = false;
        controlPoints = new LinkedList<Point2D>();
        GraphEditor editor = e.getEditor();
        hideIssue(editor);
        editor.getWorkspaceEntry().setCanModify(false);
    }

    public VisualConnection finishConnection(GraphEditorMouseEvent e) {
        VisualConnection connection = null;
        try {
            if (firstNode instanceof VisualConnection) {
                VisualConnection vc = (VisualConnection) firstNode;
                AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(vc);
                vc.setSplitPoint(rootToLocalTransform.transform(firstPoint, null));
            }
            if (currentNode instanceof VisualConnection) {
                VisualConnection vc = (VisualConnection) currentNode;
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
                connection.copyStyle(templateNode);
                if (controlPoints.isEmpty() && (firstNode == currentNode)) {
                    // Self-loop without predefined control points.
                    connection.getGraphic().setDefaultControlPoints();
                } else {
                    ConnectionHelper.addControlPoints(connection, controlPoints);
                }
            }
        } catch (InvalidConnectionException exeption) {
            Toolkit.getDefaultToolkit().beep();
        }
        return connection;
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent e) {
        if ((firstPoint != null) && (e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            resetState(e.getEditor());
            e.getEditor().repaint();
            return true;
        }
        return super.keyPressed(e);
    }

    @Override
    public String getHintText() {
        return (firstNode == null) ? getFirstHintMessage() : getSecondHintMessage();
    }

    public String getFirstHintMessage() {
        return "Click on a first component.";
    }

    public String getSecondHintMessage() {
        return "Click on a second component or create a polyline segment. Hold "
                + DesktopApi.getMenuKeyMaskName() + " to connect continuously.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
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
            }
        };
    }

}
