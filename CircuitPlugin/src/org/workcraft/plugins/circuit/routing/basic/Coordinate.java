package org.workcraft.plugins.circuit.routing.basic;

public final class Coordinate implements Comparable<Coordinate> {
    private final CoordinateOrientation orientation;
    private final boolean isPublic;
    private final double value;

    public Coordinate(CoordinateOrientation orientation, boolean isPublic, double value) {
        this.orientation = orientation;
        this.isPublic = isPublic;
        this.value = value;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (isPublic() ? 1231 : 1237);
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
        if (isPublic() != other.isPublic()) {
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

    public boolean isPublic() {
        return isPublic;
    }

    public double getValue() {
        return value;
    }
}
