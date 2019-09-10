package org.workcraft.plugins.xbm.properties;

import org.workcraft.dom.Node;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.xbm.Signal;
import org.workcraft.plugins.xbm.VisualXbm;
import org.workcraft.plugins.xbm.Xbm;

import java.util.Map;

public class DeclaredSignalPropertyDescriptor implements PropertyDescriptor<Boolean> {

    public static final String PROPERTY_NEW_SIGNAL = "Create a new signal";
    public static final String PROPERTY_NEW_INPUT = "Create a new input signal";
    public static final String PROPERTY_NEW_OUTPUT = "Create a new output signal";

    private final VisualXbm visualXbm;
    private final String signalName;
    private final Signal.Type targetType;

    public DeclaredSignalPropertyDescriptor(VisualXbm visualXbm, String signalName, Signal.Type targetType) {
        this.visualXbm = visualXbm;
        this.signalName = signalName;
        this.targetType = targetType;
    }

    @Override
    public Map<Boolean, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return signalName + " declared";
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public Boolean getValue() {
        if (signalName.equals(PROPERTY_NEW_SIGNAL)) return false;
        Xbm xbm = visualXbm.getMathModel();
        for (Signal signal : xbm.getSignals()) {
            if (xbm.getName(signal).equals(signalName)) return true;
        }
        return false;
    }

    @Override
    public void setValue(Boolean value) {
        if (signalName != null) {
            if (value) insertNewSignal();
            else removeSignal();
        }
    }

    private void insertNewSignal() {
        final Xbm xbm = visualXbm.getMathModel();
        Signal signal = xbm.createSignal(null);
        signal.setType(targetType);
    }

    private void removeSignal() {
        final Xbm xbm = visualXbm.getMathModel();
        final Node node = xbm.getNodeByReference(signalName);
        if (node instanceof Signal) {
            Signal signal = (Signal) node;
            xbm.removeSignal(signal);
        }
        else {
            throw new RuntimeException("Unknown error: Node " + signalName + " is not a signal.");
        }
    }
}