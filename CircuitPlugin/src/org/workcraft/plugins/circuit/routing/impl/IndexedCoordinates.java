package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.IndexedInterval;
import org.workcraft.plugins.circuit.routing.basic.RouterConstants;

/**
 * This class maps coordinates to integer indexes.
 */
public class IndexedCoordinates {

    private final TreeMap<Double, Coordinate> values = new TreeMap<>();
    private final Collection<Coordinate> readOnlyValues = Collections.unmodifiableCollection(values.values());

    private final SortedMap<Coordinate, Integer> toIndex = new TreeMap<>();
    private Coordinate[] toCoordinate;

    private final Set<Double> publicValues = new HashSet<Double>();

    private boolean isBuilt = false;

    /**
     * Returns the accumulated list of values.
     *
     * @return the accumulated list of values
     */
    public Collection<Coordinate> getValues() {
        return readOnlyValues;
    }

    /**
     * Shows whether value indices are built.
     *
     * @return true if indices are built, false otherwise
     */
    public boolean isBuilt() {
        return isBuilt;
    }

    /**
     * Remove all values.
     */
    public void clear() {
        values.clear();
        publicValues.clear();
        clearMaps();
    }

    /**
     * Remove a single value.
     */
    public void remove(double value) {
        values.remove(value);
        publicValues.remove(value);
        clearMaps();
    }

    private void clearMaps() {
        toIndex.clear();
        toCoordinate = null;
        isBuilt = false;
    }

    /**
     * Add a single value to the mapping.
     *
     * @param value
     *            a single value to be added to the mapping
     */
    private boolean add(Coordinate coordinate) {

        Coordinate oldValue = values.put(coordinate.getValue(), coordinate);

        if (coordinate.isPublic()) {
            publicValues.add(coordinate.getValue());
        }

        return oldValue == null;
    }

    private void addValue(boolean isPublic, CoordinateOrientation orientation, double... values) {
        boolean changed = false;

        for (double value : values) {
            CoordinateOrientation newOrientation = orientation;

            Coordinate oldCoordinate = this.values.get(value);

            if (oldCoordinate != null) {
                newOrientation = orientation.merge(oldCoordinate.getOrientation());
            }

            Coordinate newCoordinate = new Coordinate(newOrientation, isPublic || isPublic(value), value);

            changed |= add(newCoordinate);
        }

        if (changed && isBuilt) {
            clearMaps();
        }
    }

    /**
     * Add public values to the mapping.
     *
     * @param values
     *            a values to be added to the mapping
     */
    public void addPublic(CoordinateOrientation orientation, double... values) {
        addValue(true, orientation, values);
    }

    /**
     * Add given coordinate to the mapping.
     *
     * @param values
     *            a values to be added to the mapping
     * @param times
     *            the number of times to add this coordinate
     */
    public void addCoordinate(Coordinate coordinate, int times) {

        for (int i = 0; i < times; i++) {
            boolean doHigher = coordinate.getOrientation() == CoordinateOrientation.ORIENT_HIGHER
                    || coordinate.getOrientation() == CoordinateOrientation.ORIENT_BOTH;
            boolean doLower = coordinate.getOrientation() == CoordinateOrientation.ORIENT_HIGHER
                    || coordinate.getOrientation() == CoordinateOrientation.ORIENT_BOTH;

            if (doHigher) {
                addValue(coordinate.isPublic(), coordinate.getOrientation(),
                        coordinate.getValue() + i * RouterConstants.MINOR_SNAP);
            }

            if (doLower) {
                addValue(coordinate.isPublic(), coordinate.getOrientation(),
                        coordinate.getValue() - i * RouterConstants.MINOR_SNAP);
            }

        }
    }

    /**
     * Add private values to the mapping.
     *
     * @param values
     *            a values to be added to the mapping
     */
    public void addPrivate(CoordinateOrientation orientation, double... values) {
        addValue(false, orientation, values);
    }

    public int size() {
        return values.size();
    }

    /**
     * Create the value-to-index mapping after all the values were added.
     */
    public void build() {
        if (isBuilt) {
            return;
        }

        clearMaps();

        toCoordinate = new Coordinate[values.size()];
        int idx = 0;
        for (Coordinate coordinate : values.values()) {
            toIndex.put(coordinate, idx);
            toCoordinate[idx] = coordinate;
            idx++;
        }

        isBuilt = true;
    }

