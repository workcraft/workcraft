package org.workcraft.dom.visual;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.util.Func;

public class NodeHelper {

    @SuppressWarnings("unchecked")
    public static <T, O> Collection<T> filterByType(Collection<O> original, Class<T> type) {
        ArrayList<T> result = new ArrayList<>();
        for (Object node : original) {
            if (type.isInstance(node)) {
                result.add((T) node);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T, O> Collection<T> filterByType(Collection<O> original, Class<T> type, Func<T, Boolean> filter) {
        ArrayList<T> result = new ArrayList<>();
        for (Object node : original) {
            if (type.isInstance(node) && filter.eval((T) node)) {
                result.add((T) node);
            }
        }
        return result;
    }
}
