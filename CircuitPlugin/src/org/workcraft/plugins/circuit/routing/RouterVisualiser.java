package org.workcraft.plugins.circuit.routing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.gui.graph.Viewport;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.impl.Route;
import org.workcraft.plugins.circuit.routing.impl.Router;
import org.workcraft.plugins.circuit.routing.impl.RouterCells;

public class RouterVisualiser {

    public static void drawEverything(Router router, Graphics2D g) {
        drawBlocks(router, g);
        drawSegments(router, g);
        drawRoutes(router, g);
        drawCells(router, g);
    }

    public static void drawBlocks(Router router, Graphics2D g) {
        float[] pattern = {0.05f, 0.05f};
        g.setStroke(new BasicStroke(0.1f * (float) CircuitSettings.getBorderWidth(),
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));

        g.setColor(Color.GREEN.darker());
        for (Rectangle rec : router.getCoordinatesRegistry().blocked) {
            Rectangle2D drec = new Rectangle2D.Double(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());
            g.draw(drec);
        }
    }

    public static void drawSegments(Router router, Graphics2D g) {
        g.setStroke(new BasicStroke(0.6f * (float) CircuitSettings.getBorderWidth()));
        g.setColor(Color.GREEN.darker());
        for (Line registeredSegment : router.getObstacles().getSegments()) {
            Path2D shape = new Path2D.Double();
            shape.moveTo(registeredSegment.getX1(), registeredSegment.getY1());
            shape.lineTo(registeredSegment.getX2(), registeredSegment.getY2());
            g.draw(shape);
        }
    }

    public static void drawRoutes(Router router, Graphics2D g) {
        for (Route route : router.getRoutingResult()) {
            Path2D routeSegments = new Path2D.Double();
            routeSegments.moveTo(route.source.getLocation().getX(), route.source.getLocation().getY());
            if (route.isRouteFound()) {
                g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
            } else {
                g.setStroke(new BasicStroke(2.5f * (float) CircuitSettings.getBorderWidth()));
            }
            for (Point routePoint : route.getPoints()) {
                routeSegments.lineTo(routePoint.getX(), routePoint.getY());
            }
            g.setColor(Color.GREEN);
            g.setStroke(new BasicStroke(0.1f * (float) CircuitSettings.getBorderWidth()));
            g.draw(routeSegments);
        }
    }

    public static void drawCells(Router router, Graphics2D g) {
        RouterCells rcells = router.getCoordinatesRegistry().getRouterCells();
        int[][] cells = rcells.cells;
        int y = 0;
        for (Coordinate dy : router.getCoordinatesRegistry().getYCoordinates()) {
            int x = 0;
            for (Coordinate dx : router.getCoordinatesRegistry().getXCoordinates()) {
                boolean isBusy = (cells[x][y] & CellState.BUSY) > 0;
                boolean isVerticalPrivate = (cells[x][y] & CellState.VERTICAL_PUBLIC) == 0;
                boolean isHorizontalPrivate = (cells[x][y] & CellState.HORIZONTAL_PUBLIC) == 0;

                boolean isVerticalBlock = (cells[x][y] & CellState.VERTICAL_BLOCK) != 0;
                boolean isHorizontalBlock = (cells[x][y] & CellState.HORIZONTAL_BLOCK) != 0;

                g.setStroke(new BasicStroke(0.6f * (float) CircuitSettings.getBorderWidth()));
                Path2D shape = new Path2D.Double();
                if (isBusy) {
                    g.setColor(Color.RED);
                    shape.moveTo(dx.getValue() - 0.1, dy.getValue() - 0.1);
                    shape.lineTo(dx.getValue() + 0.1, dy.getValue() + 0.1);
                    shape.moveTo(dx.getValue() + 0.1, dy.getValue() - 0.1);
                    shape.lineTo(dx.getValue() - 0.1, dy.getValue() + 0.1);
                    g.draw(shape);
                } else {
                    if (isVerticalPrivate) {
                        shape = new Path2D.Double();
                        g.setColor(Color.BLUE.darker());
                        shape.moveTo(dx.getValue(), dy.getValue() - 0.1);
                        shape.lineTo(dx.getValue(), dy.getValue() + 0.1);
                        g.draw(shape);
                    }

                    if (isHorizontalPrivate) {
                        shape = new Path2D.Double();
                        g.setColor(Color.BLUE.darker());
                        shape.moveTo(dx.getValue() - 0.1, dy.getValue());
                        shape.lineTo(dx.getValue() + 0.1, dy.getValue());
                        g.draw(shape);
                    }

                    if (isVerticalBlock) {
                        shape = new Path2D.Double();
                        g.setColor(Color.RED);
                        shape.moveTo(dx.getValue(), dy.getValue() - 0.1);
                        shape.lineTo(dx.getValue(), dy.getValue() + 0.1);
                        g.draw(shape);
                    }

                    if (isHorizontalBlock) {
                        shape = new Path2D.Double();
                        g.setColor(Color.RED);
                        shape.moveTo(dx.getValue() - 0.1, dy.getValue());
                        shape.lineTo(dx.getValue() + 0.1, dy.getValue());
                        g.draw(shape);
                    }
                }
                x++;
            }
            y++;
        }
    }

    public static void drawCoordinates(Router router, Graphics2D g, Viewport viewport) {
        java.awt.Rectangle bounds = viewport.getShape();
        java.awt.Point screenTopLeft = new java.awt.Point(bounds.x, bounds.y);
        Point2D userTopLeft = viewport.screenToUser(screenTopLeft);
        java.awt.Point screenBottomRight = new java.awt.Point(bounds.x + bounds.width, bounds.y + bounds.height);
        Point2D userBottomRight = viewport.screenToUser(screenBottomRight);

        for (Coordinate x : router.getCoordinatesRegistry().getXCoordinates()) {
            if (!x.isPublic()) {
                continue;
            }

            Path2D grid = new Path2D.Double();
            grid.moveTo(x.getValue(), userTopLeft.getY());
            grid.lineTo(x.getValue(), userBottomRight.getY());
            g.setColor(Color.GRAY.brighter());

            if (x.getOrientation() == CoordinateOrientation.ORIENT_LOWER) {
                g.setColor(Color.BLUE);
            }
            if (x.getOrientation() == CoordinateOrientation.ORIENT_HIGHER) {
                g.setColor(Color.GREEN);
            }

            g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
            g.draw(grid);
        }
        for (Coordinate y : router.getCoordinatesRegistry().getYCoordinates()) {
            if (!y.isPublic()) {
                continue;
            }

            Path2D grid = new Path2D.Double();
            grid.moveTo(userTopLeft.getX(), y.getValue());
            grid.lineTo(userBottomRight.getX(), y.getValue());
            g.setColor(Color.GRAY.brighter());

            if (y.getOrientation() == CoordinateOrientation.ORIENT_LOWER) {
                g.setColor(Color.BLUE);
            }
            if (y.getOrientation() == CoordinateOrientation.ORIENT_HIGHER) {
                g.setColor(Color.GREEN);
            }
            g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
            g.draw(grid);
        }
    }

}