    /**
     * Return true if the interval is occupied by some value.
     *
     * @param from
     *            lowest border interval
     * @param to
     *            highest border interval
     * @return true if interval contains a value, false otherwise
     */
    public boolean isIntervalOccupied(double from, double to) {
        assert from <= to : "the interval borders must be provided from lower to higher";

        Double minBorder = values.ceilingKey(from - RouterConstants.EPSILON);
        Double maxBorder = values.floorKey(to + RouterConstants.EPSILON);

        if (minBorder == null || maxBorder == null) {
            return false;
        }

        if (minBorder > maxBorder) {
            return false;
        }

        return true;
    }

    /**
     * Return the indices covered by the given interval.
     *
     * @param from
     *            lowest border interval
     * @param to
     *            highest border interval
     * @return the interval in indexed values. Returns null if the interval is
     *         not covering any indexed values
     */
    public IndexedInterval getIndexedInterval(double from, double to) {
        assert from <= to : "the interval borders must be provided from lower to higher";

        build();

        Double minBorder = values.ceilingKey(from - RouterConstants.EPSILON);
        Double maxBorder = values.floorKey(to + RouterConstants.EPSILON);

        if (minBorder == null || maxBorder == null) {
            return null;
        }

        if (minBorder > maxBorder) {
            return null;
        }

        return new IndexedInterval(toIndex.get(values.get(minBorder)), toIndex.get(values.get(maxBorder)));
    }

    /**
     * Return the indices covered by the given interval. It does not include the
     * interval boundaries.
     *
     * @param from
     *            lowest border interval
     * @param to
     *            highest border interval
     * @return the interval in indexed values. Returns null if the interval is
     *         not covering any indexed values
     */
    public IndexedInterval getIndexedIntervalExclusive(double from, double to) {
        assert from <= to : "the interval borders must be provided from lower to higher";

        build();

        Double minBorder = values.ceilingKey(from + 2 * RouterConstants.EPSILON);
        Double maxBorder = values.floorKey(to - 2 * RouterConstants.EPSILON);

        if (minBorder == null || maxBorder == null) {
            return null;
        }

        if (minBorder > maxBorder) {
            return null;
        }

        return new IndexedInterval(toIndex.get(values.get(minBorder)), toIndex.get(values.get(maxBorder)));
    }

    /**
     * Return registered value by specified index.
     *
     * @param index
     *            the index of value to be returned
     * @return the value registered for specified index
     */
    public double getValueByIndex(int index) {

        return getCoordinateByIndex(index).getValue();
    }

    public double getDistance(int idx1, int idx2) {
        return Math.abs(toCoordinate[idx1].getValue() - toCoordinate[idx2].getValue());
    }

    /**
     * Get coordinate by given index.
     *
     * @param index
     *            the index of the coordinate
     * @return the coordinate by the index provided
     */
    public Coordinate getCoordinateByIndex(int index) {

        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException();
        }

        build();

        return toCoordinate[index];
    }

    public boolean isPublic(double value) {
        return publicValues.contains(value);
    }

    public void mergeCoordinates() {

        Coordinate last = null;
        List<Coordinate> toAdd = new ArrayList<Coordinate>();
        List<Coordinate> toDelete = new ArrayList<Coordinate>();

        for (Coordinate coordinate : getValues()) {
            if (!(coordinate.isPublic())) {
                continue;
            }

            if (coordinate.getOrientation() == CoordinateOrientation.ORIENT_HIGHER
                    || coordinate.getOrientation() == CoordinateOrientation.ORIENT_BOTH) {

                if (last != null && (last.getOrientation() == CoordinateOrientation.ORIENT_HIGHER)) {
                    toDelete.add(last);
                }

                last = coordinate;
                continue;
            }

            if (last != null) {
                if (coordinate.getOrientation() == CoordinateOrientation.ORIENT_LOWER) {
                    toDelete.add(coordinate);
                }

                if (last.getOrientation() == CoordinateOrientation.ORIENT_HIGHER) {
                    toDelete.add(last);

                    if (coordinate.getOrientation() == CoordinateOrientation.ORIENT_LOWER) {

                        double middle = SnapCalculator.snapToClosest((last.getValue() + coordinate.getValue()) / 2,
                                RouterConstants.SEGMENT_MARGIN);

                        if (coordinate.getOrientation() == CoordinateOrientation.ORIENT_BOTH) {
                            middle = coordinate.getValue();
                        }

                        coordinate = new Coordinate(CoordinateOrientation.ORIENT_BOTH, true, middle);

                        toAdd.add(coordinate);
                    }
                }

            }

            last = coordinate;
        }

        for (Coordinate coordinate : toDelete) {
            remove(coordinate.getValue());
        }

        for (Coordinate coordinate : toAdd) {
            add(coordinate);
        }
    }

}
