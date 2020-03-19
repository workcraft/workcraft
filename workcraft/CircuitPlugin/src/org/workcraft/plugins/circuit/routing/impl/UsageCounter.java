package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;

import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;

public class UsageCounter {

    private final int[][] horizontalSegments;
    private final int[][] verticalSegments;

    private final int[] xCoordMaxUsage;
    private final int[] yCoordMaxUsage;

    private final int width;
    private final int height;

    public UsageCounter(int width, int height) {
        horizontalSegments = new int[width][height];
        verticalSegments = new int[width][height];
        xCoordMaxUsage = new int[width];
        yCoordMaxUsage = new int[height];
        this.width = width;
        this.height = height;
    }

    public void markUsage(int x1, int y1, int x2, int y2) {
        assert x1 == x2 || y1 == y2;
        assert x1 != x2 || y1 != y2;
        if (y1 == y2) {
            increaseHorizontal(Math.min(x1, x2), Math.max(x1, x2), y1);
        }
        if (x1 == x2) {
            increaseVertical(x1, Math.min(y1, y2), Math.max(y1, y2));
        }
    }

    private void increaseVertical(int x, int y1, int y2) {
        for (int y = y1; y <= y2; y++) {
            xCoordMaxUsage[x] = Math.max(xCoordMaxUsage[x], ++verticalSegments[x][y]);
        }
    }

    private void increaseHorizontal(int x1, int x2, int y) {
        for (int x = x1; x <= x2; x++) {
            yCoordMaxUsage[y] = Math.max(yCoordMaxUsage[y], ++horizontalSegments[x][y]);
        }
    }

    public int getXCoordUsage(int x) {
        return xCoordMaxUsage[x];
    }

    public int getYCoordUsage(int y) {
        return yCoordMaxUsage[y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void updateUsageCounter(List<List<IndexedPoint>> paths) {
        for (List<IndexedPoint> path : paths) {
            for (int i = 1; i < path.size(); i++) {
                IndexedPoint p1 = path.get(i - 1);
                IndexedPoint p2 = path.get(i);
                markUsage(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }
    }

}
