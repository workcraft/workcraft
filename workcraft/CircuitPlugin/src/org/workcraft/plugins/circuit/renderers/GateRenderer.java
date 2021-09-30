package org.workcraft.plugins.circuit.renderers;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.plugins.circuit.naryformula.NaryBooleanFormula;
import org.workcraft.plugins.circuit.naryformula.NaryBooleanFormulaBuilder;
import org.workcraft.plugins.circuit.naryformula.NaryBooleanFormulaVisitor;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class GateRenderer {

    public static final double bubbleSize = 0.3;
    public static final double ANDGateAspectRatio = 0.8125;
    public static final double XORGateAspectRatio = 1.15;
    public static final double contactMargin = 0.5;

    public static Color foregroundColor = Color.BLACK;
    public static Color backgroundColor = Color.WHITE;

    private static class NaryBooleanFormulaRenderer implements NaryBooleanFormulaVisitor<ComponentRenderingResult> {

        private boolean isBuffer = true;

        @Override
        public ComponentRenderingResult visit(final BooleanVariable var) {

            final Rectangle2D box = isBuffer
                    ? new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0)
                    : new Rectangle2D.Double(0.0, -0.25, 0.0, 0.5);

            return new ComponentRenderingResult() {
                @Override
                public Rectangle2D boundingBox() {
                    return box;
                }

                @Override
                public Map<String, List<Point2D>> contactPositions() {
                    Map<String, List<Point2D>> literalToPositions = new HashMap<>();
                    String literal = var.getLabel();
                    if (isBuffer) {
                        literalToPositions.put(literal, Collections.singletonList(new Point2D.Double(-0.5, 0)));
                    } else {
                        literalToPositions.put(literal, Collections.singletonList(new Point2D.Double(0, 0)));
                    }
                    return literalToPositions;
                }

                @Override
                public void draw(Graphics2D g) {
                    if (isBuffer) {
                        Path2D path = new Path2D.Double();
                        path.moveTo(-0.5, -0.5);
                        path.lineTo(-0.5, 0.5);
                        path.lineTo(0.5, 0);
                        path.closePath();

                        g.setColor(backgroundColor);
                        g.fill(path);
                        g.setColor(foregroundColor);
                        g.draw(path);
                    }
                }
            };
        }

        @Override
        public ComponentRenderingResult visitNot(NaryBooleanFormula arg) {
            final ComponentRenderingResult renderingResult = arg.accept(this);
            final Rectangle2D box = renderingResult.boundingBox();
            final Ellipse2D.Double bubbleShape = new Ellipse2D.Double(
                    -bubbleSize / 2, -bubbleSize / 2, bubbleSize, bubbleSize);

            final double w = box.getWidth() + bubbleSize;
            final double h = Math.max(box.getHeight(), 0.5);

            box.setRect(new Rectangle2D.Double(-w / 2, -h / 2, w, h));

            return new ComponentRenderingResult() {
                @Override
                public void draw(Graphics2D g) {
                    g.translate(-bubbleSize / 2, 0);
                    renderingResult.draw(g);
                    g.translate(w / 2, 0);
                    g.setColor(backgroundColor);
                    g.fill(bubbleShape);
                    g.setColor(foregroundColor);
                    g.draw(bubbleShape);
                    g.translate(-w / 2 + bubbleSize / 2, 0);
                }

                @Override
                public Map<String, List<Point2D>> contactPositions() {
                    Map<String, List<Point2D>> literalToPositions = new HashMap<>();
                    for (String literal : renderingResult.contactPositions().keySet()) {
                        List<Point2D> positions = literalToPositions.computeIfAbsent(literal, key -> new ArrayList<>());
                        for (Point2D position : renderingResult.contactPositions().get(literal)) {
                            positions.add(new Point2D.Double(position.getX() - bubbleSize / 2, position.getY()));
                        }
                    }
                    return literalToPositions;
                }

                @Override
                public Rectangle2D boundingBox() {
                    return new Rectangle2D.Double(-w / 2, -h / 2, w, h);
                }
            };
        }

        @Override
        public ComponentRenderingResult visitAnd(List<NaryBooleanFormula> args) {
            isBuffer = false;
            final List<ComponentRenderingResult> renderingResults = new LinkedList<>();
            for (NaryBooleanFormula formula : args) {
                renderingResults.add(formula.accept(this));
            }

            return new ComponentRenderingResult() {
                private Rectangle2D cachedBox = null;
                private Map<String, List<Point2D>> cachedPositions = null;

                @Override
                public Rectangle2D boundingBox() {
                    if (cachedBox == null) {
                        double maxX = 0;
                        double sumY = 0;
                        for (ComponentRenderingResult renderingResult : renderingResults) {
                            Rectangle2D box = renderingResult.boundingBox();
                            if (maxX < box.getWidth()) {
                                maxX = box.getWidth();
                            }
                            sumY += box.getHeight();
                        }
                        maxX += sumY * ANDGateAspectRatio;
                        cachedBox = new Rectangle2D.Double(-maxX / 2, -sumY / 2, maxX, sumY);
                    }
                    return cachedBox;
                }

                @Override
                public Map<String, List<Point2D>> contactPositions() {
                    if (cachedPositions == null) {
                        Map<String, List<Point2D>> literalToPositions = new HashMap<>();
                        double x = boundingBox().getMaxX() - boundingBox().getHeight() * ANDGateAspectRatio;
                        double y = boundingBox().getMinY();
                        for (ComponentRenderingResult renderingResult : renderingResults) {
                            Rectangle2D box = renderingResult.boundingBox();
                            for (String literal : renderingResult.contactPositions().keySet()) {
                                List<Point2D> positions = literalToPositions.computeIfAbsent(literal, key -> new ArrayList<>());
                                for (Point2D position : renderingResult.contactPositions().get(literal)) {
                                    positions.add(new Point2D.Double(
                                            position.getX() + x - box.getWidth() / 2,
                                            position.getY() + y + box.getHeight() / 2));
                                }
                            }
                            y += box.getHeight();
                        }
                        cachedPositions = literalToPositions;
                    }
                    return cachedPositions;
                }

                @Override
                public void draw(Graphics2D g) {
                    double s = boundingBox().getHeight();
                    double x = boundingBox().getMaxX() - s * ANDGateAspectRatio;
                    double y = boundingBox().getMinY();
                    for (ComponentRenderingResult renderingResult : renderingResults) {
                        Rectangle2D box = renderingResult.boundingBox();
                        double w = box.getWidth();
                        double h = box.getHeight();
                        g.translate(x - w / 2, y + h / 2);
                        renderingResult.draw(g);
                        g.translate(-x + w / 2, -y - h / 2);
                        y += h;
                    }

                    y = boundingBox().getMinY();
                    Path2D.Double path = new Path2D.Double();
                    path.moveTo(x, y);
                    path.lineTo(x + s / 4, y);
                    path.curveTo(x + s, y, x + s, y + s, x + s / 4, y + s);
                    path.lineTo(x, y + s);
                    path.closePath();

                    g.setColor(backgroundColor);
                    g.fill(path);
                    g.setColor(foregroundColor);
                    g.draw(path);
                }

            };
        }

        @Override
        public ComponentRenderingResult visitOr(List<NaryBooleanFormula> args) {
            isBuffer = false;
            final List<ComponentRenderingResult> renderingResults = new LinkedList<>();

            for (NaryBooleanFormula formula : args) {
                renderingResults.add(formula.accept(this));
            }

            return new ComponentRenderingResult() {
                private Rectangle2D cachedBox = null;
                private Map<String, List<Point2D>> cachedPositions = null;

                @Override
                public Rectangle2D boundingBox() {
                    if (cachedBox == null) {
                        double maxX = 0;
                        double sumY = 0;
                        for (ComponentRenderingResult renderingResult : renderingResults) {
                            Rectangle2D rec = renderingResult.boundingBox();
                            if (maxX < rec.getWidth()) maxX = rec.getWidth();
                            sumY += rec.getHeight();
                        }
                        cachedBox = new Rectangle2D.Double(-(sumY + maxX) / 2, -sumY / 2, sumY + maxX, sumY);
                    }
                    return cachedBox;
                }

                @Override
                public Map<String, List<Point2D>> contactPositions() {
                    if (cachedPositions == null) {
                        Map<String, List<Point2D>> literalToPositions = new HashMap<>();

                        double s = boundingBox().getHeight();
                        double x = boundingBox().getMaxX() - s;
                        double y1 = boundingBox().getMinY();
                        double y2 = boundingBox().getMaxY();
                        double y = y1;

                        for (ComponentRenderingResult renderingResult : renderingResults) {
                            Rectangle2D rec = renderingResult.boundingBox();
                            for (String literal : renderingResult.contactPositions().keySet()) {
                                List<Point2D> positions = literalToPositions.computeIfAbsent(literal, key -> new ArrayList<>());
                                for (Point2D position : renderingResult.contactPositions().get(literal)) {
                                    double xOffset = 0;
                                    if (renderingResult.boundingBox().getHeight() <= 0.5) {
                                        xOffset = getXFromY((y2 - (y + rec.getHeight() / 2)) / (y2 - y1), s / 3);
                                    }
                                    positions.add(new Point2D.Double(
                                            position.getX() + x - rec.getWidth() / 2 + xOffset,
                                            position.getY() + y + rec.getHeight() / 2));
                                }
                            }
                            y += rec.getHeight();
                        }
                        cachedPositions = literalToPositions;
                    }
                    return cachedPositions;
                }

                @Override
                public void draw(Graphics2D g) {
                    double s = boundingBox().getHeight();
                    double x = boundingBox().getMaxX() - s;
                    double y = boundingBox().getMinY();
                    double y1 = boundingBox().getMinY();
                    double y2 = boundingBox().getMaxY();

                    Path2D.Double path = new Path2D.Double();

                    path.moveTo(x, y);
                    path.curveTo(x + s / 2, y, x + 0.85 * s, y + s / 4, x + s, y + s / 2);
                    path.curveTo(x + 0.85 * s, y + 0.75 * s, x + s / 2, y + s, x, y + s);
                    path.quadTo(x + s / 3, y + s / 2, x, y);
                    path.closePath();

                    g.setColor(backgroundColor);
                    g.fill(path);
                    g.setColor(foregroundColor);
                    g.draw(path);

                    for (ComponentRenderingResult res: renderingResults) {
                        Rectangle2D rec = res.boundingBox();
                        double xOffset = 0;
                        if (rec.getHeight() <= 0.5) {
                            xOffset = getXFromY((y2 - (y + rec.getHeight() / 2)) / (y2 - y1), s / 3);
                        }
                        g.translate(x - rec.getWidth() / 2 + xOffset, y + rec.getHeight() / 2);
                        res.draw(g);
                        g.translate(-x + rec.getWidth() / 2 - xOffset, -y - rec.getHeight() / 2);
                        y += rec.getHeight();
                    }
                }
            };
        }

        @Override
        public ComponentRenderingResult visitXor(List<NaryBooleanFormula> args) {
            isBuffer = false;
            final List<ComponentRenderingResult> renderingResults = new LinkedList<>();

            for (NaryBooleanFormula formula : args) {
                renderingResults.add(formula.accept(this));
            }

            return new ComponentRenderingResult() {
                private Rectangle2D cachedBox = null;
                private Map<String, List<Point2D>> cachedPositions = null;
                @Override
                public Rectangle2D boundingBox() {
                    if (cachedBox == null) {
                        double maxX = 0;
                        double sumY = 0;
                        for (ComponentRenderingResult renderingResult : renderingResults) {
                            Rectangle2D rec = renderingResult.boundingBox();
                            if (maxX < rec.getWidth()) {
                                maxX = rec.getWidth();
                            }
                            sumY += rec.getHeight();
                        }
                        maxX += sumY + XORGateAspectRatio - 1;
                        cachedBox = new Rectangle2D.Double(-maxX / 2, -sumY / 2, maxX, sumY);
                    }
                    return cachedBox;
                }

                @Override
                public Map<String, List<Point2D>> contactPositions() {
                    if (cachedPositions == null) {
                        Map<String, List<Point2D>> contactToPositions = new HashMap<>();

                        double s = boundingBox().getHeight();
                        double x = boundingBox().getMaxX() - s - XORGateAspectRatio + 1;
                        double y = boundingBox().getMinY();
                        double y1 = boundingBox().getMinY();
                        double y2 = boundingBox().getMaxY();
                        for (ComponentRenderingResult renderingResult : renderingResults) {
                            Rectangle2D box = renderingResult.boundingBox();
                            for (String literal : renderingResult.contactPositions().keySet()) {
                                double xOffset = 0;
                                if (box.getHeight() <= 0.5) {
                                    xOffset = getXFromY((y2 - (y + box.getHeight() / 2)) / (y2 - y1), s / 3);
                                }
                                List<Point2D> newPositions = new ArrayList<>();
                                for (Point2D p : renderingResult.contactPositions().get(literal)) {
                                    double newX = p.getX() + x - box.getWidth() / 2 + xOffset;
                                    double newY = p.getY() + y + box.getHeight() / 2;
                                    newPositions.add(new Point2D.Double(newX, newY));
                                    contactToPositions.put(literal, newPositions);
                                }
                            }
                            y += box.getHeight();
                        }
                        cachedPositions = contactToPositions;
                    }
                    return cachedPositions;
                }

                @Override
                public void draw(Graphics2D g) {
                    double s = boundingBox().getHeight();
                    double x = boundingBox().getMaxX() - s;
                    double y = boundingBox().getMinY();

                    Path2D.Double path = new Path2D.Double();
                    path.moveTo(x, y);
                    path.curveTo(x + s / 2, y, x + 0.85 * s, y + s / 4, x + s, y + s / 2);
                    path.curveTo(x + 0.85 * s, y + 0.75 * s, x + s / 2, y + s, x, y + s);
                    path.quadTo(x + s / 3, y + s / 2, x, y);
                    path.closePath();

                    g.setColor(backgroundColor);
                    g.fill(path);
                    g.setColor(foregroundColor);
                    g.draw(path);

                    x -= XORGateAspectRatio - 1;
                    Path2D.Double path2 = new Path2D.Double();
                    path2.moveTo(x, y + s);
                    path2.quadTo(x + s / 3, y + s / 2, x, y);

                    g.draw(path2);

                    double y1 = boundingBox().getMinY();
                    double y2 = boundingBox().getMaxY();
                    for (ComponentRenderingResult renderingResult : renderingResults) {
                        Rectangle2D box = renderingResult.boundingBox();
                        double xOffset = 0;
                        if (renderingResult.boundingBox().getHeight() <= 0.5) {
                            xOffset = getXFromY((y2 - (y + box.getHeight() / 2)) / (y2 - y1), s / 3);
                        }
                        g.translate(x - box.getWidth() / 2 + xOffset, y + box.getHeight() / 2);
                        renderingResult.draw(g);
                        g.translate(-x + box.getWidth() / 2 - xOffset, -y - box.getHeight() / 2);
                        y += box.getHeight();
                    }
                }
            };
        }
    }

    private static double getXFromY(double y, double pivot) {
        return -2 * y * y * pivot + 2 * y * pivot;
    }

    public static ComponentRenderingResult renderGate(BooleanFormula formula) {
        NaryBooleanFormula naryFormula = NaryBooleanFormulaBuilder.build(formula);
        return naryFormula.accept(new NaryBooleanFormulaRenderer());
    }

}
