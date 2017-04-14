package org.workcraft.plugins.circuit.routing.basic;

/**
 * The connection class between two ports, defines one of the routing tasks.
 */
public final class RouterConnection implements Comparable<RouterConnection> {
    private final RouterPort source;
    private final RouterPort destination;

    public RouterConnection(RouterPort source, RouterPort destination) {

        assert source != null : "cannot register connection with unknown source";
        assert destination != null : "cannot register connection with unknown destination";

        this.source = source;
        this.destination = destination;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((getDestination() == null) ? 0 : getDestination().hashCode());
        result = prime * result + ((getSource() == null) ? 0 : getSource().hashCode());
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
        RouterConnection other = (RouterConnection) obj;
        if (getDestination() == null) {
            if (other.getDestination() != null) {
                return false;
            }
        } else if (!getDestination().equals(other.getDestination())) {
            return false;
        }
        if (getSource() == null) {
            if (other.getSource() != null) {
                return false;
            }
        } else if (!getSource().equals(other.getSource())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Connection [source=" + getSource() + ", destination=" + getDestination() + "]";
    }

    public RouterPort getSource() {
        return source;
    }

    public RouterPort getDestination() {
        return destination;
    }

    @Override
    public int compareTo(RouterConnection other) {
        double dist = Math.abs(source.getLocation().getX() - destination.getLocation().getX())
                + Math.abs(source.getLocation().getY() - destination.getLocation().getY());

        double otherDist = Math.abs(other.source.getLocation().getX() - other.destination.getLocation().getX())
                + Math.abs(other.source.getLocation().getY() - other.destination.getLocation().getY());

        int compare = Double.compare(dist, otherDist);
        if (compare == 0) {
            compare = source.getLocation().compareTo(other.getSource().getLocation());
        }
        if (compare == 0) {
            compare = destination.getLocation().compareTo(other.getDestination().getLocation());
        }
        return compare;
    }

}
