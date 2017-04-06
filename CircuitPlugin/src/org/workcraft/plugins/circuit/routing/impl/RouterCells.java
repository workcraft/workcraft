package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.IndexedInterval;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

/**
 * Representation of router cells that are used to find routes.
 */
public class RouterCells {

    /** Router cell states. */
    public final int[][] cells;

    /** source port related to this cell. */
    public final RouterPort[][] verticalSourcePorts;
    public final RouterPort[][] horizontalSourcePorts;

    public RouterCells(int width, int height) {
        cells = new int[width][height];
        verticalSourcePorts = new RouterPort[width][height];
        horizontalSourcePorts = new RouterPort[width][height];
    }

    public void mark(int x1, int y1, int x2, int y2, int value) {
        if (value == 0) {
            return;
        }

        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                cells[x][y] |= value;
            }
        }
    }

    public void markSourcePorts(int x1, int y1, int x2, int y2, RouterPort sourcePort) {

        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {

                if (x1 == x2) {
                    verticalSourcePorts[x][y] = sourcePort;
                }

                if (y1 == y2) {
                    horizontalSourcePorts[x][y] = sourcePort;
                }
            }
        }
    }

    public RouterPort getVerticalSourcePort(int x, int y) {
        return verticalSourcePorts[x][y];
    }

    public RouterPort getHorizontalSourcePort(int x, int y) {
        return horizontalSourcePorts[x][y];
    }

    public boolean isMarked(int x, int y, int value) {
        return (cells[x][y] & value) > 0;
    }

    public void mark(IndexedInterval hInterval, IndexedInterval vInterval, int value) {
        if (hInterval == null || vInterval == null) {
            return;
        }
        mark(hInterval.getFrom(), vInterval.getFrom(), hInterval.getTo(), vInterval.getTo(), value);
    }

    public void unmark(int x1, int y1, int x2, int y2, int value) {
        if (value == 0) {
            return;
        }

        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                cells[x][y] = cells[x][y] & (~value);
            }
        }
    }

    public void unmark(IndexedInterval hInterval, IndexedInterval vInterval, int value) {
        if (hInterval == null || vInterval == null) {
            return;
        }
        unmark(hInterval.getFrom(), vInterval.getFrom(), hInterval.getTo(), vInterval.getTo(), value);
    }

    public void markBusy(IndexedInterval hInterval, IndexedInterval vInterval) {
        mark(hInterval, vInterval, CellState.BUSY);
    }

    public void unmarkBusy(IndexedInterval hInterval, IndexedInterval vInterval) {
        unmark(hInterval, vInterval, CellState.BUSY);
    }

}
