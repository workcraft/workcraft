package org.workcraft.plugins.circuit.routing.basic;

public final class Line {
    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;

    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(getX1());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getX2());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY1());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY2());
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
        Line other = (Line) obj;
        if (Double.doubleToLongBits(getX1()) != Double.doubleToLongBits(other.getX1())) {
            return false;
        }
        if (Double.doubleToLongBits(getX2()) != Double.doubleToLongBits(other.getX2())) {
            return false;
        }
        if (Double.doubleToLongBits(getY1()) != Double.doubleToLongBits(other.getY1())) {
            return false;
        }
        if (Double.doubleToLongBits(getY2()) != Double.doubleToLongBits(other.getY2())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Line [x1=" + getX1() + ", y1=" + getY1() + ", x2=" + getX2() + ", y2=" + getY2() + "]";
    }

    /**
     * Returns true if line is vertical, false otherwise.
     *
     * @return true if line is vertical, false otherwise
     */
    public boolean isVertical() {
        return getX1() == getX2() && getY1() != getY2();
    }

    /**
     * Returns true if line is horizontal, false otherwise.
     *
     * @return true if line is horizontal, false otherwise
     */
    public boolean isHorizontal() {
        return getX1() != getX2() && getY1() == getY2();
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }
}
