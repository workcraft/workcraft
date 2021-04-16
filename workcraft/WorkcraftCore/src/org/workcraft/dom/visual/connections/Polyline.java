package org.workcraft.dom.visual.connections;

import org.workcraft.dom.ArbitraryInsertionGroupImpl;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.observation.*;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.Geometry;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class Polyline implements ConnectionGraphic, Container, StateObserver,
        HierarchyObserver, ObservableHierarchy, SelectionObserver {

    private final ArbitraryInsertionGroupImpl groupImpl;
    protected final VisualConnectionProperties connectionInfo;
    protected PartialCurveInfo curveInfo = null;
    private Rectangle2D boundingBox = null;
    private ControlPointScaler scaler = null;

    public Polyline(VisualConnection parent) {
        groupImpl = new ArbitraryInsertionGroupImpl(this);
        groupImpl.setParent(parent);
        groupImpl.addObserver(this);
        connectionInfo = parent;
    }

    @Override
    public void setDefaultControlPoints() {
        resetControlPoints();
        if (connectionInfo.isSelfLoop()) {
            Point2D p = connectionInfo.getFirstCenter();

            ControlPoint cp1 = new ControlPoint();
            cp1.setPosition(new Point2D.Double(p.getX() - 1.0, p.getY() + 1.5));
            addControlPoint(cp1);

            ControlPoint cp2 = new ControlPoint();
            cp2.setPosition(new Point2D.Double(p.getX() + 1.0, p.getY() + 1.5));
            addControlPoint(cp2);
        }
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        PartialCurveInfo curveInfo = getCurveInfo();

        int start = getSegmentIndex(curveInfo.tStart);
        Point2D startPt = getPointOnCurve(curveInfo.tStart);

        int end = getSegmentIndex(curveInfo.tEnd);
        Point2D endPt = getPointOnCurve(curveInfo.tEnd);

        Path2D connectionPath = new Path2D.Double();
        connectionPath.moveTo(startPt.getX(), startPt.getY());
        for (int i = start; i < end; i++) {
            Line2D segment = getSegment(i);
            connectionPath.lineTo(segment.getX2(), segment.getY2());
        }
        connectionPath.lineTo(endPt.getX(), endPt.getY());

        Color color = ColorUtils.colorise(connectionInfo.getDrawColor(), r.getDecoration().getColorisation());
        g.setColor(color);
        g.setStroke(connectionInfo.getStroke());
        g.draw(connectionPath);

        if (connectionInfo.hasArrow()) {
            DrawHelper.drawArrowHead(g, curveInfo.headPosition, curveInfo.headOrientation,
                    connectionInfo.getArrowLength(), connectionInfo.getArrowWidth(), color);
        }

        if (connectionInfo.hasBubble()) {
            DrawHelper.drawBubbleHead(g, curveInfo.headPosition, curveInfo.headOrientation,
                    connectionInfo.getBubbleSize(), color, connectionInfo.getStroke());
        }
    }

    @Override
    public Rectangle2D getBoundingBox() {
        if (boundingBox == null) {
            for (int i = 0; i < getSegmentCount(); i++) {
                Line2D segment = getSegment(i);
                if (i == 0) {
                    boundingBox = getSegmentBoundsWithThreshold(segment);
                } else {
                    boundingBox.add(getSegmentBoundsWithThreshold(segment));
                }
            }
        }
        return boundingBox;
    }

    @Override
    public Set<Point2D> getIntersections(Rectangle2D rect) {
        Set<Point2D> result = new HashSet<>();
        for (int i = 0; i < getSegmentCount(); i++) {
            Line2D segment = getSegment(i);
            result.addAll(Geometry.getSegmentFrameIntersections(segment, rect));
        }
        return result;
    }

    @Override
    public PartialCurveInfo getCurveInfo() {
        if (curveInfo == null) {
            curveInfo = Geometry.buildConnectionCurveInfo(connectionInfo, this, 0);
        }
        return curveInfo;
    }

    protected int getSegmentIndex(double t) {
        int segments = getSegmentCount();
        double l = 1.0 / segments;
        double tl = t / l;

        int n = (int) Math.floor(tl);
        if (n == segments) n -= 1;
        return n;
    }

    private double getParameterOnSegment(double t, int segmentIndex) {
        return t * getSegmentCount() - segmentIndex;
    }

    public int getNearestSegment(Point2D pt, Point2D outPointOnSegment) {
        double min = Double.MAX_VALUE;
        int nearest = -1;

        for (int i = 0; i < getSegmentCount(); i++) {
            Line2D segment = getSegment(i);
            Point2D a = new Point2D.Double(pt.getX() - segment.getX1(), pt.getY() - segment.getY1());
            Point2D b = new Point2D.Double(segment.getX2() - segment.getX1(), segment.getY2() - segment.getY1());

            double magB = b.distance(0, 0);
            double dist;
            if (magB < 0.0000001) {
                dist = pt.distance(segment.getP1());
            } else {
                b.setLocation(b.getX() / magB, b.getY() / magB);
                double magAonB = a.getX() * b.getX() + a.getY() * b.getY();
                if (magAonB < 0) {
                    magAonB = 0;
                }
                if (magAonB > magB) {
                    magAonB = magB;
                }
                a.setLocation(segment.getX1() + b.getX() * magAonB, segment.getY1() + b.getY() * magAonB);
                dist = new Point2D.Double(pt.getX() - a.getX(), pt.getY() - a.getY()).distance(0, 0);
            }

            if (dist < min) {
                min = dist;
                if (outPointOnSegment != null) {
                    outPointOnSegment.setLocation(a);
                }
                nearest = i;
            }
        }
        return nearest;
    }

    public int getControlPointCount() {
        return getChildren().size();
    }

    protected int getSegmentCount() {
        return getControlPointCount() + 1;
    }

    protected int getAnchorPointCount() {
        return getControlPointCount() + 2;
    }

    private Point2D getAnchorPointLocation(int index) {
        if (index <= 0) {
            return connectionInfo.getFirstCenter();
        }
        if (index >= getAnchorPointCount() - 1) {
            return connectionInfo.getSecondCenter();
        }
        return getControlPoint(index - 1).getPosition();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ControlPoint> getControlPoints() {
        return Collections.unmodifiableList((List) getChildren());
    }

    public ControlPoint getControlPoint(int index) {
        ControlPoint result = null;
        if ((index >= 0) && (index < getControlPointCount())) {
            result = getControlPoints().get(index);
        }
        return result;
    }

    protected Line2D getSegment(int index) {
        if (index < getSegmentCount()) {
            return new Line2D.Double(getAnchorPointLocation(index), getAnchorPointLocation(index + 1));
        } else {
            throw new RuntimeException("Segment index is greater than number of segments");
        }
    }

    private Rectangle2D getSegmentBoundsWithThreshold(Line2D segment) {
        Point2D pt1 = segment.getP1();
        Point2D pt2 = segment.getP2();

        Rectangle2D bb = new Rectangle2D.Double(pt1.getX(), pt1.getY(), 0, 0);
        bb.add(pt2);
        Point2D lineVec = new Point2D.Double(pt2.getX() - pt1.getX(), pt2.getY() - pt1.getY());

        double mag = lineVec.distance(0, 0);
        if (mag != 0) {
            lineVec.setLocation(lineVec.getY() * VisualConnection.HIT_THRESHOLD / mag,
                    -lineVec.getX() * VisualConnection.HIT_THRESHOLD / mag);
            bb.add(pt1.getX() + lineVec.getX(), pt1.getY() + lineVec.getY());
            bb.add(pt2.getX() + lineVec.getX(), pt2.getY() + lineVec.getY());
            bb.add(pt1.getX() - lineVec.getX(), pt1.getY() - lineVec.getY());
            bb.add(pt2.getX() - lineVec.getX(), pt2.getY() - lineVec.getY());
        }
        return bb;
    }

    public void resetControlPoints() {
        ArrayList<Node> children = new ArrayList<>(groupImpl.getChildren());
        for (Node node: children) {
            groupImpl.remove(node);
        }
    }

    public ControlPoint insertControlPointInSegment(ControlPoint cp, int segmentIndex) {
        groupImpl.add(segmentIndex, cp);
        controlPointsChanged();
        return cp;
    }

    public ControlPoint insertControlPointInSegment(Point2D location, int segmentIndex) {
        ControlPoint cp = new ControlPoint();
        cp.setPosition(location);
        groupImpl.add(segmentIndex, cp);
        controlPointsChanged();
        return cp;
    }

    public ControlPoint addControlPoint(ControlPoint cp) {
        return insertControlPointInSegment(cp, getSegmentCount() - 1);
    }

    public ControlPoint addControlPoint(Point2D location) {
        return insertControlPointInSegment(location, getSegmentCount() - 1);
    }

    @Override
    public boolean hitTest(Point2D point) {
        return getDistanceToCurve(point) < VisualConnection.HIT_THRESHOLD;
    }

    @Override
    public Collection<Node> getChildren() {
        return groupImpl.getChildren();
    }

    @Override
    public Node getParent() {
        return groupImpl.getParent();
    }

    @Override
    public void setParent(Node parent) {
        throw new RuntimeException("Node does not support reparenting");
    }

    @Override
    public void add(Collection<? extends Node> nodes) {
        groupImpl.add(nodes);
    }

    @Override
    public void add(Node node) {
        groupImpl.add(node);
    }

    @Override
    public void addObserver(HierarchyObserver obs) {
        groupImpl.addObserver(obs);
    }

    @Override
    public void remove(Collection<? extends Node> nodes) {
        groupImpl.remove(nodes);
    }

    @Override
    public void remove(Node node) {
        groupImpl.remove(node);
    }

    @Override
    public void removeObserver(HierarchyObserver obs) {
        groupImpl.removeObserver(obs);
    }

    @Override
    public void removeAllObservers() {
        groupImpl.removeAllObservers();
    }

    @Override
    public void reparent(Collection<? extends Node> nodes, Container newParent) {
        groupImpl.reparent(nodes, newParent);
    }

    @Override
    public void reparent(Collection<? extends Node> nodes) {
        groupImpl.reparent(nodes);
    }

    @Override
    public Point2D getNearestPointOnCurve(Point2D pt) {
        Point2D result = new Point2D.Double();
        getNearestSegment(pt, result);
        return result;
    }

    @Override
    public Point2D getPointOnCurve(double t) {
        int segmentIndex = getSegmentIndex(t);
        double t2 = getParameterOnSegment(t, segmentIndex);
        Line2D segment = getSegment(segmentIndex);
        double x = segment.getP1().getX() * (1 - t2) + segment.getP2().getX() * t2;
        double y = segment.getP1().getY() * (1 - t2) + segment.getP2().getY() * t2;
        return new Point2D.Double(x, y);
    }

    @Override
    public double getDistanceToCurve(Point2D pt) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < getSegmentCount(); i++) {
            Line2D segment = getSegment(i);
            double dist = segment.ptSegDist(pt);
            if (dist < min) {
                min = dist;
            }
        }
        return min;
    }

    @Override
    public void notify(StateEvent e) {
        controlPointsChanged();
    }

    @Override
    public void notify(HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            for (Node n : e.getAffectedNodes()) {
                if (n instanceof ControlPoint) {
                    ControlPoint cp = (ControlPoint) n;
                    cp.removeObserver(this);
                }
            }
        }
        if (e instanceof NodesAddedEvent) {
            for (Node n : e.getAffectedNodes()) {
                if (n instanceof ControlPoint) {
                    ControlPoint cp = (ControlPoint) n;
                    cp.addObserver(this);
                }
            }
        }

        controlPointsChanged();
    }

    @Override
    public void notify(SelectionChangedEvent event) {
        boolean controlsVisible = false;
        for (Node n : event.getSelection()) {
            if (n == getParent() || getChildren().contains(n)) {
                controlsVisible = true;
                break;
            }
        }
        for (Node n : getChildren()) {
            ((ControlPoint) n).setHidden(!controlsVisible);
        }
    }

    @Override
    public void componentsTransformChanged() {
        if (scaler != null) {
            Collection<ControlPoint> controlPoints = Hierarchy.filterNodesByType(getChildren(), ControlPoint.class);
            scaler.scale(connectionInfo.getFirstCenter(), connectionInfo.getSecondCenter(),
                    controlPoints, connectionInfo.getScaleMode());
        }
        scaler = new ControlPointScaler(connectionInfo.getFirstCenter(), connectionInfo.getSecondCenter());
        invalidate();
    }

    @Override
    public void componentsTransformChanging() {
        scaler = new ControlPointScaler(connectionInfo.getFirstCenter(), connectionInfo.getSecondCenter());
    }

    @Override
    public void controlPointsChanged() {
        invalidate();
    }

    @Override
    public void invalidate() {
        boundingBox = null;
        curveInfo = null;
    }

    @Override
    public Point2D getDerivativeAt(double t) {
        if (t < 0) {
            t = 0;
        }
        if (t > 1) {
            t = 1;
        }
        int segmentIndex = getSegmentIndex(t);
        Line2D segment = getSegment(segmentIndex);
        return Geometry.subtract(segment.getP2(), segment.getP1());
    }

    @Override
    public Point2D getSecondDerivativeAt(double t) {
        Point2D left = getDerivativeAt(t - 0.05);
        Point2D right = getDerivativeAt(t + 0.05);

        return Geometry.subtract(right, left);
    }

    @Override
    public Point2D getCenter() {
        return getPointOnCurve(0.5);
    }

    private int getIndex(ControlPoint cp) {
        int index = -1;
        for (Node node: getChildren()) {
            index++;
            if (node == cp) {
                return index;
            }
        }
        return -1;
    }

    public Point2D getPrevAnchorPointLocation(ControlPoint cp) {
        int index = getIndex(cp);
        Point2D pos = null;
        if (index >= 0) {
            pos = getAnchorPointLocation(index);
        }
        return pos;
    }

    public Point2D getNextAnchorPointLocation(ControlPoint cp) {
        int index = getIndex(cp);
        Point2D pos = null;
        if (index >= 0) {
            pos = getAnchorPointLocation(index + 2);
        }
        return pos;
    }

    public ControlPoint getFirstControlPoint() {
        return getControlPoint(0);
    }

    public ControlPoint getLastControlPoint() {
        return getControlPoint(getControlPointCount() - 1);
    }

}
