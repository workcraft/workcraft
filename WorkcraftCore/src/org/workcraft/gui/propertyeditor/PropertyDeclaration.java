package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class PropertyDeclaration<O, V> implements PropertyDescriptor, Disableable {
    private final O object;
    private final String name;
    private final Class<V> cls;
    private final boolean writable;
    private final boolean combinable;
    private final boolean templatable;

    public PropertyDeclaration(O object, String name, Class<V> cls, boolean writable, boolean combinable, boolean templatable) {
        this.object = object;
        this.name = name;
        this.cls = cls;
        this.writable = writable;
        this.combinable = combinable;
        this.templatable = templatable;
    }

    protected abstract void setter(O object, V value);

    protected abstract V getter(O object);

    @Override
    public Map<V, String> getChoice() {
        LinkedHashMap<V, String> result = null;
        if (cls.isEnum()) {
            result = new LinkedHashMap<V, String>();
            for (V item : cls.getEnumConstants()) {
                result.put(item, item.toString());
            }
        }
        return result;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return getter(object);
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        try {
            setter(object, cls.cast(value));
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<V> getType() {
        return cls;
    }

    @Override
    public boolean isWritable() {
        return writable;
    }

    @Override
    public boolean isCombinable() {
        return combinable;
    }

    @Override
    public boolean isTemplatable() {
        return templatable;
    }

    public O getObject() {
        return object;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}
