package org.workcraft.types;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class MultiMap<K, V, C extends Collection<V>> {

    private final Map<K, C> map = new HashMap<>();
    private final Supplier<C> collectionFactory;
    private final UnaryOperator<C> unmodifiableWrapper;

    protected MultiMap(Supplier<C> collectionFactory, UnaryOperator<C> unmodifiableWrapper) {
        this.collectionFactory = collectionFactory;
        this.unmodifiableWrapper = unmodifiableWrapper;
    }

    public void put(K key, V value) {
        map.computeIfAbsent(key, k -> collectionFactory.get()).add(value);
    }

    public void remove(K key, V value) {
        C values = map.get(key);
        if (values != null) {
            values.remove(value);
            if (values.isEmpty()) {
                map.remove(key);
            }
        }
    }

    public C get(K key) {
        C values = map.get(key);
        return values == null ? collectionFactory.get() : unmodifiableWrapper.apply(values);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        map.clear();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Set<V> valueSet() {
        Set<V> result = new HashSet<>();
        for (C values : map.values()) {
            result.addAll(values);
        }
        return result;
    }

}