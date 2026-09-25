package org.workcraft.types;

import java.util.*;

public class SetMultiMap<K, V> extends MultiMap<K, V, Set<V>> {

    public SetMultiMap() {
        super(HashSet::new, Collections::unmodifiableSet);
    }

}