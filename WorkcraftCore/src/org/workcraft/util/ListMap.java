/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ListMap <K,V> {
    private HashMap<K,LinkedList<V>> map =  new HashMap<K, LinkedList<V>>();

    public void put (K key, V value) {
        LinkedList<V> list = map.get(key);
        if (list == null) {
            list = new LinkedList<V>();
            map.put(key, list);
        }
        list.add(value);
    }

    public void remove(K key, V value) {
        LinkedList<V> list = map.get(key);
        if (list != null)
        {
            list.remove(value);
            if (list.isEmpty())
                map.remove(key);
        }
    }

    public List<V> get(K key) {
        LinkedList<V> list = map.get(key);
        if (list != null)
            return Collections.unmodifiableList(list);
        else
            return Collections.emptyList();
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

    public void clear()
    {
        map.clear();
    }
}
