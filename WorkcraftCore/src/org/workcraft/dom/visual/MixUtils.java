package org.workcraft.dom.visual;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class MixUtils {

    public static <T> T vote(Collection<T> items, Class<T> type, T value) {
        HashMap<T, Integer> typeCount = new HashMap<>();
        for (T item: items) {
            int count = 0;
            if (typeCount.containsKey(item)) {
                count = typeCount.get(item);
            }
            typeCount.put(item, count + 1);
        }
        int maxCount = 0;
        T result = value;
        for (T item: typeCount.keySet()) {
            int count = typeCount.get(item);
            if (count > maxCount) {
                maxCount = count;
                result = item;
            }
        }
        return result;
    }

    public static double average(LinkedList<Double> sizes) {
        double sum = 0.0;
        int count = 0;
        for (double size: sizes) {
            sum += size;
            count++;
        }
        return count > 0 ? sum / count : 0.0;
    }

}
