package org.workcraft.types;

import java.util.*;
import java.util.stream.Collectors;

public class ListMap<K, V> {

    private final HashMap<K, LinkedList<V>> map = new HashMap<>();

    public void put(K key, V value) {
        LinkedList<V> list = map.get(key);
        if (list == null) {
            list = new LinkedList<>();
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

    public Collection<V> values() {
        return map.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void clear() {
        map.clear();
    }

}
