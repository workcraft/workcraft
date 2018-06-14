package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.NamedTransition;
import org.workcraft.plugins.stg.Stg;

public class InstancePropertyDescriptor implements PropertyDescriptor {
    private final Stg stg;
    private final NamedTransition nt;

    public InstancePropertyDescriptor(Stg stg, NamedTransition nt) {
        this.stg = stg;
        this.nt = nt;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return stg.getInstanceNumber(nt);
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
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

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isCombinable() {
        return false;
    }

    @Override
    public boolean isTemplatable() {
        return false;
    }

}
