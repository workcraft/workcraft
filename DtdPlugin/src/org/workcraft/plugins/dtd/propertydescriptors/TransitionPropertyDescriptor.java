package org.workcraft.plugins.dtd.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.dtd.Dtd;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.Signal.Type;
import org.workcraft.plugins.dtd.Transition;

public class TransitionPropertyDescriptor implements PropertyDescriptor {
    private final Dtd dtd;
    private final Transition transition;

    public TransitionPropertyDescriptor(Dtd dtd, Transition transition) {
        this.dtd = dtd;
        this.transition = transition;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return "Signal";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        Signal signal = transition.getSignal();
        if (signal != null) {
            return dtd.getName(signal);
        }
        return null;
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        Signal signal = null;
        String signalName = (String) value;
        if (!signalName.isEmpty()) {
            Node node = dtd.getNodeByReference(signalName);
            if (node instanceof Signal) {
                signal = (Signal) node;
            } else {
                Signal oldSignal = transition.getSignal();
                Type type = oldSignal.getType();
                signal = dtd.createSignal(signalName, type);
            }
        }
        if (signal != null) {
            transition.setSymbol(signal);
        }
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isCombinable() {
        return true;
    }

    @Override
    public boolean isTemplatable() {
        return true;
    }

}
