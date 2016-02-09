package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;

public class InstancePropertyDescriptor implements PropertyDescriptor {
    private final STG stg;
    private final Node st;

    public InstancePropertyDescriptor(STG stg, Node st) {
        this.stg = stg;
        this.st = st;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return stg.getInstanceNumber(st);
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        stg.setInstanceNumber(st, Integer.parseInt(value.toString()));
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
