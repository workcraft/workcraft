package org.workcraft.gui.properties;

import java.util.Map;
import java.util.Set;

public final class PropertyCombiner implements PropertyDescriptor {
    private final String name;
    private final Class<?> type;
    private final Set<PropertyDescriptor> values;

    public PropertyCombiner(String name, Class<?> type, Set<PropertyDescriptor> values) {
        this.name = name;
        this.type = type;
        this.values = values;
    }

    @Override
    public Object getValue() {
        Object result = null;
        for (PropertyDescriptor descriptor: values) {
            if (descriptor.isVisible()) {
                if (result == null) {
                    result = descriptor.getValue();
                } else if (!result.equals(descriptor.getValue())) {
                    return null;
                }
            }
        }
        return result;
    }

    @Override
    public void setValue(Object value) {
        for (PropertyDescriptor descriptor : values) {
            if (descriptor.isVisible()) {
                descriptor.setValue(value);
            }
        }
    }

    @Override
    public Map<?, String> getChoice() {
        Map<?, String> result = null;
        for (PropertyDescriptor descriptor: values) {
            result = descriptor.getChoice();
        }
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean isEditable() {
        return values.stream().map(PropertyDescriptor::isEditable).reduce(true, Boolean::logicalAnd);
    }

    @Override
    public boolean isVisible() {
        return values.stream().map(PropertyDescriptor::isVisible).reduce(false, Boolean::logicalOr);
    }

    @Override
    public boolean isCombinable() {
        return values.stream().map(PropertyDescriptor::isCombinable).reduce(true, Boolean::logicalAnd);
    }

    @Override
    public boolean isTemplatable() {
        return values.stream().map(PropertyDescriptor::isTemplatable).reduce(true, Boolean::logicalAnd);
    }

    @Override
    public boolean isSpan() {
        return values.stream().map(PropertyDescriptor::isSpan).reduce(true, Boolean::logicalAnd);
    }

}
