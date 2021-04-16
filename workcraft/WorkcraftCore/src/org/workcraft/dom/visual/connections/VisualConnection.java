package org.workcraft.dom.visual.connections;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.*;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class VisualConnection extends VisualNode implements Node, Drawable, Shapable, Dependent,
        Connection, VisualConnectionProperties, ObservableHierarchy {

    public static final String PROPERTY_CONNECTION_TYPE = "Connection type";
    public static final String PROPERTY_COLOR = "Color";
    public static final String PROPERTY_LINE_WIDTH = "Line width";
    public static final String PROPERTY_ARROW_LENGTH = "Arrow length";
    public static final String PROPERTY_ARROW_WIDTH = "Arrow width";
    public static final String PROPERTY_SCALE_MODE = "Scale mode";
    public static final Map<Double, String> PREDEFINED_ARROW_LENGTHS = new LinkedHashMap<>();

    static {
        PREDEFINED_ARROW_LENGTHS.put(0.0, "none");
        PREDEFINED_ARROW_LENGTHS.put(0.2, "short");
        PREDEFINED_ARROW_LENGTHS.put(0.4, "medium");
        PREDEFINED_ARROW_LENGTHS.put(0.8, "long");
    }

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
    }

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

    private Color color = VisualCommonSettings.getConnectionColor();
    private double lineWidth = VisualCommonSettings.getConnectionLineWidth();
    private double arrowWidth = VisualCommonSettings.getConnectionArrowWidth();
    private double arrowLength = VisualCommonSettings.getConnectionArrowLength();
    private double bubbleSize = VisualCommonSettings.getConnectionBubbleSize();

    private boolean withArrow = true;
    private boolean withBubble = false;
    private boolean tokenColorPropagator = true;
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
        addPropertyDeclaration(new PropertyDeclaration<>(Double.class, PROPERTY_LINE_WIDTH,
                this::setLineWidth, this::getLineWidth).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Double.class, PROPERTY_ARROW_WIDTH,
                this::setArrowWidth, this::getArrowWidth).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<Double>(Double.class, PROPERTY_ARROW_LENGTH,
                this::setArrowLength, this::getArrowLength) {
            @Override
            public Map<Double, String> getChoice() {
                return PREDEFINED_ARROW_LENGTHS;
            }
        }.setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(ConnectionType.class, PROPERTY_CONNECTION_TYPE,
                value -> {
                    setConnectionType(value);
                    for (ControlPoint cp : getGraphic().getControlPoints()) {
                        if (cp != null) {
                            cp.setHidden(false);
                        }
                    }
                },
                this::getConnectionType).setCombinable());

        addPropertyDeclaration(new PropertyDeclaration<>(ScaleMode.class, PROPERTY_SCALE_MODE,
                this::setScaleMode, this::getScaleMode).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, PROPERTY_COLOR,
                this::setColor, this::getColor).setCombinable().setTemplatable());
    }

    protected void initialise() {
        children.clear();
        componentsTransformObserver = new ComponentsTransformObserver(this);
        children.add(componentsTransformObserver);
        if (graphic != null) {
            children.add(graphic);
        }
        if (refConnection instanceof ObservableState) {
            ((ObservableState) refConnection).addObserver(e -> observableStateImpl.sendNotification(e));
        }
    }

    public void setVisualConnectionDependencies(VisualNode first, VisualNode second,
            ConnectionGraphic graphic, MathConnection refConnection) {

        if (first == null) {
            throw new IllegalArgumentException("first is null");
        }
        if (second == null) {
            throw new IllegalArgumentException("second is null");
        }
        if (graphic == null) {
            throw new IllegalArgumentException("graphic is null");
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
            if (connectionType == ConnectionType.POLYLINE) {
                graphic = new Polyline(this);
                setScaleMode(ScaleMode.NONE);
            }
            if (connectionType == ConnectionType.BEZIER) {
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
            sendNotification(new PropertyChangedEvent(this, PROPERTY_CONNECTION_TYPE));
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color value) {
        if (!color.equals(value)) {
            color = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_COLOR));
        }
    }

    @Override
    public Color getDrawColor() {
        return getColor();
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(double value) {
        if (value < 0.01) value = 0.01;
        if (value > 0.5) value = 0.5;
        if (lineWidth != value) {
            lineWidth = value;
            invalidate();
            sendNotification(new PropertyChangedEvent(this, PROPERTY_LINE_WIDTH));
        }
    }

    @Override
    public Stroke getStroke() {
        return new BasicStroke((float) getLineWidth());
    }

    @Override
    public boolean hasArrow() {
        return withArrow;
    }

    public void setArrow(boolean value) {
        withArrow = value;
    }

    @Override
    public double getArrowWidth() {
        return arrowWidth;
    }

    public void setArrowWidth(double value) {
        if (value > 1.0) value = 1.0;
        if (value < 0.0) value = 0.0;
        if (arrowLength != value) {
            arrowWidth = value;
            invalidate();
            sendNotification(new PropertyChangedEvent(this, PROPERTY_ARROW_WIDTH));
        }

    }

    @Override
    public double getArrowLength() {
        if (!hasArrow()) return 0.0;
        return arrowLength;
    }

    public void setArrowLength(double value) {
        if (value > 1.0) value = 1.0;
        if (value < 0.0) value = 0.0;
        if (arrowLength != value) {
            arrowLength = value;
            invalidate();
            sendNotification(new PropertyChangedEvent(this, PROPERTY_ARROW_LENGTH));
        }
    }

    public void invalidate() {
        if (graphic != null) {
            graphic.invalidate();
        }
    }

    @Override
    public boolean hasBubble() {
        return withBubble;
    }

    public void setBubble(boolean value) {
        withBubble = value;
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
        return tokenColorPropagator;
    }

    public void setTokenColorPropagator(boolean value) {
        tokenColorPropagator = value;
    }

    @Override
    public boolean isSelfLoop() {
        return (first != null) && (first == second);
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

    @Override
    public boolean hitTest(Point2D pointInParentSpace) {
        return graphic.hitTest(pointInParentSpace);
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return graphic.getBoundingBox();
    }

    @Override
    public VisualNode getFirst() {
        return first;
    }

    @Override
    public VisualNode getSecond() {
        return second;
    }

    @Override
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

    @Override
    public void addObserver(HierarchyObserver obs) {
        observableHierarchyImpl.addObserver(obs);
    }

    @Override
    public void removeObserver(HierarchyObserver obs) {
        observableHierarchyImpl.removeObserver(obs);
    }

    @Override
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

    public void setScaleMode(ScaleMode value) {
        if (scaleMode != value) {
            scaleMode = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_SCALE_MODE));
        }
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
                dstHasBubble |= srcConnection.hasBubble();
                bubbleSizes.add(srcConnection.getBubbleSize());
            }
        }
        setConnectionType(MixUtils.vote(connectionTypes, ConnectionType.POLYLINE));
        setColor(ColorUtils.mix(colors));
        setLineWidth(MixUtils.average(lineWidths));
        setArrow(dstHasArrow);
        setArrowLength(MixUtils.average(arrowLengths));
        setArrowWidth(MixUtils.average(arrowWidths));
        setBubble(dstHasBubble);
        setBubbleSize(MixUtils.average(bubbleSizes));
        setScaleMode(MixUtils.vote(scaleModes, ScaleMode.ADAPTIVE));
    }

    @Override
    public void copyShape(Shapable src) {
        if (src instanceof VisualConnection) {
            VisualConnection srcConnection = (VisualConnection) src;
            ConnectionUtils.copyShape(srcConnection, this);
        }
    }

    public Set<Point2D> getIntersections(Rectangle2D rect) {
        return graphic.getIntersections(rect);
    }

}
