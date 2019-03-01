package org.workcraft.plugins.policy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.utils.ColorUtils;
import org.workcraft.types.Pair;

@DisplayName ("Bundle")
public class VisualBundle extends VisualNode implements Drawable, Dependent {
    public static final String PROPERTY_COLOR = "color";

    private final Bundle bundle;
    protected double strokeWidth = CommonVisualSettings.getStrokeWidth();
    private Color color = ColorUtils.getLabColor(0.7f, (float) Math.random(), (float) Math.random());
    private Collection<Line2D> spanningTree = null;

    public VisualBundle(Bundle bundle) {
        super();
        this.bundle = bundle;
    }

    public Bundle getReferencedBundle() {
        return bundle;
    }

    @Override
    public Collection<MathNode> getMathReferences() {
        ArrayList<MathNode> result = new ArrayList<>();
        result.add(getReferencedBundle());
        return result;
    }

    @Override
    public boolean hitTest(Point2D point) {
        return false;
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        Path2D shape = new Path2D.Double();
        float w = (float) strokeWidth / 4.0f;

        if (spanningTree == null) {
            HashSet<Point2D> points = new HashSet<>();
            Collection<VisualBundledTransition> transitions = ((VisualPolicyNet) r.getModel()).getTransitionsOfBundle(this);
            for (VisualBundledTransition t: transitions) {
                Point2D point = TransformHelper.getTransformToRoot(t).transform(t.getCenter(), null);
                points.add(point);
            }
            spanningTree = buildSpanningTree(points);
        }
        for (Line2D l: spanningTree) {
            shape.moveTo(l.getX1(), l.getY1());
            shape.lineTo(l.getX2(), l.getY2());
        }
        g.setColor(Coloriser.colorise(color, d.getColorisation()));
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{10 * w, 10 * w}, 0.0f));
        g.draw(shape);
    }

    public void invalidateSpanningTree() {
        spanningTree = null;
    }

    private Collection<Line2D> buildSpanningTree(Collection<Point2D> points) {
        HashSet<Line2D> result = new HashSet<>();
        HashMap<Pair<Point2D, Point2D>, Double> weights = new HashMap<>();
        for (Point2D p1: points) {
            for (Point2D p2: points) {
                if (p1 != p2) {
                    Double weight = p1.distanceSq(p2);
                    weights.put(Pair.of(p1, p2), weight);
                }
            }
        }
        HashSet<Point2D> connectedPoints = new HashSet<>();
        while (!points.isEmpty()) {
            Point2D bestPoint = null;
            if (connectedPoints.isEmpty()) {
                bestPoint = points.iterator().next();
            } else {
                Line2D bestLink = null;
                Double bestWeight = 0.0;
                for (Point2D p1: connectedPoints) {
                    for (Point2D p2: points) {
                        Double weight = weights.get(Pair.of(p1, p2));
                        if (bestLink == null || weight < bestWeight) {
                            bestWeight = weight;
                            bestLink = new Line2D.Double(p1, p2);
                            bestPoint = p2;
                        }
                    }
                }
                result.add(bestLink);
            }
            points.remove(bestPoint);
            connectedPoints.add(bestPoint);
        }
        return result;
    }

    public void setColor(Color value) {
        if (!color.equals(value)) {
            color = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_COLOR));
        }
    }

    public Color getColor() {
        return color;
    }

}
