package org.workcraft.dom.references;

import java.util.Comparator;
import java.util.TreeSet;

import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.util.Pair;

public class IDGenerator {
    private static final Comparator<? super Pair<Integer, Integer>> comparator = new Comparator<Pair<Integer, Integer>>() {
                @Override
                public int compare(Pair<Integer, Integer> o1,
                        Pair<Integer, Integer> o2) {
                    return o1.getFirst().compareTo(o2.getFirst());
                }
            };

    TreeSet<Pair<Integer, Integer>> takenRanges = new TreeSet<>(comparator);

    public void reserveID(int id) {
        final Pair<Integer, Integer> point = emptyRange(id);
        final Pair<Integer, Integer> floor = takenRanges.floor(point);
        final Pair<Integer, Integer> ceiling = takenRanges.ceiling(point);

        if (floor != null && id < floor.getSecond()) {
            throw new DuplicateIDException(id);
        }
        if (ceiling != null && id == ceiling.getFirst().intValue()) {
            throw new DuplicateIDException(id);
        }

        boolean mergeFirst = floor != null && id == floor.getSecond();
        boolean mergeSecond = ceiling != null && id == ceiling.getFirst() - 1;

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
        final int result = (takenRanges.size() > 0 && takenRanges.first().getFirst().intValue() == 0) ? takenRanges.first().getSecond().intValue() : 0;
        reserveID(result);
        return result;
    }

    public boolean isEmpty() {
        return takenRanges.isEmpty();
    }
}
