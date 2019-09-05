package org.workcraft.plugins.xbm.properties;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.xbm.*;

import java.util.regex.Matcher;

public class SignalPropertyDescriptors {

    public static final PropertyDescriptor nameProperty(final VisualXbm visualXbm, final Signal signal) {
        final Xbm xbm = visualXbm.getMathModel();
        final String propertyName = xbm.getName(signal) + " name";

        return new PropertyDeclaration<Signal, String>
                (signal, propertyName, String.class, true, true) {

            @Override
            public void setter(Signal object, String value) {
                Node node = xbm.getNodeByReference(value);
                Matcher matcher = Signal.VALID_SIGNAL_NAME.matcher(value);
                if (node == null && matcher.find()) {
                    String origName = object.getName();
                    for (BurstEvent event: xbm.getBurstEvents()) {
                        for (String ref: event.getConditionalMapping().keySet()) {
                            if (ref.equals(origName)) {
                                boolean mapValue = event.getConditionalMapping().get(origName);
                                event.getConditionalMapping().put(value, mapValue);
                                event.getConditionalMapping().remove(origName);
                                break;
                            }
                        }
                    }
                    xbm.setName(object, value);
                    object.setName(value);
                }
                else if (!matcher.find()) {
                    throw new ArgumentException(value + " is not a valid node name.");
                }
                else if (object != node) {
                    throw new ArgumentException("Node " + value + " already exists.");
                }
            }

            @Override
            public String getter(Signal object) {
                return xbm.getName(object);
            }
        };
    }

    public static final PropertyDescriptor typeProperty(final VisualXbm visualXbm, final Signal signal) {
        final Xbm xbm = visualXbm.getMathModel();
        final String propertyName = xbm.getName(signal) + " type";
        return new PropertyDeclaration<Signal, Signal.Type>
                (signal, propertyName, Signal.Type.class, true, true) {
            @Override
            public void setter(Signal object, Signal.Type value) {
                object.setType(value);
            }

            @Override
            public Signal.Type getter(Signal object) {
                return object.getType();
            }
        };
    }

    public static final PropertyDescriptor valueProperty(final VisualXbm visualXbm, final XbmState targetState, final Signal targetSignal) {
        final Xbm xbm = visualXbm.getMathModel();
        final String propertyName = xbm.getName(targetSignal) + " value";
        return new PropertyDeclaration<XbmState, SignalState>
                (targetState, propertyName, SignalState.class, true, true) {
            @Override
            public void setter(XbmState object, SignalState value) {
                object.addOrChangeSignalValue(targetSignal, value);
            }

            @Override
            public SignalState getter(XbmState object) {
                return object.getEncoding().get(targetSignal);
            }
        };
    }
}
