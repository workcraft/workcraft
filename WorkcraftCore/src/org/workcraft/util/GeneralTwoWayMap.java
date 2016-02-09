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

import java.util.Map;
import java.util.Set;

import org.workcraft.exceptions.ArgumentException;

public class GeneralTwoWayMap<T1,T2> {
    public GeneralTwoWayMap(Map<T1, T2> map1, Map<T2, T1> map2)
    {
        if(!map1.isEmpty() || !map2.isEmpty())
            throw new ArgumentException("maps should be empty");
        this.from1to2 = map1;
        this.from2to1 = map2;
    }
    final Map<T1, T2> from1to2;
    final Map<T2, T1> from2to1;
    public void put(T1 first, T2 second)
    {
        removeKey(first);
        removeValue(second);
        if (first == null || second == null)
            throw new NullPointerException();
        from1to2.put(first, second);
        from2to1.put(second, first);
    }
    public Set<T1> keys()
    {
        return from1to2.keySet();
    }
    public Set<T2> values()
    {
        return from2to1.keySet();
    }
    public T2 getValue(T1 first) {
        return from1to2.get(first);
    }
    public T1 getKey(T2 second) {
        return from2to1.get(second);
    }
    public void removeKey(T1 first)
    {
        T2 second = getValue(first);
        if(second != null)
            remove(first,second);
    }
    public void removeValue(T2 second)
    {
        T1 first = getKey(second);
        if(first != null)
            remove(first,second);
    }
    private void remove(T1 first, T2 second) {
        from1to2.remove(first);
        from2to1.remove(second);
    }
    public boolean isEmpty() {
        return from1to2.isEmpty();
    }
    public boolean containsKey(T1 key) {
        return from1to2.containsKey(key);
    }
    public boolean containsValue(T2 value) {
        return from2to1.containsKey(value);
    }

    public Set<T1> getKeys() {
        return from1to2.keySet();
    }

    public Set<T2> getValues() {
        return from2to1.keySet();
    }

}
