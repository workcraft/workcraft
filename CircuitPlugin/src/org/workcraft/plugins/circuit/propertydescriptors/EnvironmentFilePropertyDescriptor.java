package org.workcraft.plugins.circuit.propertydescriptors;

import java.io.File;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.circuit.VisualCircuit;

public class EnvironmentFilePropertyDescriptor implements PropertyDescriptor {

    private final VisualCircuit circuit;

    public EnvironmentFilePropertyDescriptor(VisualCircuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public String getName() {
        return "Environment";
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public Class<?> getType() {
        return File.class;
    }

    @Override
    public Object getValue() {
        return circuit.getEnvironmentFile();
    }

    @Override
    public void setValue(Object value) {
        circuit.setEnvironmentFile((File) value);
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
