package org.workcraft.plugins.circuit.routing.basic;

public final class Coordinate implements Comparable<Coordinate> {
    private final CoordinateOrientation orientation;
    private final boolean accessible;
    private final double value;

    public Coordinate(CoordinateOrientation orientation, boolean accessible, double value) {
        this.orientation = orientation;
        this.accessible = accessible;
        this.value = value;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (isAccessible() ? 1231 : 1237);
        result = prime * result + ((getOrientation() == null) ? 0 : getOrientation().hashCode());
        long temp;
        temp = Double.doubleToLongBits(getValue());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coordinate other = (Coordinate) obj;
        if (isAccessible() != other.isAccessible()) {
            return false;
        }
        if (getOrientation() != other.getOrientation()) {
            return false;
        }
        if (Double.doubleToLongBits(getValue()) != Double.doubleToLongBits(other.getValue())) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Coordinate other) {
        return Double.compare(getValue(), other.getValue());
    }

    public CoordinateOrientation getOrientation() {
        return orientation;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public double getValue() {
        return value;
    }
}
