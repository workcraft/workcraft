package org.workcraft.plugins.xbm.properties;

import org.workcraft.dom.Node;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.xbm.XbmSignal;
import org.workcraft.plugins.xbm.VisualXbm;
import org.workcraft.plugins.xbm.Xbm;

import java.util.Map;

public class DeclaredSignalPropertyDescriptor implements PropertyDescriptor<Boolean> {

    public static final String PROPERTY_NEW_SIGNAL = "Create a new signal";
    public static final String PROPERTY_NEW_INPUT = "Create a new input signal";
    public static final String PROPERTY_NEW_OUTPUT = "Create a new output signal";

    private final VisualXbm visualXbm;
    private final String signalName;
    private final XbmSignal.Type targetType;

    public DeclaredSignalPropertyDescriptor(VisualXbm visualXbm, String signalName, XbmSignal.Type targetType) {
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
        for (XbmSignal xbmSignal : xbm.getSignals()) {
            if (xbm.getName(xbmSignal).equals(signalName)) return true;
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
        XbmSignal xbmSignal = xbm.createSignal(null);
        xbmSignal.setType(targetType);
    }

    private void removeSignal() {
        final Xbm xbm = visualXbm.getMathModel();
        final Node node = xbm.getNodeByReference(signalName);
        if (node instanceof XbmSignal) {
            XbmSignal xbmSignal = (XbmSignal) node;
            xbm.removeSignal(xbmSignal);
        }
        else {
            throw new RuntimeException("Unknown error: Node " + signalName + " is not a signal.");
        }
    }
}