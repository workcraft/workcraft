package org.workcraft.gui.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface PropertyDescriptor {
    String getName();
    Class<?> getType();
    Object getValue() throws InvocationTargetException;
    void setValue(Object value) throws InvocationTargetException;
    Map<? extends Object, String> getChoice();

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
}
