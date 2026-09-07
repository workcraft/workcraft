package org.workcraft.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListPermutationIterator<T> implements Iterator<List<T>> {

    // Iterator over permutations implements Even's variant of Steinhaus–Johnson–Trotter algorithm
    // (https://en.wikipedia.org/wiki/Steinhaus%E2%80%93Johnson%E2%80%93Trotter_algorithm).
    // Code is based on this StackOverflow answer https://stackoverflow.com/a/11916946
    static class IndexPermutationIterator implements Iterator<int[]> {
        private final int size;
        private int[] currentPermutation;
        private int[] nextPermutation;
        private int[] moveDirections;

        IndexPermutationIterator(int size) {
            this.size = size;
            if (this.size > 0) {
                currentPermutation = new int[this.size];
                moveDirections = new int[this.size];
                for (int i = 0; i < this.size; i++) {
                    currentPermutation[i] = i;
                    moveDirections[i] = -1;
                }
                moveDirections[0] = 0;
            }
            nextPermutation = currentPermutation;
        }

        @Override
        public int[] next() {
            int[] result = calcNextPermutation();
            nextPermutation = null;
            return result;
        }

        @Override
        public boolean hasNext() {
            return (calcNextPermutation() != null);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private int[] calcNextPermutation() {
            if (nextPermutation != null) {
                return nextPermutation;
            }

            if (currentPermutation == null) {
                return null;
            }

            // Find the largest element with non-zero direction
            int i = -1;
            int e = -1;
            for (int j = 0; j < size; j++) {
                if ((moveDirections[j] != 0) && (currentPermutation[j] > e)) {
                    e = currentPermutation[j];
                    i = j;
                }
            }
            if (i == -1) {
                currentPermutation = null;
                moveDirections = null;
                return null;
            }
            // Swap with the element in its direction
            int k = i + moveDirections[i];
            swap(i, k, moveDirections);
            swap(i, k, currentPermutation);

            // If it is first or last element, or if the next element in the direction is greater, then reset its direction
            if ((k == 0) || (k == size - 1) || (currentPermutation[k + moveDirections[k]] > e)) {
                moveDirections[k] = 0;
            }

            // Set directions to all greater elements
            for (int j = 0; j < size; j++) {
                if (currentPermutation[j] > e) {
                    moveDirections[j] = (j < k) ? +1 : -1;
                }
            }
            nextPermutation = currentPermutation;
            return nextPermutation;
        }

        private static void swap(int i, int j, int[] arr) {
            int v = arr[i];
            arr[i] = arr[j];
            arr[j] = v;
        }
    }

    private final List<T> list;
    private final IndexPermutationIterator indexPermutationIterator;
    private final List<T> nextPermutationList;
    private boolean needsEmptyListPermutation;

    public ListPermutationIterator(List<T> list, boolean reusePermutationList) {
        this.list = list;
        this.indexPermutationIterator = new IndexPermutationIterator(list.size());
        this.nextPermutationList = reusePermutationList ? new ArrayList<>() : null;
        needsEmptyListPermutation = list.isEmpty();
    }

    @Override
    public List<T> next() {
        if (needsEmptyListPermutation) {
            needsEmptyListPermutation = false;
            return preparePermutationList();
        }
        int[] permutationIndexes = indexPermutationIterator.next();
        if (permutationIndexes == null) {
            return null;
        }
        List<T> result = preparePermutationList();
        for (int index = 0; index < list.size(); index++) {
            int permutationIndex = permutationIndexes[index];
            result.add(list.get(permutationIndex));
        }
        return result;
    }

    private List<T> preparePermutationList() {
        List<T> result = nextPermutationList;
        if (result == null) {
            result = new ArrayList<>();
        } else {
            result.clear();
        }
        return result;
    }

    @Override
    public boolean hasNext() {
        return needsEmptyListPermutation || indexPermutationIterator.hasNext();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
