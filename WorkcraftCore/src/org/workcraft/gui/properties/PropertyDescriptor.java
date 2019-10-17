package org.workcraft.gui.properties;

import java.util.Map;

public interface PropertyDescriptor<V> {
    String getName();
    Class<V> getType();
    V getValue();
    void setValue(V value);
    Map<V, String> getChoice();
    boolean isEditable();
    boolean isVisible();
    boolean isCombinable();
    boolean isTemplatable();
    boolean isSpan();
}
