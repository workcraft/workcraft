package org.workcraft.plugins.fst;

import org.workcraft.dom.Node;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

public class FstPropertyHelper {

    public static PropertyDescriptor getEventSignalProperty(Fst fst, SignalEvent signalEvent) {
        return new PropertyDeclaration<>(String.class, "Signal",
                value -> {
                    Signal signal = null;
                    if (!value.isEmpty()) {
                        Node node = fst.getNodeByReference(value);
                        if (node instanceof Signal) {
                            signal = (Signal) node;
                        } else {
                            Signal oldSignal = signalEvent.getSignal();
                            Signal.Type type = oldSignal.getType();
                            signal = fst.createSignal(value, type);
                        }
                    }
                    if (signal != null) {
                        signalEvent.setSymbol(signal);
                    }
                },
                () -> {
                    Signal signal = signalEvent.getSignal();
                    if (signal != null) {
                        return fst.getName(signal);
                    }
                    return null;
                })
                .setCombinable().setTemplatable();
    }

    public static PropertyDescriptor getSignalTypeProperty(Signal signal, String description) {
        return new PropertyDeclaration<>(Signal.Type.class, description,
                value -> signal.setType(value), () -> signal.getType())
                .setCombinable().setTemplatable();
    }

    public static PropertyDescriptor getEventDrectionProperty(SignalEvent signalEvent) {
        return new PropertyDeclaration<>(SignalEvent.Direction.class, SignalEvent.PROPERTY_DIRECTION,
                value -> signalEvent.setDirection(value),
                () -> signalEvent.getDirection())
                .setCombinable().setTemplatable();
    }

}
