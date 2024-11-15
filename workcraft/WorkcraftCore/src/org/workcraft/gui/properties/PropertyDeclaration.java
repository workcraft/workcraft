package org.workcraft.gui.properties;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PropertyDeclaration<V> implements PropertyDescriptor<V> {

    private final String name;
    private final Class<V> cls;
    private final Consumer<V> setter;
    private final Supplier<V> getter;
    private Supplier<Boolean> visibilitySupplier = null;
    private boolean editable = true;
    private boolean combinable = false;
    private boolean templatable = false;
    private boolean span = false;

    public PropertyDeclaration(Class<V> cls, String name, Consumer<V> setter, Supplier<V> getter) {
        this.name = name;
        this.cls = cls;
        this.setter = setter;
        this.getter = getter;
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
    public String getName() {
        return name;
    }

    @Override
    public Class<V> getType() {
        return cls;
    }

    @Override
    public V getValue() {
        return getter.get();
    }

    @Override
    public void setValue(V value) {
        setter.accept(value);
    }

    public PropertyDeclaration<V> setVisibilitySupplier(Supplier<Boolean> value) {
        visibilitySupplier = value;
        return this;
    }

    public PropertyDeclaration<V> setReadonly() {
        return setEditable(false);
    }

    public PropertyDeclaration<V> setEditable(boolean value) {
        this.editable = value;
        return this;
    }

    @Override
    public boolean isVisible() {
        return (visibilitySupplier == null) || visibilitySupplier.get();
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    public PropertyDeclaration<V> setCombinable() {
        this.combinable = true;
        return this;
    }

    @Override
    public boolean isCombinable() {
        return combinable;
    }

    public PropertyDeclaration<V> setTemplatable() {
        this.templatable = true;
        return this;
    }

    @Override
    public boolean isTemplatable() {
        return templatable;
    }

    public PropertyDeclaration<V> setSpan() {
        this.span = true;
        return this;
    }

    @Override
    public boolean isSpan() {
        return span;
    }

}
