package org.workcraft.plugins.circuit.routing.basic;

public final class IndexedInterval {

    private final int from;
    private final int to;

    public IndexedInterval(int from, int to) {
        this.from = Math.min(from, to);
        this.to = Math.max(from, to);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + getFrom();
        result = prime * result + getTo();
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
        IndexedInterval other = (IndexedInterval) obj;
        if (getFrom() != other.getFrom()) {
            return false;
        }
        if (getTo() != other.getTo()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IntegerInterval [from=" + getFrom() + ", to=" + getTo() + "]";
    }

    /**
     * Check if this interval intersects some other interval.
     *
     * @param other
     *            the other interval
     * @return true if intervals intersect, false otherwise
     */
    public boolean intersects(IndexedInterval other) {
        return getFrom() <= other.getTo() && getTo() >= other.getFrom();
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
