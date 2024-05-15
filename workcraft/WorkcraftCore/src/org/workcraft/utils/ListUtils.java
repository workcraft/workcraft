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

}
