package org.workcraft.plugins.circuit.routing.basic;

public final class Rectangle {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public Rectangle(double x, double y, double width, double height) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(getHeight());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getWidth());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getX());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
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
        final Rectangle other = (Rectangle) obj;
        if (Double.doubleToLongBits(getHeight()) != Double.doubleToLongBits(other.getHeight())) {
            return false;
        }
        if (Double.doubleToLongBits(getWidth()) != Double.doubleToLongBits(other.getWidth())) {
            return false;
        }
        if (Double.doubleToLongBits(getX()) != Double.doubleToLongBits(other.getX())) {
            return false;
        }
        if (Double.doubleToLongBits(getY()) != Double.doubleToLongBits(other.getY())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Rectangle [x=" + getX() + ", y=" + getY() + ", width=" + getWidth() + ", height=" + getHeight() + "]";
    }

    public Line getPortSegment(Point location) {
        double dx = 0;
        double dy = 0;

        if (location.getX() < getX()) {
            dx = getX() - location.getX();
        }

        if (location.getX() > getX() + getWidth()) {
            dx = getX() + getWidth() - location.getX();
        }

        if (location.getY() < getY()) {
            dy = getY() - location.getY();
        }

        if (location.getY() > getY() + getHeight()) {
            dy = getY() + getHeight() - location.getY();
        }

        if (dx == 0 && dy == 0) {
            return null;
        }

        return new Line(location.getX(), location.getY(), location.getX() + dx, location.getY() + dy);
    }

    public Rectangle merge(Rectangle other) {
        double x1 = Math.min(getX(), other.getX());
        double x2 = Math.max(getX() + getWidth(), other.getX() + other.getWidth());
        double y1 = Math.min(getY(), other.getY());
        double y2 = Math.max(getY() + getHeight(), other.getY() + other.getHeight());
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    public boolean intersects(Rectangle other) {
        final boolean intersectsH = (getX() <= other.getX() + other.getWidth()) && (other.getX() <= getX() + getWidth());
        final boolean intersectsV = (getY() <= other.getY() + other.getHeight()) && (other.getY() <= getY() + getHeight());
        return intersectsH && intersectsV;
    }

    public double middleH() {
        return getX() + getWidth() / 2;
    }

    public double middleV() {
        return getY() + getHeight() / 2;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

}
