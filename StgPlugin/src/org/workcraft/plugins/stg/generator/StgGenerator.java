package org.workcraft.plugins.stg.generator;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgSettings;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Hierarchy;

public abstract class StgGenerator {

    public enum SignalLayoutType { LEFT_TO_RIGHT, RIGHT_TO_LEFT, LEFT_TO_RIGHT_INVERTED, RIGHT_TO_LEFT_INVERTED }

    private final VisualModel src;
    private final VisualStg stg;

    public StgGenerator(VisualModel src) {
        this.src = src;
        this.stg = new VisualStg(new Stg());
        convert();
    }

    public VisualModel getSrcModel() {
        return src;
    }

    public VisualStg getStgModel() {
        return stg;
    }

    public abstract void convert();

    public void setPosition(Movable node, double x, double y) {
        TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
    }

    public VisualConnection createConsumingArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
        VisualConnection connection = null;
        if (p != null && t != null) {
            connection = stg.connect(p, t);
        }
        return connection;
    }

    public VisualConnection createConsumingArc(VisualPlace p, VisualSignalTransition t, boolean propagateTokenColor) throws InvalidConnectionException {
        VisualConnection connection = null;
        if (p != null && t != null) {
            connection = stg.connect(p, t);
            connection.setTokenColorPropagator(propagateTokenColor);
        }
        return connection;
    }

    public VisualConnection createProducingArc(VisualSignalTransition t, VisualPlace p) throws InvalidConnectionException {
        VisualConnection connection = null;
        if (p != null && t != null) {
            connection = stg.connect(t, p);
        }
        return connection;
    }

    public VisualConnection createProducingArc(VisualSignalTransition t, VisualPlace p, boolean propagateTokenColor) throws InvalidConnectionException {
        VisualConnection connection = null;
        if (p != null && t != null) {
            connection = stg.connect(t, p);
            connection.setTokenColorPropagator(propagateTokenColor);
        }
        return connection;
    }

    public VisualConnection createReadArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
        VisualConnection connection = null;
        if (p != null && t != null) {
            connection = stg.connectUndirected(p, t);
        }
        return connection;
    }

    public VisualConnection createReadArc(VisualPlace p, VisualSignalTransition t, boolean propagateTokenColor) throws InvalidConnectionException {
        VisualConnection connection = null;
        if (p != null && t != null) {
            connection = stg.connectUndirected(p, t);
            connection.setTokenColorPropagator(propagateTokenColor);
        }
        return connection;
    }

    public void createReadArcs(VisualPlace p, Collection<VisualSignalTransition> ts) throws InvalidConnectionException {
        if (ts != null) {
            for (VisualSignalTransition t: ts) {
                stg.connectUndirected(p, t);
            }
        }
    }

    public void createReadArcs(VisualPlace p, Collection<VisualSignalTransition> ts, boolean propagateTokenColor) throws InvalidConnectionException {
        for (VisualSignalTransition t : new HashSet<VisualSignalTransition>(ts)) {
            createReadArc(p, t, propagateTokenColor);
        }
    }

    public void createReadArcsBetweenSignals(SignalStg from, SignalStg to) throws InvalidConnectionException {
        if ((from != null) && (to != null)) {
            createReadArcs(from.one, to.riseList);
            createReadArcs(from.zero, to.fallList);
        }
    }

    public void createReplicaReadArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
        double dx = (p.getRootSpaceX() > t.getRootSpaceX()) ? 6.0 : -6.0;
        createReplicaReadArc(p, t, dx, 0.0);
    }

    public void createReplicaReadArc(VisualPlace p, VisualSignalTransition t, double xOffset, double yOffset) throws InvalidConnectionException {
        Point2D replicaPosition = new Point2D.Double(t.getRootSpaceX() + xOffset, t.getRootSpaceY() + yOffset);
        createReplicaReadArcs(p, Arrays.asList(t), replicaPosition);
    }

    public void createReplicaReadArcs(VisualPlace p, List<VisualSignalTransition> ts, double xOffset, double yOffset) throws InvalidConnectionException {
        if ((p != null) && (ts != null)) {
            VisualReplicaPlace replicaPlace = null;
            for (VisualSignalTransition t: ts) {
                if (replicaPlace == null) {
                    Container container = Hierarchy.getNearestContainer(new HashSet<Node>(ts));
                    replicaPlace = stg.createVisualReplica(p, container, VisualReplicaPlace.class);
                    Point2D pos = new Point2D.Double(t.getRootSpaceX() + xOffset, t.getRootSpaceY() + yOffset);
                    replicaPlace.setRootSpacePosition(pos);
                }
                stg.connectUndirected(replicaPlace, t);
            }
        }
    }

    public void createReplicaReadArcs(VisualPlace p, Collection<VisualSignalTransition> ts, Point2D replicaPosition) throws InvalidConnectionException {
        if ((p != null) && (ts != null)) {
            Container container = Hierarchy.getNearestContainer(new HashSet<Node>(ts));
            VisualReplicaPlace replicaPlace = stg.createVisualReplica(p, container, VisualReplicaPlace.class);
            if (replicaPosition != null) {
                replicaPlace.setRootSpacePosition(replicaPosition);
            }
            for (VisualSignalTransition t: ts) {
                stg.connectUndirected(replicaPlace, t);
            }
        }
    }

    public void createReplicaReadArcBetweenSignals(SignalStg from, SignalStg to) throws InvalidConnectionException {
        double xT = to.fallList.get(0).getRootSpaceX();
        double xP = to.one.getRootSpaceX();
        double yZero = to.zero.getRootSpaceY();
        double yOne = to.one.getRootSpaceY();
        double x = (xT > xP) ? xT + 6.0 : xT - 6.0;
        createReplicaReadArcs(from.zero, to.fallList, new Point2D.Double(x, yZero));
        createReplicaReadArcs(from.one, to.riseList, new Point2D.Double(x, yOne));
    }

    public Point2D getSignalCenterPosition(SignalStg signal) {
        double x = 0.0;
        for (VisualSignalTransition t: signal.getAllTransitions()) {
            x += t.getRootSpaceX();
        }
        x /= signal.getAllTransitions().size();

        double y = 0.0;
        for (VisualPlace p: signal.getAllPlaces()) {
            y += p.getRootSpaceY();
        }
        y /= signal.getAllPlaces().size();

        return new Point2D.Double(x, y);
    }

    public SignalStg generateBasicSignalStg(String signalName, double x, double y, SignalTransition.Type type) throws InvalidConnectionException {
        VisualPlace zero = stg.createPlace(SignalStg.getLowName(signalName), null);
        zero.getReferencedPlace().setTokens(1);
        zero.setNamePositioning(Positioning.BOTTOM);
        zero.setLabelPositioning(Positioning.TOP);
        setPosition(zero, x + 0.0, y + 2.0);

        VisualPlace one = stg.createPlace(SignalStg.getHighName(signalName), null);
        one.getReferencedPlace().setTokens(0);
        one.setNamePositioning(Positioning.TOP);
        one.setLabelPositioning(Positioning.BOTTOM);
        setPosition(one, x + 0.0, y - 2.0);

        VisualSignalTransition fall = stg.createSignalTransition(signalName, type, SignalTransition.Direction.MINUS, null);
        createConsumingArc(one, fall);
        createProducingArc(fall, zero);
        setPosition(fall, x + 4.0, y + 0.0);

        VisualSignalTransition rise = stg.createSignalTransition(signalName, type, SignalTransition.Direction.PLUS, null);
        createConsumingArc(zero, rise);
        createProducingArc(rise, one);
        setPosition(rise, x - 4.0, y - 0.0);

        return new SignalStg(zero, one, fall, rise);
    }

    public SignalStg generateSignalStg(SignalLayoutType layoutType, String signalName, Point2D pos, SignalTransition.Type type, int fallCount, int riseCount) throws InvalidConnectionException {
        double x = pos.getX();
        double y = pos.getY();
        int xSign = 1;
        if ((layoutType == SignalLayoutType.RIGHT_TO_LEFT)    || (layoutType == SignalLayoutType.RIGHT_TO_LEFT_INVERTED)) {
            xSign = -1;
        }
        int ySign = 1;
        if ((layoutType == SignalLayoutType.LEFT_TO_RIGHT_INVERTED) || (layoutType == SignalLayoutType.RIGHT_TO_LEFT_INVERTED)) {
            ySign = -1;
        }

        VisualPlace zero = stg.createPlace(SignalStg.getLowName(signalName), null);
        zero.getReferencedPlace().setTokens(1);
        zero.setNamePositioning((ySign < 0) ? Positioning.BOTTOM : Positioning.TOP);
        zero.setLabelPositioning((ySign < 0) ? Positioning.TOP : Positioning.BOTTOM);
        setPosition(zero, x + xSign * 4.0, y + ySign * 2.0);

        VisualPlace one = stg.createPlace(SignalStg.getHighName(signalName), null);
        one.getReferencedPlace().setTokens(0);
        one.setNamePositioning((ySign < 0) ? Positioning.TOP : Positioning.BOTTOM);
        one.setLabelPositioning((ySign < 0) ? Positioning.BOTTOM : Positioning.TOP);
        setPosition(one, x + xSign * 4.0, y - ySign * 2.0);

        ArrayList<VisualSignalTransition> fallList = new ArrayList<>(fallCount);
        for (int i = fallCount - 1; i >= 0; --i) {
            VisualSignalTransition fall = stg.createSignalTransition(signalName, type, SignalTransition.Direction.MINUS, null);
            createConsumingArc(one, fall);
            createProducingArc(fall, zero);
            setPosition(fall, x + 0.0, y + ySign * (2.0 + i));
            fallList.add(fall);
        }

        ArrayList<VisualSignalTransition> riseList = new ArrayList<>(riseCount);
        for (int i = riseCount - 1; i >= 0; --i) {
            VisualSignalTransition rise = stg.createSignalTransition(signalName, type, SignalTransition.Direction.PLUS, null);
            createConsumingArc(zero, rise);
            createProducingArc(rise, one);
            setPosition(rise, x + 0.0, y - ySign * (2.0 + i));
            riseList.add(rise);
        }

        return new SignalStg(zero, one, fallList, riseList);
    }

    public void setSignalInitialState(SignalStg signalStg, boolean initToOne) {
        if (initToOne) {
            signalStg.one.getReferencedPlace().setTokens(1);
            signalStg.zero.getReferencedPlace().setTokens(0);
        } else {
            signalStg.one.getReferencedPlace().setTokens(0);
            signalStg.zero.getReferencedPlace().setTokens(1);
        }
    }

    public Point2D getComponentPosition(VisualComponent component) {
        AffineTransform transform = TransformHelper.getTransformToRoot(component);
        double x = getScale().getX() * (transform.getTranslateX() + component.getX());
        double y = getScale().getY() * (transform.getTranslateY() + component.getY());
        return new Point2D.Double(x, y);
    }

    public Point2D getScale() {
        return new Point2D.Double(10.0, 10.0);
    }

    public void groupComponentStg(NodeStg nodeStg) {
        if (StgSettings.getGroupSignalConversion()) {
            stg.select(nodeStg.getAllNodes());
            stg.groupSelection();
        }
    }

}
