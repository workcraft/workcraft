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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GateRenderer {

    public static final double bubbleSize = 0.3;
    public static final double ANDGateAspectRatio = 0.8125;
    public static final double XORGateAspectRatio = 1.15;
    public static final double contactMargin = 0.5;
    public static Color foreground = Color.BLACK;
    public static Color background = Color.WHITE;

    private static class NaryBooleanFormulaRenderer implements NaryBooleanFormulaVisitor<ComponentRenderingResult> {

        private boolean isBuffer = true;

        @Override
        public ComponentRenderingResult visit(final BooleanVariable var) {

            final Rectangle2D bb =
                    isBuffer ? new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0) : new Rectangle2D.Double(0.0, -0.25, 0.0, 0.5);

            return new ComponentRenderingResult() {
                @Override
                public Rectangle2D boundingBox() {
                    return bb;
                }

                @Override
                public Map<String, Point2D> contactPositions() {
                    Map<String, Point2D> result = new HashMap<>();
                    if (isBuffer) {
                        result.put(var.getLabel(), new Point2D.Double(-0.5, 0));
                    } else {
                        result.put(var.getLabel(), new Point2D.Double(0, 0));
                    }
                    return result;
                }

                @Override
                public void draw(Graphics2D g) {
                    if (isBuffer) {
                        Path2D path = new Path2D.Double();
                        path.moveTo(-0.5, -0.5);
                        path.lineTo(-0.5, 0.5);
                        path.lineTo(0.5, 0);
                        path.closePath();

                        g.setColor(background);
                        g.fill(path);
                        g.setColor(foreground);
                        g.draw(path);
                    }
                }
            };
        }

        @Override
        public ComponentRenderingResult visitNot(NaryBooleanFormula arg) {

            final ComponentRenderingResult result = arg.accept(this);
            final Rectangle2D bb = result.boundingBox();
            final Ellipse2D.Double bubbleShape = new Ellipse2D.Double(-bubbleSize / 2, -bubbleSize / 2, bubbleSize, bubbleSize);
            final double w = bb.getWidth() + bubbleSize;
            final double h = Math.max(bb.getHeight(), 0.5);

            bb.setRect(new Rectangle2D.Double(-w / 2, -h / 2, w, h));

            return new ComponentRenderingResult() {

                @Override
                public void draw(Graphics2D g) {
                    g.translate(-bubbleSize / 2, 0);
                    result.draw(g);
                    g.translate(w / 2, 0);
                    g.setColor(background);
                    g.fill(bubbleShape);
                    g.setColor(foreground);
                    g.draw(bubbleShape);
                    g.translate(-w / 2 + bubbleSize / 2, 0);
                }

                @Override
                public Map<String, Point2D> contactPositions() {
                    Map<String, Point2D> positions = new HashMap<>();
                    for (String v : result.contactPositions().keySet()) {
                        Point2D p = result.contactPositions().get(v);
                        positions.put(v, new Point2D.Double(p.getX() - bubbleSize / 2, p.getY()));
                    }
                    return positions;
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
            final List<ComponentRenderingResult> results = new LinkedList<>();

            for (NaryBooleanFormula formula : args) {
                results.add(formula.accept(this));
            }

            return new ComponentRenderingResult() {

                private Rectangle2D cachedBB = null;
                private Map<String, Point2D> cachedPositions = null;

                @Override
                public Rectangle2D boundingBox() {
                    if (cachedBB == null) {
                        double maxX = 0;
                        double sumY = 0;
                        for (ComponentRenderingResult res: results) {
                            Rectangle2D rec = res.boundingBox();
                            if (maxX < rec.getWidth()) maxX = rec.getWidth();
                            sumY += rec.getHeight();
                        }

                        maxX += sumY * ANDGateAspectRatio;
                        cachedBB = new Rectangle2D.Double(-maxX / 2, -sumY / 2, maxX, sumY);
                    }
                    return cachedBB;
                }

                @Override
                public Map<String, Point2D> contactPositions() {
                    if (cachedPositions == null) {
                        Map<String, Point2D> positions = new HashMap<>();

                        double x = boundingBox().getMaxX() - boundingBox().getHeight() * ANDGateAspectRatio;
                        double y = boundingBox().getMinY();

                        for (ComponentRenderingResult res: results) {
                            Rectangle2D rec = res.boundingBox();
                            for (String v : res.contactPositions().keySet()) {
                                Point2D p = res.contactPositions().get(v);
                                positions.put(v, new Point2D.Double(p.getX() + x - rec.getWidth() / 2, p.getY() + y + rec.getHeight() / 2));
                            }
                            y += rec.getHeight();
                        }
                        cachedPositions = positions;
                    }
                    return cachedPositions;
                }

                @Override
                public void draw(Graphics2D g) {

                    double h = boundingBox().getHeight();
                    double x = boundingBox().getMaxX() - h * ANDGateAspectRatio;
                    double y = boundingBox().getMinY();

                    for (ComponentRenderingResult res: results) {
                        Rectangle2D rec = res.boundingBox();

                        g.translate(x - rec.getWidth() / 2, y + rec.getHeight() / 2);
                        res.draw(g);
                        g.translate(-x + rec.getWidth() / 2, -y - rec.getHeight() / 2);

                        y += rec.getHeight();
                    }

                    Path2D.Double path = new Path2D.Double();
                    y = boundingBox().getMinY();
                    double w = h * (ANDGateAspectRatio / 0.8125);

                    path.moveTo(x, y);
                    path.lineTo(x + h / 4, y);
                    path.curveTo(x + w, y, x + w, y + h, x + h / 4, y + h);
                    path.lineTo(x, y + h);
                    path.closePath();

                    g.setColor(background);
                    g.fill(path);
                    g.setColor(foreground);
                    g.draw(path);
                }

            };
        }

        @Override
        public ComponentRenderingResult visitOr(List<NaryBooleanFormula> args) {
            isBuffer = false;
            final List<ComponentRenderingResult> results = new LinkedList<>();

            for (NaryBooleanFormula formula : args) {
                results.add(formula.accept(this));
            }

            return new ComponentRenderingResult() {
                private Rectangle2D cachedBB = null;
                private Map<String, Point2D> cachedPositions = null;

                @Override
                public Rectangle2D boundingBox() {
                    if (cachedBB == null) {
                        double maxX = 0;
                        double sumY = 0;
                        for (ComponentRenderingResult res: results) {
                            Rectangle2D rec = res.boundingBox();
                            if (maxX < rec.getWidth()) maxX = rec.getWidth();
                            sumY += rec.getHeight();
                        }
                        cachedBB = new Rectangle2D.Double(-(sumY + maxX) / 2, -sumY / 2, sumY + maxX, sumY);
                    }
                    return cachedBB;
                }

                @Override
                public Map<String, Point2D> contactPositions() {
                    if (cachedPositions == null) {
                        Map<String, Point2D> positions = new HashMap<>();

                        double h = boundingBox().getHeight();
                        double x = boundingBox().getMaxX() - h;
                        double y1 = boundingBox().getMinY();
                        double y2 = boundingBox().getMaxY();
                        double y = y1;

                        for (ComponentRenderingResult res: results) {
                            Rectangle2D rec = res.boundingBox();
                            for (String v : res.contactPositions().keySet()) {
                                Point2D p = res.contactPositions().get(v);
                                double xofs = 0;
                                if (res.boundingBox().getHeight() <= 0.5) {
                                    xofs = +getXFromY((y2 - (y + rec.getHeight() / 2)) / (y2 - y1), h / 3);
                                }
                                positions.put(v, new Point2D.Double(
                                        p.getX() + x - rec.getWidth() / 2 + xofs,
                                        p.getY() + y + rec.getHeight() / 2));
                            }
                            y += rec.getHeight();
                        }
                        cachedPositions = positions;
                    }
                    return cachedPositions;
                }

                @Override
                public void draw(Graphics2D g) {
                    double h = boundingBox().getHeight();
                    double x = boundingBox().getMaxX() - h;
                    double y = boundingBox().getMinY();
                    double y1 = boundingBox().getMinY();
                    double y2 = boundingBox().getMaxY();

                    Path2D.Double path = new Path2D.Double();

                    path.moveTo(x, y);
                    path.curveTo(x + h / 2, y, x + 0.85 * h, y + h / 4, x + h, y + h / 2);
                    path.curveTo(x + 0.85 * h, y + 0.75 * h, x + h / 2, y + h, x, y + h);
                    path.quadTo(x + h / 3, y + h / 2, x, y);
                    path.closePath();

                    g.setColor(background);
                    g.fill(path);
                    g.setColor(foreground);
                    g.draw(path);

                    for (ComponentRenderingResult res: results) {
                        Rectangle2D rec = res.boundingBox();
                        double xofs = 0;
                        if (rec.getHeight() <= 0.5) {
                            xofs = getXFromY((y2 - (y + rec.getHeight() / 2)) / (y2 - y1), h / 3);
                        }
                        g.translate(x - rec.getWidth() / 2 + xofs, y + rec.getHeight() / 2);
                        res.draw(g);
                        g.translate(-x + rec.getWidth() / 2 - xofs, -y - rec.getHeight() / 2);
                        y += rec.getHeight();
                    }
                }
            };
        }

        @Override
        public ComponentRenderingResult visitXor(List<NaryBooleanFormula> args) {
            isBuffer = false;
            final List<ComponentRenderingResult> results = new LinkedList<>();

            for (NaryBooleanFormula formula : args) {
                results.add(formula.accept(this));
            }

            return new ComponentRenderingResult() {
                private Rectangle2D cachedBB = null;
                private Map<String, Point2D> cachedPositions = null;
                @Override
                public Rectangle2D boundingBox() {
                    if (cachedBB == null) {
                        double maxX = 0;
                        double sumY = 0;
                        for (ComponentRenderingResult res: results) {
                            Rectangle2D rec = res.boundingBox();
                            if (maxX < rec.getWidth()) maxX = rec.getWidth();
                            sumY += rec.getHeight();
                        }
                        maxX += sumY + XORGateAspectRatio - 1;
                        cachedBB = new Rectangle2D.Double(-maxX / 2, -sumY / 2, maxX, sumY);
                    }
                    return cachedBB;
                }

                @Override
                public Map<String, Point2D> contactPositions() {
                    if (cachedPositions == null) {
                        Map<String, Point2D> positions = new HashMap<>();

                        double h = boundingBox().getHeight();
                        double x = boundingBox().getMaxX() - h - XORGateAspectRatio + 1;
                        double y1 = boundingBox().getMinY();
                        double y2 = boundingBox().getMaxY();
                        double y = y1;

                        for (ComponentRenderingResult res: results) {
                            Rectangle2D rec = res.boundingBox();
                            for (String v : res.contactPositions().keySet()) {
                                double xofs = 0;
                                if (res.boundingBox().getHeight() <= 0.5) {
                                    xofs = +getXFromY((y2 - (y + rec.getHeight() / 2)) / (y2 - y1), h / 3);
                                }
                                Point2D p = res.contactPositions().get(v);
                                positions.put(v, new Point2D.Double(p.getX() + x - rec.getWidth() / 2 + xofs, p.getY() + y + rec.getHeight() / 2));
                            }
                            y += rec.getHeight();
                        }
                        cachedPositions = positions;
                    }
                    return cachedPositions;
                }

                @Override
                public void draw(Graphics2D g) {
                    double h = boundingBox().getHeight();
                    double x = boundingBox().getMaxX() - h;
                    double y1 = boundingBox().getMinY();
                    double y2 = boundingBox().getMaxY();
                    double y = y1;
                    Path2D.Double path = new Path2D.Double();
                    y = boundingBox().getMinY();
                    path.moveTo(x, y);
                    path.curveTo(x + h / 2, y, x + 0.85 * h, y + h / 4, x + h, y + h / 2);
                    path.curveTo(x + 0.85 * h, y + 0.75 * h, x + h / 2, y + h, x, y + h);
                    path.quadTo(x + h / 3, y + h / 2, x, y);
                    path.closePath();

                    g.setColor(background);
                    g.fill(path);
                    g.setColor(foreground);
                    g.draw(path);

                    Path2D.Double path2 = new Path2D.Double();

                    x -= XORGateAspectRatio - 1;

                    path2.moveTo(x, y + h);
                    path2.quadTo(x + h / 3, y + h / 2, x, y);

                    g.draw(path2);

                    for (ComponentRenderingResult res: results) {
                        Rectangle2D rec = res.boundingBox();
                        double xofs = 0;
                        if (res.boundingBox().getHeight() <= 0.5) {
                            xofs = +getXFromY((y2 - (y + rec.getHeight() / 2)) / (y2 - y1), h / 3);
                        }
                        g.translate(x - rec.getWidth() / 2 + xofs, y + rec.getHeight() / 2);
                        res.draw(g);
                        g.translate(-x + rec.getWidth() / 2 - xofs, -y - rec.getHeight() / 2);
                        y += rec.getHeight();
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
