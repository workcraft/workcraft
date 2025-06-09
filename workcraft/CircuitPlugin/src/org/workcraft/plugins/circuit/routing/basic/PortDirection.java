package org.workcraft.plugins.circuit.routing.basic;

public enum PortDirection {
    NORTH(0, -1),
    SOUTH(0, 1),
    EAST(1, 0),
    WEST(-1, 0);

    private final int dx;
    private final int dy;

    PortDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    /**
     * Returns true if the direction is vertical, returns false otherwise.
     */
    public boolean isVertical() {
        return this == NORTH || this == SOUTH;
    }

    /**
     * Returns true if the direction is horizontal, returns false otherwise.
     */
    public boolean isHorizontal() {
        return this == EAST || this == WEST;
    }

    /**
     * Gets the opposite direction.
     *
     * @return the opposite direction
     */
    public PortDirection flip() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }

    /**
     * Convert direction to the horizontal orientation.
     *
     * @return corresponding orientation
     */
    public CoordinateOrientation getHorizontalOrientation() {
        if (this == WEST) {
            return CoordinateOrientation.ORIENT_LOWER;
        }

        if (this == EAST) {
            return CoordinateOrientation.ORIENT_HIGHER;
        }

        return CoordinateOrientation.ORIENT_BOTH;
    }

    /**
     * Convert direction to the vertical orientation.
     *
     * @return corresponding orientation
     */
    public CoordinateOrientation getVerticalOrientation() {
        if (this == NORTH) {
            return CoordinateOrientation.ORIENT_LOWER;
        }

        if (this == SOUTH) {
            return CoordinateOrientation.ORIENT_HIGHER;
        }

        return CoordinateOrientation.ORIENT_BOTH;
    }

}
