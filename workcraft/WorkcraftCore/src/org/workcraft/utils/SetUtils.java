package org.workcraft.utils;

import org.workcraft.types.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class SetUtils {

    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>();
        if ((set1 != null) && (set2 != null)) {
            result.addAll(set1);
            result.retainAll(set2);
        }
        return result;
    }

    public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>();
        if (set1 != null) {
            result.addAll(set1);
        }
        if (set2 != null) {
            result.addAll(set2);
        }
        return result;
    }

    public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>();
        if (set1 != null) {
            result.addAll(set1);
        }
        if (set2 != null) {
            result.removeAll(set2);
        }
        return result;
    }

    public static <T> Set<T> symmetricDifference(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>();
        Set<T> tmp = new HashSet<>();
        if (set1 != null) {
            result.addAll(set1);
            tmp.addAll(set1);
        }
        if (set2 != null) {
            result.addAll(set2);
            tmp.retainAll(set2);
        }
        result.removeAll(tmp);
        return result;
    }

    public static <T> boolean isFirstSmaller(HashSet<T> set1, HashSet<T> set2, boolean equalWins) {
        if (set2.containsAll(set1)) {
            if (set2.size() > set1.size()) return true;
            return equalWins;
        }
        return false;
    }

    public static <T> Set<Set<T>> convertArraysToSets(T[][] w) {
        Set<Set<T>> result = new HashSet<>();
        for (T[] v : w) {
            result.add(new HashSet<>(Arrays.asList(v)));
        }
        return result;
    }

    public static <T> Pair<Set<T>, Set<T>> partition(Collection<T> items, Predicate<T> predicate) {
        Set<T> trueItems = new HashSet<>();
        Set<T> falseItems = new HashSet<>();
        for (T item : items) {
            Set<T> partitionItems = predicate.test(item) ? trueItems : falseItems;
            partitionItems.add(item);
        }
        return Pair.of(trueItems, falseItems);
    }

}
