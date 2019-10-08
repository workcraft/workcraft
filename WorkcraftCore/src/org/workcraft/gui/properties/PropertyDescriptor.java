package org.workcraft.gui.properties;

import java.util.Map;

public interface PropertyDescriptor<V> {
    String getName();
    Class<V> getType();
    V getValue();
    void setValue(V value);
    Map<V, String> getChoice();

    default boolean isEditable() {
        return true;
    }

    default boolean isVisible() {
        return true;
    }

    default boolean isCombinable() {
        return false;
    }

    default boolean isTemplatable() {
        return false;
    }

    default boolean isSpan() {
        return false;
    }

}
