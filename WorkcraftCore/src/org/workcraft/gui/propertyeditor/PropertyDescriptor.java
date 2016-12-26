package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface PropertyDescriptor {
    String getName();
    Class<?> getType();
    boolean isWritable();
    boolean isCombinable();
    boolean isTemplatable();
    Object getValue() throws InvocationTargetException;
    void setValue(Object value) throws InvocationTargetException;
    Map<? extends Object, String> getChoice();
}
