package org.workcraft.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    public static <T> List<T> zip(List<T> aList, List<T> bList) {
        return zip(aList, bList, 1);
    }

    public static <T> List<T> zip(List<T> aList, List<T> bList, int sliceSize) {
        List<T> result = new ArrayList<>();
        if (sliceSize < 1) {
            sliceSize = 1;
        }
        int aSize = aList.size();
        int bSize = bList.size();
        int aFromIndex = 0;
        int bFromIndex = 0;
        while ((aFromIndex < aSize) && (bFromIndex < bSize)) {
            int aToIndex = Math.min(aFromIndex + sliceSize, aSize);
            int bToIndex = Math.min(bFromIndex + sliceSize, bSize);
            result.addAll(aList.subList(aFromIndex, aToIndex));
            result.addAll(bList.subList(bFromIndex, bToIndex));
            aFromIndex = aToIndex;
            bFromIndex = bToIndex;
        }
        result.addAll(aList.subList(aFromIndex, aSize));
        result.addAll(bList.subList(bFromIndex, bSize));
        return result;
    }

    public static <T> List<List<T>> permutate(List<T> list) {
        List<List<T>> result = new ArrayList<>();
        if (list.isEmpty()) {
            result.add(new ArrayList<>());
        } else {
            T firstElement = list.get(0);
            List<T> remainingList = list.subList(1, list.size());
            List<List<T>> permutations = permutate(remainingList);
            for (List<T> permutation : permutations) {
                for (int index = 0; index <= permutation.size(); index++) {
                    List<T> tmp = new ArrayList<>(permutation);
                    tmp.add(index, firstElement);
                    result.add(tmp);
                }
            }
        }
        return result;
    }

    public static <T> List<List<T>> combine(List<T> values, int itemCount) {
        int combinationCount = 1;
        int valueCount = values.size();
        for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
            if (combinationCount > (Integer.MAX_VALUE / valueCount)) {
                throw new RuntimeException("Too many combinations of " + valueCount + " values in " + itemCount + " positions");
            }
            combinationCount *= valueCount;
        }
        List<List<T>> result = new ArrayList<>(combinationCount);
        for (int combinationIndex = 0; combinationIndex < combinationCount; combinationIndex++) {
            result.add(combinationIndex, combine(values, itemCount, combinationIndex));
        }
        return result;
    }

    public static <T> List<T> combine(List<T> values,  int itemCount, int combinationIndex) {
        int valueCount = values.size();
        List<T> result = new ArrayList<>(itemCount);
        for (int index = 0; index < itemCount; index++) {
            int valueIndex = combinationIndex % valueCount;
            result.add(index, values.get(valueIndex));
            combinationIndex /= valueCount;
        }
        return result;
    }

}
