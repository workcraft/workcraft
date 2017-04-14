package org.workcraft.plugins.circuit.routing.basic;

public enum CoordinateOrientation {
    /** coordinate facing higher coordinates. */
    ORIENT_HIGHER,
    /** coordinate facing lower coordinates. */
    ORIENT_LOWER,
    /** Coordinate facing both sides. */
    ORIENT_BOTH,
    /** Coordinate not facing any side. */
    ORIENT_NONE;

    public CoordinateOrientation merge(CoordinateOrientation other) {
        assert other != null;
        if (other == ORIENT_NONE) {
            return this;
        }
        if (this == ORIENT_NONE) {
            return other;
        }
        if (this == other) {
            return this;
        }
        return ORIENT_BOTH;
    }

}
