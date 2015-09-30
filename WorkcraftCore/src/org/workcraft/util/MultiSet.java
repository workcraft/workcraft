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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MultiSet<T> implements Set<T> {
	private final HashMap<T, Integer> map = new HashMap<>();

	@Override
	public int size() {
		int result = 0;
		for (T o: map.keySet()) {
			result += count(o);
		}
		return result;
	}

	@Override
	public boolean isEmpty() {
		return (size() == 0);
	}

	@Override
	public boolean contains(Object o) {
		return (count(o) > 0);
	}

	@Override
	public Iterator<T> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public Object[] toArray() {
		ArrayList<T> result = new ArrayList<>();
		for (T o: map.keySet()) {
			for (int i = 0; i < count(o); i++) {
				result.add(o);
			}
		}
		return result.toArray(new Object[result.size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R[] toArray(R[] a) {
		int idx = 0;
		for (T o: map.keySet()) {
			for (int i = 0; i < count(o); i++) {
				if (idx > a.length)
				a[idx] = (R)o;
			}
		}
		return a;
	}

	@Override
	public boolean add(T e) {
		Integer count = 0;
		if (map.containsKey(e)) {
			count = map.get(e);
		}
		count++;
		map.put(e, count);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		boolean result = false;
		Integer count = 0;
		if (map.containsKey(o)) {
			count = map.get(o);
		}
		if (count > 1) {
			count--;
			map.put((T)o, count);
			result = true;
		} else {
			map.remove(o);
		}
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		boolean result = true;
		for (Object o: c) {
			result &= map.containsKey(o);
		}
		return result;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean result = false;
		for (T o: c) {
			result |= add(o);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = false;
		HashSet<Object> keys = new HashSet<>();
		keys.addAll(map.keySet());
		keys.addAll(c);
		MultiSet<Object> otherMultiset = new MultiSet<>();
		otherMultiset.addAll(c);
		for (Object o: keys) {
			int otherCount = otherMultiset.count(o);
			int thisCount = count(o);
			if (otherCount == 0) {
				map.remove(o);
				result = true;
			} else if (thisCount > otherCount) {
				map.put((T)o, otherCount);
				result = true;
			}
		}
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (Object o: c) {
			result |= remove(o);
		}
		return result;
	}

	@Override
	public void clear() {
		map.clear();
	}

	public int count(Object o) {
		int result = 0;
		if (map.containsKey(o)) {
			result = map.get(o);
		}
		return result;
	}

}