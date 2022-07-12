package org.workcraft.dom.references;

import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.types.Pair;

import java.util.Comparator;
import java.util.TreeSet;

public class IDGenerator {

    private final TreeSet<Pair<Integer, Integer>> takenRanges = new TreeSet<>(Comparator.comparing(Pair::getFirst));

    public void reserveID(int id) {
        final Pair<Integer, Integer> point = emptyRange(id);
        final Pair<Integer, Integer> floor = takenRanges.floor(point);
        if ((floor != null) && (id < floor.getSecond())) {
            throw new DuplicateIDException(id);
        }

        final Pair<Integer, Integer> ceiling = takenRanges.ceiling(point);
        if ((ceiling != null) && (id == ceiling.getFirst())) {
            throw new DuplicateIDException(id);
        }

        boolean mergeFirst = (floor != null) && (id == floor.getSecond());
        boolean mergeSecond = (ceiling != null) && (id == ceiling.getFirst() - 1);

        if (mergeFirst) {
            takenRanges.remove(floor);
        }
        if (mergeSecond) {
            takenRanges.remove(ceiling);
        }

        int left = id;
        int right = id + 1;

        if (mergeFirst) {
            left = floor.getFirst();
        }
        if (mergeSecond) {
            right = ceiling.getSecond();
        }

        takenRanges.add(Pair.of(left, right));
    }

    public void releaseID(int id) {
        final Pair<Integer, Integer> range = takenRanges.floor(emptyRange(id));

        if (range == null || range.getSecond() <= id) {
            throw new RuntimeException("ID is above the range");
        }
        if (range.getFirst() > id) {
            throw new RuntimeException("ID is below the range");
        }

        Pair<Integer, Integer> r1 = Pair.of(range.getFirst(), id);
        Pair<Integer, Integer> r2 = Pair.of(id + 1, range.getSecond());

        takenRanges.remove(range);
        tryAddRange(r1);
        tryAddRange(r2);
    }

    private Pair<Integer, Integer> emptyRange(Integer id) {
        return Pair.of(id, id);
    }

    private void tryAddRange(Pair<Integer, Integer> r2) {
        if (takenRanges.floor(emptyRange(r2.getFirst())) != takenRanges.floor(emptyRange(r2.getSecond()))) {
            throw new RuntimeException("taken range error");
        }

        if (!r2.getSecond().equals(r2.getFirst())) {
            takenRanges.add(r2);
        }
    }

    public int getNextID() {
        boolean b = !takenRanges.isEmpty() && (takenRanges.first().getFirst() == 0);
        int result = b ? takenRanges.first().getSecond() : 0;
        reserveID(result);
        return result;
    }

    public boolean isEmpty() {
        return takenRanges.isEmpty();
    }

}
