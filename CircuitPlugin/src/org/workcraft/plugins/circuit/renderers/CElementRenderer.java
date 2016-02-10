package org.workcraft.plugins.circuit.renderers;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.util.Pair;

public class CElementRenderer extends GateRenderer {

    static boolean doNegate = false;
    static boolean isNegated = false;

    static boolean isFirstNode;
    static boolean isGlobalNegation;

    static BooleanVisitor<LinkedList<Pair<String,Boolean>>> defaultVisitor = new BooleanVisitor<LinkedList<Pair<String,Boolean>>>() {

        @Override
        public LinkedList<Pair<String,Boolean>> visit(And node) {
            isFirstNode = false;
            LinkedList<Pair<String,Boolean>> retX = node.getX().accept(this);
            LinkedList<Pair<String,Boolean>> retY = node.getY().accept(this);
            if (retX!=null&&retY!=null) {
                retX.addAll(retY);
            } else retX = retY;
            return retX;
        }

        @Override
        public LinkedList<Pair<String,Boolean>> visit(Iff node) {
            isFirstNode = false;
            LinkedList<Pair<String,Boolean>> retX = node.getX().accept(this);
            LinkedList<Pair<String,Boolean>> retY = node.getY().accept(this);
            if (retX!=null&&retY!=null) {
                retX.addAll(retY);
            } else retX = retY;
            return retX;
        }

        @Override
        public LinkedList<Pair<String,Boolean>> visit(Xor node) {
            isFirstNode = false;
            LinkedList<Pair<String,Boolean>> retX = node.getX().accept(this);
            LinkedList<Pair<String,Boolean>> retY = node.getY().accept(this);
            if (retX!=null&&retY!=null) {
                retX.addAll(retY);
            } else retX = retY;
            return retX;
        }

        @Override
        public LinkedList<Pair<String,Boolean>> visit(Zero node) {
            isFirstNode = false;
            return null;
        }

        @Override
        public LinkedList<Pair<String,Boolean>> visit(One node) {
            isFirstNode = false;
            return null;
        }

        @Override
        public LinkedList<Pair<String,Boolean>> visit(Not node) {
            if (isFirstNode) isGlobalNegation = true;
            isFirstNode = false;
            isNegated = !isNegated;
            LinkedList<Pair<String,Boolean>> ret = node.getX().accept(this);
            isNegated = !isNegated;
            return ret;
        }

        @Override
        public LinkedList<Pair<String,Boolean>> visit(Imply node) {
            isFirstNode = false;
            LinkedList<Pair<String,Boolean>> retX = node.getX().accept(this);
            LinkedList<Pair<String,Boolean>> retY = node.getY().accept(this);
            if (retX!=null&&retY!=null) {
                retX.addAll(retY);
            } else retX = retY;
            return retX;
        }

        @Override
        public LinkedList<Pair<String,Boolean>> visit(BooleanVariable variable) {
            isFirstNode = false;
            LinkedList<Pair<String,Boolean>> ret = new LinkedList<Pair<String,Boolean>>();

            Pair<String,Boolean> vv = new Pair<String,Boolean>(variable.getLabel(), (doNegate)?!isNegated:isNegated);

            ret.add(vv);
            return ret;
        }

        @Override
        public LinkedList<Pair<String,Boolean>> visit(Or node) {
            isFirstNode = false;
            LinkedList<Pair<String,Boolean>> retX = node.getX().accept(this);
            LinkedList<Pair<String,Boolean>> retY = node.getY().accept(this);
            if (retX!=null&&retY!=null) {
                retX.addAll(retY);
            } else retX = retY;
            return retX;
        }

    };

