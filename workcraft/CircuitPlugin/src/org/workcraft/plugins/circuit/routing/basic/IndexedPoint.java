package org.workcraft.plugins.circuit.routing.basic;

public final class IndexedPoint implements Comparable<IndexedPoint> {
    private static final int CACHE_SIZE = 100;
    private static final int LOW_BITS = 15;
    private static IndexedPoint[][] pointsCache = new IndexedPoint[CACHE_SIZE][CACHE_SIZE];

    private final int x;
    private final int y;
    private final int hash;

    private IndexedPoint(int x, int y) {
        if (x < 0 || x >= (1 << LOW_BITS) || y < 0 || y >= (1 << LOW_BITS)) {
            throw new IllegalArgumentException("x or y are outside acceptable boundaries");
        }
        this.x = x;
        this.y = y;
        hash = (x << LOW_BITS) + y;
    }

    /**
     * Returns indexed point. For small x and y values, the cached version is
     * re-used.
     *
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return indexed point
     */
    public static IndexedPoint create(int x, int y) {
        if (x < 0 || x >= CACHE_SIZE || y < 0 || y >= CACHE_SIZE) {
            return new IndexedPoint(x, y);
        }
        if (pointsCache[x][y] == null) {
            pointsCache[x][y] = new IndexedPoint(x, y);
        }
        return pointsCache[x][y];
    }

    @Override
    public int hashCode() {
        return getHash();
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
        IndexedPoint other = (IndexedPoint) obj;
        return getHash() == other.getHash();
    }

    @Override
    public String toString() {
        return "IndexedPoint [x=" + getX() + ", y=" + getY() + "]";
    }

    @Override
    public int compareTo(IndexedPoint other) {
        return getHash() - other.getHash();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHash() {
        return hash;
    }

}
