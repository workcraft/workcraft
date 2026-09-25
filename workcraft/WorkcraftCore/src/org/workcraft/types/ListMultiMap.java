package org.workcraft.types;

import java.util.*;

public class ListMultiMap<K, V> extends MultiMap<K, V, List<V>> {

    public ListMultiMap() {
        super(LinkedList::new, Collections::unmodifiableList);
    }

}