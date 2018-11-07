package org.workcraft.plugins.stg.properties;

import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.stg.NamedTransition;
import org.workcraft.plugins.stg.Stg;

import java.util.Map;

public class InstancePropertyDescriptor implements PropertyDescriptor {
    private final Stg stg;
    private final NamedTransition nt;

    public InstancePropertyDescriptor(Stg stg, NamedTransition nt) {
        this.stg = stg;
        this.nt = nt;
    }

    @Override
    public Object getValue() {
        return stg.getInstanceNumber(nt);
    }

    @Override
    public void setValue(Object value) {
        stg.setInstanceNumber(nt, Integer.parseInt(value.toString()));
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return "Instance";
    }

    @Override
    public Class<?> getType() {
        return int.class;
    }

}
