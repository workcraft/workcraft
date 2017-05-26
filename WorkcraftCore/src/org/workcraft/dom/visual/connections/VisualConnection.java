package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.MixUtils;
import org.workcraft.dom.visual.Shapable;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.ObservableHierarchyImpl;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualConnection extends VisualNode implements Node, Drawable, Shapable, Dependent,
        Connection, VisualConnectionProperties, ObservableHierarchy {

    public static final String PROPERTY_CONNECTION_TYPE = "Connection type";
    public static final String PROPERTY_COLOR = "Color";
    public static final String PROPERTY_LINE_WIDTH = "Line width";
    public static final String PROPERTY_ARROW_LENGTH = "Arrow length";
    public static final String PROPERTY_ARROW_WIDTH = "Arrow width";
    public static final String PROPERTY_SCALE_MODE = "Scale mode";

    public enum ConnectionType {
        POLYLINE("Polyline"),
        BEZIER("Bezier");

        private final String name;

        ConnectionType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    public enum ScaleMode {
        NONE("Lock anchors"),
        LOCK_RELATIVELY("Bind to components"),
        SCALE("Proportional"),
        STRETCH("Stretch"),
        ADAPTIVE("Adaptive");

        private final String name;

        ScaleMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final ObservableHierarchyImpl observableHierarchyImpl = new ObservableHierarchyImpl();

    private MathConnection refConnection = null;
    private VisualNode first = null;
    private VisualNode second = null;

    private ConnectionType connectionType = ConnectionType.POLYLINE;
    private ScaleMode scaleMode = ScaleMode.NONE;

    private ConnectionGraphic graphic = null;

    public static final double HIT_THRESHOLD = 0.2;

    private Color color = CommonVisualSettings.getConnectionColor();
    private double lineWidth = CommonVisualSettings.getConnectionLineWidth();
    private double arrowWidth = CommonVisualSettings.getConnectionArrowWidth();
    private double arrowLength = CommonVisualSettings.getConnectionArrowLength();
    private double bubbleSize = CommonVisualSettings.getConnectionBubbleSize();

    private boolean hasArrow = true;
    private boolean hasBubble = false;
    private boolean isTokenColorPropagator = false;
    private Point2D splitPoint = null;

    private final LinkedHashSet<Node> children = new LinkedHashSet<>();
    private ComponentsTransformObserver componentsTransformObserver = null;

    public VisualConnection() {
        this(null, null, null);
    }

    public VisualConnection(MathConnection refConnection) {
        this(refConnection, null, null);
    }

    public VisualConnection(MathConnection refConnection, VisualNode first, VisualNode second) {
        this.refConnection = refConnection;
        if ((first != null) && (second != null)) {
            this.first = first;
            this.second = second;
            this.graphic = new Polyline(this);
        }
        initialise();
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualConnection, Double>(
                this, PROPERTY_LINE_WIDTH, Double.class, true, true, true) {
            @Override
            public void setter(VisualConnection object, Double value) {
                object.setLineWidth(value);
            }
            @Override
            public Double getter(VisualConnection object) {
                return object.getLineWidth();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualConnection, Double>(
                this, PROPERTY_ARROW_WIDTH, Double.class, true, true, true) {
            @Override
            public void setter(VisualConnection object, Double value) {
                object.setArrowWidth(value);
            }
            @Override
            public Double getter(VisualConnection object) {
                return object.getArrowWidth();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualConnection, Double>(
                this, PROPERTY_ARROW_LENGTH, Double.class, true, true, true) {
            @Override
            public void setter(VisualConnection object, Double value) {
                object.setArrowLength(value);
            }
            @Override
            public Double getter(VisualConnection object) {
                return object.getArrowLength();
            }
            @Override
            public Map<Double, String> getChoice() {
                LinkedHashMap<Double, String> result = new LinkedHashMap<>();
                result.put(0.0, "none");
                result.put(0.2, "short");
                result.put(0.4, "medium");
                result.put(0.8, "long");
                return result;
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualConnection, ConnectionType>(
                this, PROPERTY_CONNECTION_TYPE, ConnectionType.class, true, true, false) {
            protected void setter(VisualConnection object, ConnectionType value) {
                object.setConnectionType(value);
                for (ControlPoint cp: object.getGraphic().getControlPoints()) {
                    if (cp != null) {
                        cp.setHidden(false);
                    }
                }
            }
            protected ConnectionType getter(VisualConnection object) {
                return object.getConnectionType();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualConnection, ScaleMode>(
                this, PROPERTY_SCALE_MODE, ScaleMode.class, true, true, true) {
            protected void setter(VisualConnection object, ScaleMode value) {
                object.setScaleMode(value);
            }
            protected ScaleMode getter(VisualConnection object) {
                return object.getScaleMode();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualConnection, Color>(
                this, PROPERTY_COLOR, Color.class, true, true, true) {
            protected void setter(VisualConnection object, Color value) {
                object.setColor(value);
            }
            protected Color getter(VisualConnection object) {
                return object.getColor();
            }
        });

    }

    protected void initialise() {
        children.clear();
        componentsTransformObserver = new ComponentsTransformObserver(this);
        children.add(componentsTransformObserver);
        if (graphic != null) {
            children.add(graphic);
        }
        if (refConnection instanceof ObservableState) {
            ((ObservableState) refConnection).addObserver(new StateObserver() {
                public void notify(StateEvent e) {
                    observableStateImpl.sendNotification(e);
                }
            });
        }
    }

    public void setVisualConnectionDependencies(VisualNode first, VisualNode second,
            ConnectionGraphic graphic, MathConnection refConnection) {
        if (first == null) {
            throw new NullPointerException("first");
        }
        if (second == null) {
            throw new NullPointerException("second");
        }
        if (graphic == null) {
            throw new NullPointerException("graphic");
        }

        this.first = first;
        this.second = second;
        this.refConnection = refConnection;
        this.graphic = graphic;

        if (graphic instanceof Polyline) {
            connectionType = ConnectionType.POLYLINE;
        } else if (graphic instanceof Bezier) {
            connectionType = ConnectionType.BEZIER;
        }
        initialise();
    }

    @NoAutoSerialisation
    public ConnectionType getConnectionType() {
        return connectionType;
    }

    @NoAutoSerialisation
    public void setConnectionType(ConnectionType value) {
        if (connectionType != value) {
            connectionType = value;
            observableHierarchyImpl.sendNotification(new NodesDeletingEvent(this, getGraphic()));
            children.remove(getGraphic());
            observableHierarchyImpl.sendNotification(new NodesDeletedEvent(this, getGraphic()));
            if (connectionType == null) {
            } else if (connectionType == ConnectionType.POLYLINE) {
                graphic = new Polyline(this);
                setScaleMode(ScaleMode.NONE);
            } else if (connectionType == ConnectionType.BEZIER) {
                graphic = new Bezier(this);
                setScaleMode(ScaleMode.LOCK_RELATIVELY);
            }
            if ((first != null) && (second != null)) {
                graphic.setDefaultControlPoints();
            }
            children.add(graphic);
            observableHierarchyImpl.sendNotification(new NodesAddedEvent(this, getGraphic()));
            graphic.invalidate();
            observableStateImpl.sendNotification(new PropertyChangedEvent(this, PROPERTY_CONNECTION_TYPE));
        }
        sendNotification(new PropertyChangedEvent(this, PROPERTY_CONNECTION_TYPE));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_COLOR));
    }

    @Override
    public Color getDrawColor() {
        return getColor();
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(double lineWidth) {
        if (lineWidth < 0.01) {
            lineWidth = 0.01;
        }
        if (lineWidth > 0.5) {
            lineWidth = 0.5;
        }
        this.lineWidth = lineWidth;

        invalidate();
        sendNotification(new PropertyChangedEvent(this, PROPERTY_LINE_WIDTH));
    }

    @Override
    public Stroke getStroke() {
        return new BasicStroke((float) getLineWidth());
    }

    @Override
    public boolean hasArrow() {
        return hasArrow;
    }

    public void setArrow(boolean value) {
        hasArrow = value;
    }

    @Override
    public double getArrowWidth() {
        return arrowWidth;
    }

    public void setArrowWidth(double value) {
        if (value > 1)    value = 1;
        if (value < 0.0) value = 0.0;
        this.arrowWidth = value;
        invalidate();
        sendNotification(new PropertyChangedEvent(this, PROPERTY_ARROW_WIDTH));

    }

    @Override
    public double getArrowLength() {
        if (!hasArrow()) return 0.0;
        return arrowLength;
    }

    public void setArrowLength(double value) {
        if (value > 1) value = 1;
        if (value < 0.0) value = 0.0;
        this.arrowLength = value;
        invalidate();
        sendNotification(new PropertyChangedEvent(this, PROPERTY_ARROW_LENGTH));
    }

    public void invalidate() {
        if (graphic != null) {
            graphic.invalidate();
        }
    }

    @Override
    public boolean hasBubble() {
        return hasBubble;
    }

    public void setBubble(boolean value) {
        hasBubble = value;
    }

    @Override
    public double getBubbleSize() {
        if (!hasArrow()) return 0.0;
        return bubbleSize;
    }

    public void setBubbleSize(double value) {
        if (value > 1)value = 1;
        if (value < 0.1) value = 0.1;
        this.bubbleSize = value;
        invalidate();
    }

    @Override
    public boolean isTokenColorPropagator() {
        return isTokenColorPropagator;
    }

    public void setTokenColorPropagator(boolean value) {
        isTokenColorPropagator = value;
    }

    @NoAutoSerialisation
    public void setSplitPoint(Point2D point) {
        splitPoint = point;
    }

    @NoAutoSerialisation
    public Point2D getSplitPoint() {
        return (splitPoint == null) ? getMiddleSegmentCenterPoint() : splitPoint;
    }

    public Point2D getMiddleSegmentCenterPoint() {
        double k = 0.5;
        ConnectionGraphic graphic = getGraphic();
        boolean isSingleSegmentPolyline = (graphic instanceof Polyline)
                && (((Polyline) graphic).getSegmentCount() == 1);
        if ((graphic instanceof Bezier) || isSingleSegmentPolyline) {
            PartialCurveInfo curveInfo = graphic.getCurveInfo();
            k = 0.5 * (curveInfo.tStart + curveInfo.tEnd);
        }
        return getPointOnConnection(k);
    }

    public Point2D getPointOnConnection(double t) {
        return graphic.getPointOnCurve(t);
    }

    public Point2D getNearestPointOnConnection(Point2D pt) {
        return graphic.getNearestPointOnCurve(pt);
    }

    @Override
    public void setParent(Node parent) {
        super.setParent(parent);
        invalidate();
    }

    @Override
    public void draw(DrawRequest r) {

    }

    public MathConnection getReferencedConnection() {
        return refConnection;
    }

    public boolean hitTest(Point2D pointInParentSpace) {
        return graphic.hitTest(pointInParentSpace);
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return graphic.getBoundingBox();
    }

    public VisualNode getFirst() {
        return first;
    }

    public VisualNode getSecond() {
        return second;
    }

    public Set<MathNode> getMathReferences() {
        Set<MathNode> ret = new HashSet<>();
        ret.add(getReferencedConnection());
        return ret;
    }

    public ConnectionGraphic getGraphic() {
        return graphic;
    }

    @Override
    public Collection<Node> getChildren() {
        return children;
    }

    public void addObserver(HierarchyObserver obs) {
        observableHierarchyImpl.addObserver(obs);
    }

    public void removeObserver(HierarchyObserver obs) {
        observableHierarchyImpl.removeObserver(obs);
    }

    public void removeAllObservers() {
        observableHierarchyImpl.removeAllObservers();
    }

    @Override
    public Point2D getFirstCenter() {
        return componentsTransformObserver.getFirstCenter();
    }

    @Override
    public Touchable getFirstShape() {
        return componentsTransformObserver.getFirstShape();
    }

    @Override
    public Point2D getSecondCenter() {
        return componentsTransformObserver.getSecondCenter();
    }

    @Override
    public Touchable getSecondShape() {
        return componentsTransformObserver.getSecondShape();
    }

    @Override
    public ScaleMode getScaleMode() {
        return scaleMode;
    }

    @Override
    public Point2D getCenter() {
        return graphic.getCenter();
    }

    public void setScaleMode(ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_SCALE_MODE));
    }

    public void inverseShape() {
        if (getGraphic() instanceof Polyline) {
            Polyline polyline = (Polyline) getGraphic();
            LinkedList<ControlPoint> controlPoints = new LinkedList<>(polyline.getControlPoints());
            Collections.reverse(controlPoints);
            polyline.resetControlPoints();
            for (ControlPoint cp: controlPoints) {
                polyline.addControlPoint(cp);
            }
        } else if (getGraphic() instanceof Bezier) {
            Bezier bezier = (Bezier) getGraphic();
            BezierControlPoint[] controlPoints = bezier.getBezierControlPoints();
            Point2D tmpPoint = controlPoints[0].getPosition();
            controlPoints[0].setPosition(controlPoints[1].getPosition());
            controlPoints[1].setPosition(tmpPoint);
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualConnection) {
            VisualConnection srcConnection = (VisualConnection) src;
            setConnectionType(srcConnection.getConnectionType());
            setColor(srcConnection.getColor());
            setLineWidth(srcConnection.getLineWidth());
            setScaleMode(srcConnection.getScaleMode());
            setArrow(srcConnection.hasArrow());
            setArrowLength(srcConnection.getArrowLength());
            setArrowWidth(srcConnection.getArrowWidth());
            setBubble(srcConnection.hasBubble());
            setBubbleSize(srcConnection.getBubbleSize());
        }
    }

    @Override
    public void mixStyle(Stylable... srcs) {
        super.mixStyle(srcs);
        LinkedList<ConnectionType> connectionTypes = new LinkedList<>();
        LinkedList<Color> colors = new LinkedList<>();
        LinkedList<Double> lineWidths = new LinkedList<>();
        LinkedList<ScaleMode> scaleModes = new LinkedList<>();
        boolean dstHasArrow = false;
        LinkedList<Double> arrowLengths = new LinkedList<>();
        LinkedList<Double> arrowWidths = new LinkedList<>();
        boolean dstHasBubble = false;
        LinkedList<Double> bubbleSizes = new LinkedList<>();
        for (Stylable src: srcs) {
            if (src instanceof VisualConnection) {
                VisualConnection srcConnection = (VisualConnection) src;
                connectionTypes.add(srcConnection.getConnectionType());
                colors.add(srcConnection.getColor());
                lineWidths.add(srcConnection.getLineWidth());
                scaleModes.add(srcConnection.getScaleMode());
                dstHasArrow |= srcConnection.hasArrow();
                arrowLengths.add(srcConnection.getArrowLength());
                arrowWidths.add(srcConnection.getArrowWidth());
                bubbleSizes.add(srcConnection.getBubbleSize());
                dstHasBubble |= srcConnection.hasBubble();
            }
        }
        setConnectionType(MixUtils.vote(connectionTypes, ConnectionType.class, ConnectionType.POLYLINE));
        setColor(Coloriser.mix(colors));
        setLineWidth(MixUtils.average(lineWidths));
        setArrow(dstHasArrow);
        setArrowLength(MixUtils.average(arrowLengths));
        setArrowWidth(MixUtils.average(arrowWidths));
        setBubble(dstHasBubble);
        setScaleMode(MixUtils.vote(scaleModes, ScaleMode.class, ScaleMode.ADAPTIVE));
    }

    @Override
    public void copyShape(Shapable src) {
        if (src instanceof VisualConnection) {
            VisualConnection srcConnection = (VisualConnection) src;
            setConnectionType(srcConnection.getConnectionType());
            ConnectionGraphic srcGraphic = srcConnection.getGraphic();

            if (srcGraphic instanceof Polyline) {
                Polyline polyline = (Polyline) getGraphic();
                polyline.resetControlPoints();
                for (ControlPoint srcControlPoint: srcGraphic.getControlPoints()) {
                    polyline.addControlPoint(srcControlPoint.getPosition());
                }
            } else if (srcGraphic instanceof Bezier) {
                Bezier bezier = (Bezier) getGraphic();
                BezierControlPoint[] srcControlPoints = ((Bezier) srcGraphic).getBezierControlPoints();
                BezierControlPoint[] dstControlPoints = bezier.getBezierControlPoints();
                dstControlPoints[0].setPosition(srcControlPoints[0].getPosition());
                dstControlPoints[1].setPosition(srcControlPoints[1].getPosition());
            }
        }
    }

    public void setDefaultArrow() {
        setArrow(true);
        setArrowLength(CommonVisualSettings.getConnectionArrowLength());
        setArrowWidth(CommonVisualSettings.getConnectionArrowWidth());
    }

}
