package org.workcraft.plugins.circuit.renderers;

import org.workcraft.formula.*;
import org.workcraft.formula.dnf.Dnf;
import org.workcraft.formula.dnf.DnfGenerator;
import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.types.Pair;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CElementRenderer extends GateRenderer {

    private static final double CONTACT_STEP = 0.5;
    private static final double LABEL_OFFSET_X = 0.35;
    private static final double LABEL_OFFSET_Y = CONTACT_STEP / 2;

    private static boolean doNegate = false;
    private static boolean isNegated = false;

    private static boolean isFirstNode;
    private static boolean isGlobalNegation;

    private static final BooleanVisitor<LinkedList<Pair<String, Boolean>>> defaultVisitor = new BooleanVisitor<>() {

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
            return new LinkedList<>();
        }

        @Override
        public LinkedList<Pair<String, Boolean>> visit(One node) {
            isFirstNode = false;
            return new LinkedList<>();
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
        Dnf setDnf = DnfGenerator.generate(setFormula);
        Dnf resetDnf = DnfGenerator.generate(resetFormula);
        if ((setDnf.getClauses().size() != 1) || (resetDnf.getClauses().size() != 1)) {
            return null;
        }

        isFirstNode = true;
        isGlobalNegation = false;
        // Compute Plus set
        doNegate = false;
        isNegated = false;
        BooleanFormula setClause = setDnf.getClauses().iterator().next();
        LinkedList<Pair<String, Boolean>> plusVars = setClause.accept(defaultVisitor);

        // Compute Minus set
        doNegate = true;
        isNegated = false;
        BooleanFormula resetClause = resetDnf.getClauses().iterator().next();
        List<Pair<String, Boolean>> minusVars = resetClause.accept(defaultVisitor);

        // Compute Common set - variables that are both in Plus and Minus set with the same negation attribute
        List<Pair<String, Boolean>> commonVars = new LinkedList<>();
        for (Pair<String, Boolean> var : plusVars) {
            if (minusVars.contains(var)) {
                commonVars.add(var);
            }
        }

        // Clean up Plus and Minus sets
        for (Pair<String, Boolean> var : commonVars) {
            int plusIndex = plusVars.indexOf(var);
            if (plusIndex != -1) {
                plusVars.remove(plusIndex);
            }
            int minusIndex = minusVars.indexOf(var);
            if (minusIndex != -1) {
                minusVars.remove(minusIndex);
            }
        }

        // Check there are no binate variable (they would remain in Plus and Minus set with different negation attribute)
        Set<String> binateLiterals = plusVars.stream().map(Pair::getFirst).collect(Collectors.toSet());
        binateLiterals.retainAll(minusVars.stream().map(Pair::getFirst).collect(Collectors.toSet()));
        if (!binateLiterals.isEmpty()) {
            return null;
        }

        return new CElementRenderingResult() {

            final Ellipse2D.Double bubbleShape = new Ellipse2D.Double(-GateRenderer.bubbleSize / 2,
                    -GateRenderer.bubbleSize / 2, GateRenderer.bubbleSize, GateRenderer.bubbleSize);

            private Rectangle2D cachedBox = null;
            private Map<String, List<Point2D>> cachedPositions = null;
            private double bubbleOffset = 0.0;

            private Point2D minusPosition = null;
            private Point2D labelPosition = null;
            private Point2D plusPosition = null;

            private double getCommonLength() {
                return commonVars.size() * CONTACT_STEP;
            }

            private double getCenterLength() {
                return Math.max(getCommonLength(), 1.0);
            }

            private double getPlusLength() {
                return plusVars.size() * CONTACT_STEP;
            }

            private double getMinusLength() {
                return minusVars.size() * CONTACT_STEP;
            }

            @Override
            public Rectangle2D getBoundingBox() {
                if (cachedBox == null) {
                    // Output bubble
                    if (isGlobalNegation) {
                        bubbleOffset = GateRenderer.bubbleSize;
                    }
                    // Input bubbles
                    Set<Pair<String, Boolean>> vars = new HashSet<>();
                    vars.addAll(plusVars);
                    vars.addAll(commonVars);
                    vars.addAll(minusVars);
                    final boolean hasInputBubbles = vars.stream().anyMatch(var -> var.getSecond() ^ (bubbleOffset != 0));
                    final double inputBubbleOffset = hasInputBubbles ? GateRenderer.bubbleSize : 0;
                    // Label positions
                    final double s = getCenterLength();
                    final double s2 = s / 2;
                    final double w = inputBubbleOffset + s * GateRenderer.ANDGateAspectRatio + bubbleOffset;
                    final double h = getCenterLength() + getPlusLength() + getMinusLength();
                    if (!plusVars.isEmpty()) {
                        plusPosition = new Point2D.Double((inputBubbleOffset - w) / 2 + LABEL_OFFSET_X, -s2 - LABEL_OFFSET_Y);
                    }
                    if (!minusVars.isEmpty()) {
                        minusPosition = new Point2D.Double((inputBubbleOffset - w) / 2 + LABEL_OFFSET_X, s2 + LABEL_OFFSET_Y);
                    }
                    labelPosition = new Point2D.Double((inputBubbleOffset - bubbleOffset) / 2, 0);

                    cachedBox = new Rectangle2D.Double(-w / 2, -getPlusLength() - s2, w, h);
                }
                return cachedBox;
            }

            @Override
            public Map<String, List<Point2D>> getContactPositions() {
                if (cachedPositions == null) {
                    cachedPositions = new HashMap<>();
                    final double s = getCenterLength();
                    final double s2 = s / 2;
                    final double x = getBoundingBox().getMaxX() - s * GateRenderer.ANDGateAspectRatio;
                    cachedPositions.putAll(getContactPositions(new Point2D.Double(x, -s2 - getPlusLength()), plusVars));
                    cachedPositions.putAll(getContactPositions(new Point2D.Double(x, -getCommonLength() / 2), commonVars));
                    cachedPositions.putAll(getContactPositions(new Point2D.Double(x, s2), minusVars));
                }
                return cachedPositions;
            }

            private Map<String, List<Point2D>> getContactPositions(Point2D p, List<Pair<String, Boolean>> vars) {
                Map<String, List<Point2D>> result = new HashMap<>();
                double x = p.getX();
                double y = p.getY();
                double dy = CONTACT_STEP / 2;
                for (Pair<String, Boolean> var : vars) {
                    double xOffset = (var.getSecond() ^ (bubbleOffset != 0)) ? GateRenderer.bubbleSize : 0;
                    if (bubbleOffset != 0) {
                        xOffset += GateRenderer.bubbleSize;
                    }
                    y += dy;
                    result.put(var.getFirst(), Collections.singletonList(new Point2D.Double(x - xOffset, y)));
                    y += dy;
                }
                return result;
            }

            @Override
            public void draw(Graphics2D g) {
                final double s = getCenterLength();
                final double s2 = s / 2;
                final double x = getBoundingBox().getMaxX() - s * GateRenderer.ANDGateAspectRatio - bubbleOffset;

                // Common part
                Path2D.Double path = new Path2D.Double();
                path.moveTo(x, -s2);
                path.lineTo(x + s2 / 2, -s2);
                path.curveTo(x + s, -s2, x + s, s2, x + s2 / 2, s2);
                path.lineTo(x, s2);
                path.closePath();
                g.setColor(GateRenderer.backgroundColor);
                g.fill(path);
                g.setColor(GateRenderer.foregroundColor);
                g.draw(path);

                // Plus part
                if (!plusVars.isEmpty()) {
                    Line2D line = new Line2D.Double(x, -s2, x, -s2 - getPlusLength());
                    g.draw(line);
                }

                // Minus part
                if (!minusVars.isEmpty()) {
                    Line2D line = new Line2D.Double(x, -s2 + s, x, s2 + getMinusLength());
                    g.draw(line);
                }

                drawBubbles(g, new Point2D.Double(x, -s2 - getPlusLength()), plusVars);
                drawBubbles(g, new Point2D.Double(x, -getCommonLength() / 2), commonVars);
                drawBubbles(g, new Point2D.Double(x, s2), minusVars);

                // Output bubble
                if (bubbleOffset != 0) {
                    g.translate(getBoundingBox().getMaxX() - bubbleOffset / 2, 0);
                    drawBubble(g);
                    g.translate(-getBoundingBox().getMaxX() + bubbleOffset / 2, 0);
                }
            }

            private void drawBubbles(Graphics2D g, Point2D p, List<Pair<String, Boolean>> vars) {
                AffineTransform at = g.getTransform();
                g.translate(p.getX(), p.getY());
                double dx = GateRenderer.bubbleSize / 2;
                double dy = CONTACT_STEP / 2;
                for (Pair<String, Boolean> var : vars) {
                    g.translate(-dx, dy);
                    if (var.getSecond() ^ (bubbleOffset != 0)) {
                        drawBubble(g);
                    }
                    g.translate(dx, dy);
                }
                g.setTransform(at);
            }

            private void drawBubble(Graphics2D g) {
                g.setColor(GateRenderer.backgroundColor);
                g.fill(bubbleShape);
                g.setColor(GateRenderer.foregroundColor);
                g.draw(bubbleShape);
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
