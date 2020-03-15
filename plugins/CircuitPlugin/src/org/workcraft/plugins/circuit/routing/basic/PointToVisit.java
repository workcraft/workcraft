package org.workcraft.plugins.circuit.routing.basic;

public class PointToVisit implements Comparable<PointToVisit> {
    private final double score;
    private final IndexedPoint location;

    public PointToVisit(double score, IndexedPoint location) {
        this.score = score;
        this.location = location;
    }

    @Override
    public int compareTo(PointToVisit other) {
        assert this != other;

        int compare = Double.compare(getScore(), other.getScore());
        if (compare != 0) {
            return compare;
        }

        // compare coordinates to minimize randomness when the score is the same
        return getLocation().compareTo(other.getLocation());

    }

    @Override
    public String toString() {
        return "PointToVisit [score=" + getScore() + ", location=" + getLocation() + "]";
    }

    public double getScore() {
        return score;
    }

    public IndexedPoint getLocation() {
        return location;
    }

}