    public static ComponentRenderingResult renderGate(BooleanFormula set, BooleanFormula reset) {

        doNegate = false;
        isNegated = false;
        isFirstNode = true;
        isGlobalNegation = false;
        final LinkedList<Pair<String,Boolean>> setVars = set.accept(defaultVisitor);
        doNegate = true;
        isNegated = false;
        final LinkedList<Pair<String,Boolean>> resetVars = reset.accept(defaultVisitor);
        final LinkedList<Pair<String,Boolean>> bothVars = new LinkedList<Pair<String,Boolean>>();
        for (Pair<String,Boolean> p: setVars) {
            int resetIndex = resetVars.indexOf(p);
            if (resetIndex != -1) {
                bothVars.add(p);
            }
        }

        for (Pair<String,Boolean> p: bothVars) {
            int setIndex = setVars.indexOf(p);
            if (setIndex != -1) {
                setVars.remove(setIndex);
            }
            int resetIndex = resetVars.indexOf(p);
            if (resetIndex != -1) {
                resetVars.remove(resetIndex);
            }
        }

        if (bothVars.isEmpty()) {
            return null;
        }

        return new CElementRenderingResult() {

            final Ellipse2D.Double bubbleShape = new Ellipse2D.Double(-GateRenderer.bubbleSize/2,
                    -GateRenderer.bubbleSize/2, GateRenderer.bubbleSize, GateRenderer.bubbleSize);

            private Rectangle2D cachedBB = null;
            private Map<String, Point2D> cachedPositions = null;
            private double maxX = 0.0;
            private double gX = 0.0;
            private int svs = setVars.size();
            private int bvs = bothVars.size();
            private int rvs = resetVars.size();
            private double sumY = 0.5*(svs+bvs+rvs);

            private Point2D minusPosition = null;
            private Point2D plusPosition = null;
            private Point2D labelPosition = null;

            @Override
            public Rectangle2D boundingBox() {
                if (cachedBB == null) {
                    double h = bothVars.size()*0.5;
                    double x = h * GateRenderer.ANDGateAspectRatio;
                    maxX = 0;
                    if (isGlobalNegation) gX = GateRenderer.bubbleSize;

                    for (Pair<String, Boolean> p1: setVars)
                        if (p1.getSecond()^(gX!=0)) maxX = GateRenderer.bubbleSize;
                    for (Pair<String, Boolean> p2: resetVars)
                        if (p2.getSecond()^(gX!=0)) maxX = GateRenderer.bubbleSize;
                    for (Pair<String, Boolean> p3: bothVars)
                        if (p3.getSecond()^(gX!=0)) maxX = GateRenderer.bubbleSize;

                    if (svs>0) plusPosition = new Point2D.Double(maxX/2-gX/2,-bvs*0.5/2-0.25);
                    if (rvs>0) minusPosition = new Point2D.Double(maxX/2-gX/2,+bvs*0.5/2+0.25);
                    labelPosition = new Point2D.Double(maxX/2-gX/2,0);
                    x += maxX + gX;
                    cachedBB = new Rectangle2D.Double(-x/2,-svs*0.5-bvs*0.5/2,x,sumY);
                }
                return cachedBB;
            }

            @Override
            public Map<String, Point2D> contactPositions() {
                if (cachedPositions == null) {
                    Map<String, Point2D> positions = new HashMap<String, Point2D>();

                    double x = boundingBox().getMaxX()-(bothVars.size()*0.5) * GateRenderer.ANDGateAspectRatio;
                    double y = boundingBox().getMinY();

                    for (Pair<String, Boolean> p: setVars) {
                        double xx = (p.getSecond()^(gX!=0))?GateRenderer.bubbleSize:0;
                        if (gX!=0) xx+=GateRenderer.bubbleSize;
                        positions.put(p.getFirst(), new Point2D.Double(x - xx, y + 0.5/2));
                        y+=0.5;
                    }
                    for (Pair<String, Boolean> p: bothVars) {
                        double xx = (p.getSecond()^(gX!=0))?GateRenderer.bubbleSize:0;
                        if (gX!=0) xx+=GateRenderer.bubbleSize;
                        positions.put(p.getFirst(), new Point2D.Double(x - xx, y + 0.5/2));
                        y+=0.5;
                    }
                    for (Pair<String, Boolean> p: resetVars) {
                        double xx = (p.getSecond()^(gX!=0))?GateRenderer.bubbleSize:0;
                        if (gX!=0) xx+=GateRenderer.bubbleSize;
                        positions.put(p.getFirst(), new Point2D.Double(x - xx, y + 0.5/2));
                        y+=0.5;
                    }

                    cachedPositions = positions;
                }
                return cachedPositions;
            }


            public void drawBubble(Graphics2D g) {
                g.setColor(GateRenderer.background);
                g.fill(bubbleShape);
                g.setColor(GateRenderer.foreground);
                g.draw(bubbleShape);
            }

            @Override
            public void draw(Graphics2D g) {

                double h = bothVars.size()*0.5;
                double x = boundingBox().getMaxX() - h * GateRenderer.ANDGateAspectRatio - gX;
                double y = boundingBox().getMinY();

                Path2D.Double path = new Path2D.Double();

                y = boundingBox().getMinY();

                double w = h * (GateRenderer.ANDGateAspectRatio / 0.8125);
                double y1 = y+svs*0.5;

                path.moveTo(x, y1);
                path.lineTo(x + h / 4, y1);
                path.curveTo(x + w, y1, x + w, y1 + h, x + h / 4, y1 + h);
                path.lineTo(x, y1 + h);
                path.closePath();

                g.setColor(GateRenderer.background);
                g.fill(path);
                g.setColor(GateRenderer.foreground);
                g.draw(path);
                if (!setVars.isEmpty()) {
                    Line2D line = new Line2D.Double(x, y1, x, y1-0.5*setVars.size());
                    g.draw(line);
                }

                if (!resetVars.isEmpty()) {
                    Line2D line = new Line2D.Double(x, y1+h, x, y1+h+0.5*resetVars.size());
                    g.draw(line);
                }


                AffineTransform at= g.getTransform();

                if (gX!=0) {
                    g.translate(boundingBox().getMaxX()-gX/2, 0);
                    drawBubble(g);
                    g.translate(-boundingBox().getMaxX()+gX/2, 0);
                }

                g.translate(x,y);

                for (Pair<String, Boolean> p: setVars) {
                    g.translate(-GateRenderer.bubbleSize/2, 0.5/2);
                    if (p.getSecond()^(gX!=0)) drawBubble(g);
                    g.translate(+GateRenderer.bubbleSize/2, 0.5/2);
                }

                for (Pair<String, Boolean> p: bothVars) {
                    g.translate(-GateRenderer.bubbleSize/2, 0.5/2);
                    if (p.getSecond()^(gX!=0)) drawBubble(g);
                    g.translate(+GateRenderer.bubbleSize/2, 0.5/2);
                }

                for (Pair<String, Boolean> p: resetVars) {
                    g.translate(-GateRenderer.bubbleSize/2, 0.5/2);
                    if (p.getSecond()^(gX!=0)) drawBubble(g);
                    g.translate(+GateRenderer.bubbleSize/2, 0.5/2);
                }

                g.setTransform(at);
            }

            @Override
            public Point2D getLabelPosition() {
                if (labelPosition!=null)
                    return (Point2D) labelPosition.clone();
                return null;
            }

            @Override
            public Point2D getMinusPosition() {
                if (minusPosition!=null)
                    return (Point2D) minusPosition.clone();
                return null;
            }

            @Override
            public Point2D getPlusPosition() {
                if (plusPosition!=null)
                    return (Point2D) plusPosition.clone();
                return null;
            }

        };

        }
    }

