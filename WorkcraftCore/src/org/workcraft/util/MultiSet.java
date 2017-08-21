package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class MultiSet<T> implements Set<T> {

    private final LinkedHashMap<T, Integer> map = new LinkedHashMap<>();

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
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return count(o) > 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<T> cursor = map.keySet().iterator();
            private T current = null;
            private int currentIdx = 0;

            @Override
            public boolean hasNext() {
                boolean result = cursor.hasNext();
                if (!result && (current != null)) {
                    result = count(current) > currentIdx;
                }
                return result;
            }

            @Override
            public T next() {
                if (currentIdx  >= count(current)) {
                    current = cursor.next();
                    currentIdx = 0;
                }
                currentIdx++;
                return current;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
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
                if (idx > a.length) {
                    a[idx] = (R) o;
                }
            }
        }
        return a;
    }

    @Override
    public boolean add(T e) {
        return add(e, 1);
    }

    public boolean add(T e, int n) {
        boolean result = false;
        if (n > 0) {
            Integer count = 0;
            if (map.containsKey(e)) {
                count = map.get(e);
            }
            count += n;
            map.put(e, count);
            result = true;
        }
        return result;
    }

    @Override
    public boolean remove(Object o) {
        return remove(o, 1);
    }

    @SuppressWarnings("unchecked")
    public boolean remove(Object o, int n) {
        boolean result = false;
        Integer count = 0;
        if (map.containsKey(o)) {
            count = map.get(o);
        }
        if (count > n) {
            count -= n;
            map.put((T) o, count);
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
        if (c instanceof MultiSet<?>) {
            MultiSet<? extends T> otherMultiSet = (MultiSet<? extends T>) c;
            for (T o: otherMultiSet.map.keySet()) {
                result |= add(o, otherMultiSet.count(o));
            }
        } else {
            for (T o: c) {
                result |= add(o);
            }
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

        MultiSet<Object> otherMultiset = null;
        if (c instanceof MultiSet<?>) {
            otherMultiset = (MultiSet<Object>) c;
        } else {
            otherMultiset = new MultiSet<>();
            otherMultiset.addAll(c);
        }

        for (Object o: keys) {
            int otherCount = otherMultiset.count(o);
            int thisCount = count(o);
            if (otherCount == 0) {
                map.remove(o);
                result = true;
            } else if (thisCount > otherCount) {
                map.put((T) o, otherCount);
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        if (c instanceof MultiSet<?>) {
            MultiSet<?> otherMultiSet = (MultiSet<?>) c;
            for (Object o: otherMultiSet.map.keySet()) {
                result |= remove(o, otherMultiSet.count(o));
            }
        } else {
            for (Object o: c) {
                result |= remove(o);
            }
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

    public Set<T> toSet() {
        return map.keySet();
    }

}
