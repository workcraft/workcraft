package org.workcraft.types;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.workcraft.exceptions.ArgumentException;

public class GeneralTwoWayMap<S, T> {

    private final Map<S, T> from1to2;
    private final Map<T, S> from2to1;

    public GeneralTwoWayMap(Map<S, T> map1, Map<T, S> map2) {
        if (!map1.isEmpty() || !map2.isEmpty()) {
            throw new ArgumentException("maps should be empty");
        }
        this.from1to2 = map1;
        this.from2to1 = map2;
    }

    public void clear() {
        from1to2.clear();
        from2to1.clear();
    }

    public void put(S first, T second) {
        removeKey(first);
        removeValue(second);
        if (first == null || second == null) {
            throw new NullPointerException();
        }
        from1to2.put(first, second);
        from2to1.put(second, first);
    }

    public Set<S> keys() {
        return from1to2.keySet();
    }

    public Set<T> values() {
        return from2to1.keySet();
    }

    public T getValue(S first) {
        return from1to2.get(first);
    }

    public S getKey(T second) {
        return from2to1.get(second);
    }

    public void removeKey(S first) {
        T second = getValue(first);
        if (second != null) {
            remove(first, second);
        }
    }

    public void removeValue(T second) {
        S first = getKey(second);
        if (first != null) {
            remove(first, second);
        }
    }

    private void remove(S first, T second) {
        from1to2.remove(first);
        from2to1.remove(second);
    }

    public boolean isEmpty() {
        return from1to2.isEmpty();
    }

    public boolean containsKey(S key) {
        return from1to2.containsKey(key);
    }
    public boolean containsValue(T value) {
        return from2to1.containsKey(value);
    }

    public Set<S> getKeys() {
        return from1to2.keySet();
    }

    public Set<T> getValues() {
        return from2to1.keySet();
    }

    public Set<Entry<S, T>> entrySet() {
        return from1to2.entrySet();
    }
}
