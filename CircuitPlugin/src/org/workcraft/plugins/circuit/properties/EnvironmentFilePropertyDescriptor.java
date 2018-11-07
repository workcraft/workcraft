package org.workcraft.plugins.circuit.properties;

import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.circuit.VisualCircuit;

import java.io.File;
import java.util.Map;

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

}
