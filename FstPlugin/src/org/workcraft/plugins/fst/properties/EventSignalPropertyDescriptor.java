package org.workcraft.plugins.fst.properties;

import org.workcraft.dom.Node;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.Signal;
import org.workcraft.plugins.fst.SignalEvent;

import java.util.Map;

public class EventSignalPropertyDescriptor implements PropertyDescriptor {
    private final Fst fst;
    private final SignalEvent signalEvent;

    public EventSignalPropertyDescriptor(Fst fst, SignalEvent signalEvent) {
        this.fst = fst;
        this.signalEvent = signalEvent;
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
    public Object getValue() {
        Signal signal = signalEvent.getSignal();
        if (signal != null) {
            return fst.getName(signal);
        }
        return null;
    }

    @Override
    public void setValue(Object value) {
        Signal signal = null;
        String signalName = (String) value;
        if (!signalName.isEmpty()) {
            Node node = fst.getNodeByReference(signalName);
            if (node instanceof Signal) {
                signal = (Signal) node;
            } else {
                Signal oldSignal = signalEvent.getSignal();
                Signal.Type type = oldSignal.getType();
                signal = fst.createSignal(signalName, type);
            }
        }
        if (signal != null) {
            signalEvent.setSymbol(signal);
        }
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
