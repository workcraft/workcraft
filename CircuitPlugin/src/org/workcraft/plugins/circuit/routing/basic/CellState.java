package org.workcraft.plugins.circuit.routing.basic;

public class CellState {

    /** Cell is occupied by an obstacle (gate, pin-to-gate segment, primary port). */
    public static final int BUSY = 1;

    /** Vertical propagation is blocked for routing. */
    public static final int VERTICAL_BLOCK = 2;

    /** Horizontal propagation is blocked for routing. */
    public static final int HORIZONTAL_BLOCK = 4;

    /** Only allow horizontal propagation for entering or exiting ports. */
    public static final int HORIZONTAL_PRIVATE = 8;

    /** Only allow vertical propagation for entering or exiting ports. */
    public static final int VERTICAL_PRIVATE = 16;

}
