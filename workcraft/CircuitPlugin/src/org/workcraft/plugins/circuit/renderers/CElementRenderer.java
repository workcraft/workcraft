package org.workcraft.plugins.circuit.renderers;

import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.types.Pair;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.*;

public class CElementRenderer extends GateRenderer {

    private static boolean doNegate = false;
    private static boolean isNegated = false;

    private static boolean isFirstNode;
    private static boolean isGlobalNegation;

    private static final BooleanVisitor<LinkedList<Pair<String, Boolean>>> defaultVisitor = new BooleanVisitor<
            LinkedList<Pair<String, Boolean>>>() {

        private LinkedList<Pair<String, Boolean>> visitBinary(BinaryBooleanFormula node) {
            LinkedList<Pair<String, Boolean>> x = node.getX().accept(this);
            LinkedList<Pair<String, Boolean>> y = node.getY().accept(this);
            if (x != null) {
                if (y != null) {
                    x.addAll(y);
                }
                return x;
            }
            return y;
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(And node) {
            isFirstNode = false;
            return visitBinary(node);
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(Iff node) {
            isFirstNode = false;
            return visitBinary(node);
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(Xor node) {
            isFirstNode = false;
            return visitBinary(node);
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(Zero node) {
            isFirstNode = false;
            return null;
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(One node) {
            isFirstNode = false;
            return null;
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(Not node) {
            if (isFirstNode) isGlobalNegation = true;
            isFirstNode = false;
            isNegated = !isNegated;
            LinkedList<Pair<String, Boolean>> ret = node.getX().accept(this);
            isNegated = !isNegated;
            return ret;
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(Imply node) {
            isFirstNode = false;
            return visitBinary(node);
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(BooleanVariable variable) {
            isFirstNode = false;
            LinkedList<Pair<String, Boolean>> ret = new LinkedList<>();
            Pair<String, Boolean> vv = new Pair<>(variable.getLabel(), doNegate != isNegated);
            ret.add(vv);
            return ret;
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(Or node) {
            isFirstNode = false;
            return visitBinary(node);
        }

    };

    public static ComponentRenderingResult renderGate(BooleanFormula setFormula, BooleanFormula resetFormula) {
        isFirstNode = true;
        isGlobalNegation = false;

        // Compute Plus set
        doNegate = false;
        isNegated = false;
        LinkedList<Pair<String, Boolean>> plusVars = setFormula.accept(defaultVisitor);

        // Compute Minus set
        doNegate = true;
        isNegated = false;
        List<Pair<String, Boolean>> minusVars = resetFormula.accept(defaultVisitor);

        // Compute Common set
        List<Pair<String, Boolean>> commonVars = new LinkedList<>();
        for (Pair<String, Boolean> var : plusVars) {
            int resetIndex = minusVars.indexOf(var);
            if (resetIndex != -1) {
                commonVars.add(var);
            }
        }

        if (commonVars.isEmpty()) {
            return null;
        }

        // Clean up Plus set
        for (Pair<String, Boolean> var : commonVars) {
            int setIndex = plusVars.indexOf(var);
            if (setIndex != -1) {
                plusVars.remove(setIndex);
            }
        }

        // Clean up Minus set
        for (Pair<String, Boolean> var : commonVars) {
            int resetIndex = minusVars.indexOf(var);
            if (resetIndex != -1) {
                minusVars.remove(resetIndex);
            }
        }

        return new CElementRenderingResult() {

            final Ellipse2D.Double bubbleShape = new Ellipse2D.Double(-GateRenderer.bubbleSize / 2,
                    -GateRenderer.bubbleSize / 2, GateRenderer.bubbleSize, GateRenderer.bubbleSize);

            private Rectangle2D cachedBox = null;
            private Map<String, List<Point2D>> cachedPositions = null;
            private double gX = 0.0;
            private final int plusCount = plusVars.size();
            private final int commonCount = commonVars.size();
            private final int minusCount = minusVars.size();
            private final double sumY = 0.5 * (plusCount + commonCount + minusCount);

            private Point2D minusPosition = null;
            private Point2D labelPosition = null;
            private Point2D plusPosition = null;

            @Override
            public Rectangle2D boundingBox() {
                if (cachedBox == null) {
                    double s = commonCount * 0.5;
                    double x = s * GateRenderer.ANDGateAspectRatio;
                    double maxX = 0;
                    if (isGlobalNegation) {
                        gX = GateRenderer.bubbleSize;
                    }

                    for (Pair<String, Boolean> var : plusVars) {
                        if (var.getSecond() ^ (gX != 0)) {
                            maxX = GateRenderer.bubbleSize;
                            break;
                        }
                    }
                    for (Pair<String, Boolean> var : minusVars) {
                        if (var.getSecond() ^ (gX != 0)) {
                            maxX = GateRenderer.bubbleSize;
                            break;
                        }
                    }
                    for (Pair<String, Boolean> var : commonVars) {
                        if (var.getSecond() ^ (gX != 0)) {
                            maxX = GateRenderer.bubbleSize;
                            break;
                        }
                    }

                    if (plusCount > 0) {
                        plusPosition = new Point2D.Double(maxX / 2 - gX / 2, -commonCount * 0.5 / 2 - 0.25);
                    }
                    if (minusCount > 0) {
                        minusPosition = new Point2D.Double(maxX / 2 - gX / 2, commonCount * 0.5 / 2 + 0.25);
                    }
                    labelPosition = new Point2D.Double(maxX / 2 - gX / 2, 0);
                    x += maxX + gX;
                    cachedBox = new Rectangle2D.Double(-x / 2, -plusCount * 0.5 - commonCount * 0.5 / 2, x, sumY);
                }
                return cachedBox;
            }

            @Override
            public Map<String, List<Point2D>> contactPositions() {
                if (cachedPositions == null) {
                    Map<String, List<Point2D>> literalToPositions = new HashMap<>();

                    double x = boundingBox().getMaxX() - (commonVars.size() * 0.5) * GateRenderer.ANDGateAspectRatio;
                    double y = boundingBox().getMinY();

                    for (Pair<String, Boolean> p : plusVars) {
                        double xx = (p.getSecond() ^ (gX != 0)) ? GateRenderer.bubbleSize : 0;
                        if (gX != 0) xx += GateRenderer.bubbleSize;
                        literalToPositions.put(p.getFirst(), Collections.singletonList(new Point2D.Double(x - xx, y + 0.5 / 2)));
                        y += 0.5;
                    }
                    for (Pair<String, Boolean> p : commonVars) {
                        double xx = (p.getSecond() ^ (gX != 0)) ? GateRenderer.bubbleSize : 0;
                        if (gX != 0) xx += GateRenderer.bubbleSize;
                        literalToPositions.put(p.getFirst(), Collections.singletonList(new Point2D.Double(x - xx, y + 0.5 / 2)));
                        y += 0.5;
                    }
                    for (Pair<String, Boolean> p : minusVars) {
                        double xx = (p.getSecond() ^ (gX != 0)) ? GateRenderer.bubbleSize : 0;
                        if (gX != 0) xx += GateRenderer.bubbleSize;
                        literalToPositions.put(p.getFirst(), Collections.singletonList(new Point2D.Double(x - xx, y + 0.5 / 2)));
                        y += 0.5;
                    }

                    cachedPositions = literalToPositions;
                }
                return cachedPositions;
            }

            public void drawBubble(Graphics2D g) {
                g.setColor(GateRenderer.backgroundColor);
                g.fill(bubbleShape);
                g.setColor(GateRenderer.foregroundColor);
                g.draw(bubbleShape);
            }

            @Override
            public void draw(Graphics2D g) {
                double s = commonVars.size() * 0.5;
                double x = boundingBox().getMaxX() - s * GateRenderer.ANDGateAspectRatio - gX;
                double y = boundingBox().getMinY();
                double y1 = y + plusCount * 0.5;

                Path2D.Double path = new Path2D.Double();
                path.moveTo(x, y1);
                path.lineTo(x + s / 4, y1);
                path.curveTo(x + s, y1, x + s, y1 + s, x + s / 4, y1 + s);
                path.lineTo(x, y1 + s);
                path.closePath();

                g.setColor(GateRenderer.backgroundColor);
                g.fill(path);
                g.setColor(GateRenderer.foregroundColor);
                g.draw(path);
                if (!plusVars.isEmpty()) {
                    Line2D line = new Line2D.Double(x, y1, x, y1 - 0.5 * plusVars.size());
                    g.draw(line);
                }

                if (!minusVars.isEmpty()) {
                    Line2D line = new Line2D.Double(x, y1 + s, x, y1 + s + 0.5 * minusVars.size());
                    g.draw(line);
                }

                AffineTransform at = g.getTransform();

                if (gX != 0) {
                    g.translate(boundingBox().getMaxX() - gX / 2, 0);
                    drawBubble(g);
                    g.translate(-boundingBox().getMaxX() + gX / 2, 0);
                }

                g.translate(x, y);

                for (Pair<String, Boolean> p: plusVars) {
                    g.translate(-GateRenderer.bubbleSize / 2, 0.5 / 2);
                    if (p.getSecond() ^ (gX != 0)) drawBubble(g);
                    g.translate(GateRenderer.bubbleSize / 2, 0.5 / 2);
                }

                for (Pair<String, Boolean> p: commonVars) {
                    g.translate(-GateRenderer.bubbleSize / 2, 0.5 / 2);
                    if (p.getSecond() ^ (gX != 0)) drawBubble(g);
                    g.translate(GateRenderer.bubbleSize / 2, 0.5 / 2);
                }

                for (Pair<String, Boolean> p: minusVars) {
                    g.translate(-GateRenderer.bubbleSize / 2, 0.5 / 2);
                    if (p.getSecond() ^ (gX != 0)) drawBubble(g);
                    g.translate(GateRenderer.bubbleSize / 2, 0.5 / 2);
                }

                g.setTransform(at);
            }

            @Override
            public Point2D getLabelPosition() {
                if (labelPosition != null) {
                    return (Point2D) labelPosition.clone();
                }
                return null;
            }

            @Override
            public Point2D getMinusPosition() {
                if (minusPosition != null) {
                    return (Point2D) minusPosition.clone();
                }
                return null;
            }

            @Override
            public Point2D getPlusPosition() {
                if (plusPosition != null) {
                    return (Point2D) plusPosition.clone();
                }
                return null;
            }
        };
    }

}
