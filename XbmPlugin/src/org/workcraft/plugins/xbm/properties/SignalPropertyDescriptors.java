package org.workcraft.plugins.xbm.properties;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.xbm.*;

import java.util.regex.Matcher;

public class SignalPropertyDescriptors {

    public static PropertyDescriptor nameProperty(final VisualXbm visualXbm, final XbmSignal xbmSignal) {
        final Xbm xbm = visualXbm.getMathModel();
        final String propertyName = xbm.getName(xbmSignal) + " name";

        return new PropertyDeclaration<XbmSignal, String>(xbmSignal, propertyName, String.class, true, true) {
            @Override
            public void setter(XbmSignal object, String value) {
                Node node = xbm.getNodeByReference(value);
                Matcher matcher = XbmSignal.VALID_SIGNAL_NAME.matcher(value);
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
                } else if (!matcher.find()) {
                    throw new ArgumentException(value + " is not a valid node name.");
                } else if (object != node) {
                    throw new ArgumentException("Node " + value + " already exists.");
                }
            }

            @Override
            public String getter(XbmSignal object) {
                return xbm.getName(object);
            }
        };
    }

    public static PropertyDescriptor typeProperty(final VisualXbm visualXbm, final XbmSignal xbmSignal) {
        final Xbm xbm = visualXbm.getMathModel();
        final String propertyName = xbm.getName(xbmSignal) + " type";

        return new PropertyDeclaration<XbmSignal, XbmSignal.Type>(xbmSignal, propertyName, XbmSignal.Type.class, true, true) {
            @Override
            public void setter(XbmSignal object, XbmSignal.Type value) {
                object.setType(value);
            }

            @Override
            public XbmSignal.Type getter(XbmSignal object) {
                return object.getType();
            }
        };
    }

    public static PropertyDescriptor valueProperty(final VisualXbm visualXbm, final XbmState targetState, final XbmSignal targetXbmSignal) {
        final Xbm xbm = visualXbm.getMathModel();
        final String propertyName = xbm.getName(targetXbmSignal) + " value";

        return new PropertyDeclaration<XbmState, SignalState>(targetState, propertyName, SignalState.class, true, true) {
            @Override
            public void setter(XbmState object, SignalState value) {
                object.addOrChangeSignalValue(targetXbmSignal, value);
            }

            @Override
            public SignalState getter(XbmState object) {
                return object.getEncoding().get(targetXbmSignal);
            }
        };
    }

    public static PropertyDescriptor directionProperty(final VisualXbm visualXbm, final BurstEvent targetBurstEvent, final XbmSignal targetXbmSignal) {
        final Xbm xbm = visualXbm.getMathModel();
        final String propertyName = xbm.getName(targetXbmSignal) + " direction";

        return new PropertyDeclaration<BurstEvent, Burst.Direction>(targetBurstEvent, propertyName, Burst.Direction.class, true, true) {
            @Override
            public void setter(BurstEvent object, Burst.Direction value) {
                object.addOrChangeSignalDirection(targetXbmSignal, value);
            }

            @Override
            public Burst.Direction getter(BurstEvent object) {
                Burst burst = object.getBurst();
                return burst.getDirection().get(targetXbmSignal);
            }
        };
    }

}
