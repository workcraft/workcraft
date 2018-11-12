package org.workcraft.gui.properties;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class PropertyDeclaration<O, V> implements PropertyDescriptor<V> {
    private final O object;
    private final String name;
    private final Class<V> cls;
    private final boolean combinable;
    private final boolean templatable;

    public PropertyDeclaration(O object, String name, Class<V> cls) {
        this(object, name, cls, false, false);
    }

    public PropertyDeclaration(O object, String name, Class<V> cls, boolean combinable, boolean templatable) {
        this.object = object;
        this.name = name;
        this.cls = cls;
        this.combinable = combinable;
        this.templatable = templatable;
    }

    @Override
    public Map<V, String> getChoice() {
        LinkedHashMap<V, String> result = null;
        if (cls.isEnum()) {
            result = new LinkedHashMap<>();
            for (V item : cls.getEnumConstants()) {
                result.put(item, item.toString());
            }
        }
        return result;
    }

    @Override
    public final V getValue() {
        return getter(object);
    }

    @Override
    public final void setValue(V value) {
        setter(object, value);
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Class<V> getType() {
        return cls;
    }

    @Override
    public final boolean isCombinable() {
        return combinable;
    }

    @Override
    public final boolean isTemplatable() {
        return templatable;
    }

    public abstract void setter(O object, V value);

    public abstract V getter(O object);

}
