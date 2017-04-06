package org.workcraft.plugins.circuit.routing.basic;

/**
 * Class representing a separate port used as a start or end of any route.
 */
public final class RouterPort {
    /** Direction of the port. */
    private final PortDirection direction;
    /** Location of the port. */
    private final Point location;

    /**
     * if true, the router will add route segment in the defined direction
     * before making any turns. If false, the router can choose any neighboring
     * direction on the first route segment.
     */
    public final boolean isFixedDirection;

    private RouterPort(PortDirection direction, Point location, boolean isFixedDirection) {
        if (direction == null || location == null) {
            throw new IllegalArgumentException();
        }
        this.direction = direction;
        this.location = location;
        this.isFixedDirection = isFixedDirection;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((getDirection() == null) ? 0 : getDirection().hashCode());
        result = prime * result + (isFixedDirection ? 1231 : 1237);
        result = prime * result + ((getLocation() == null) ? 0 : getLocation().hashCode());
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
        RouterPort other = (RouterPort) obj;
        if (getDirection() != other.getDirection()) {
            return false;
        }
        if (isFixedDirection != other.isFixedDirection) {
            return false;
        }
        if (getLocation() == null) {
            if (other.getLocation() != null) {
                return false;
            }
        } else if (!getLocation().equals(other.getLocation())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Port [direction=" + getDirection() + ", location=" + getLocation() + ", "
                + (isFixedDirection ? "directed" : "undirected") + "]";
    }

    public static RouterPort withFixedDirection(PortDirection direction, Point portPoint) {
        return new RouterPort(direction, portPoint, true);
    }

    public static RouterPort withFlexibleDirection(PortDirection direction, Point portPoint) {
        return new RouterPort(direction, portPoint, false);
    }

    public PortDirection getDirection() {
        return direction;
    }

    public Point getLocation() {
        return location;
    }

}
