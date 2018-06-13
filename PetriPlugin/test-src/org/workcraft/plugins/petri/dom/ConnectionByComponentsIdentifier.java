package org.workcraft.plugins.petri.dom;

import java.util.HashMap;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;

class ConnectionByComponentsIdentifier implements KeyProvider<MathConnection> {

    private final KeyProvider<MathNode> componentKeyProvider;

    class Pair {
        private final Object o1;
        private final Object o2;

        Pair(Object o1, Object o2) {
            this.o1 = o1;
            this.o2 = o2;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + o1.hashCode();
            result = 37 * result + o2.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            Pair other = (Pair) obj;
            return o1.equals(other.o1) && o2.equals(other.o2);
        }
    }

    ConnectionByComponentsIdentifier(
            KeyProvider<MathNode> componentKeyProvider) {
        this.componentKeyProvider = componentKeyProvider;
    }

    @Override
    public Object getKey(MathConnection item) {

        return new Pair(componentKeyProvider.getKey(item.getFirst()),
                componentKeyProvider.getKey(item.getSecond()));
    }

    private static class Finder<T> {
        private final HashMap<Object, T> map;
        private final KeyProvider<T> keyProvider;

        Finder(Iterable<T> items, KeyProvider<T> keyProvider) {
            this.keyProvider = keyProvider;
            map = new HashMap<Object, T>();
            for (T item : items) {
                map.put(keyProvider.getKey(item), item);
            }
        }

        public T getMatching(T item) {
            return map.get(keyProvider.getKey(item));
        }
    }

}
