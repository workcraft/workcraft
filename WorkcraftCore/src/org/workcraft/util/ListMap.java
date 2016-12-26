package org.workcraft.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ListMap<K, V> {
    private final HashMap<K, LinkedList<V>> map = new HashMap<>();

    public void put(K key, V value) {
        LinkedList<V> list = map.get(key);
        if (list == null) {
            list = new LinkedList<V>();
            map.put(key, list);
        }
        list.add(value);
    }

    public void remove(K key, V value) {
        LinkedList<V> list = map.get(key);
        if (list != null) {
            list.remove(value);
            if (list.isEmpty()) {
                map.remove(key);
            }
        }
    }

    public List<V> get(K key) {
        LinkedList<V> list = map.get(key);
        if (list != null) {
            return Collections.unmodifiableList(list);
        } else {
            return Collections.emptyList();
        }
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Collection<LinkedList<V>> values() {
        return map.values();
    }

    public void clear() {
        map.clear();
    }
}
