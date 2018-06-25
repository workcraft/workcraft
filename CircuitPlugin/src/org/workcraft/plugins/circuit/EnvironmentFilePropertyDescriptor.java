package org.workcraft.plugins.circuit;

import java.io.File;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;

public class EnvironmentFilePropertyDescriptor implements PropertyDescriptor {
    VisualCircuit model;

    public EnvironmentFilePropertyDescriptor(VisualCircuit model) {
        this.model = model;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return "Environment";
    }

    @Override
    public Class<?> getType() {
        return File.class;
    }

    @Override
    public Object getValue() {
        return model.getEnvironmentFile();
    }

    @Override
    public void setValue(Object value) {
        model.setEnvironmentFile((File) value);
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
