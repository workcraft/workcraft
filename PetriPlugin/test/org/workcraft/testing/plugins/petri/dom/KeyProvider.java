package org.workcraft.testing.plugins.petri.dom;

interface KeyProvider<T> {
    Object getKey(T item);
}
