package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;

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

    public static double average(Collection<Double> sizes) {
        double sum = 0.0;
        int count = sizes.size();
        for (double size: sizes) {
            sum += size;
        }
        return count > 0 ? sum / count : 0.0;
    }

    public static Point2D middlePoint(Collection<Point2D> points) {
        double x = 0.0;
        double y = 0.0;
        int count = points.size();
        for (Point2D point: points) {
            x += point.getX();
            y += point.getY();
        }
        return (count > 0) ? new Point2D.Double(x / count, y / count) : null;
    }

}
