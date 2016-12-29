package org.workcraft.plugins.petri.dom;

interface KeyProvider<T> {
    Object getKey(T item);
}
