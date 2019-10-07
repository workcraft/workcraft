package org.workcraft.plugins.xbm.properties;

import org.workcraft.dom.Node;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.xbm.VisualXbm;
import org.workcraft.plugins.xbm.Xbm;
import org.workcraft.plugins.xbm.XbmSignal;

import java.util.Map;

public class DeleteSignalPropertyDescriptor implements PropertyDescriptor<Boolean> {

    private final VisualXbm visualXbm;
    private final String signalName;

    public DeleteSignalPropertyDescriptor(VisualXbm visualXbm, String signalName) {
        this.visualXbm = visualXbm;
        this.signalName = signalName;
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
        if (signalName != null) {
            Xbm xbm = visualXbm.getMathModel();
            for (XbmSignal xbmSignal : xbm.getSignals()) {
                if (signalName.equals(xbm.getName(xbmSignal))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setValue(Boolean value) {
        if (!value && (signalName != null)) {
            final Xbm xbm = visualXbm.getMathModel();
            final Node node = xbm.getNodeByReference(signalName);
            if (node instanceof XbmSignal) {
                XbmSignal xbmSignal = (XbmSignal) node;
                xbm.removeSignal(xbmSignal);
            }
        }
    }

}
