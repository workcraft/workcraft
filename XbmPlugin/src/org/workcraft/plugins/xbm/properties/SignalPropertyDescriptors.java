package org.workcraft.plugins.xbm.properties;

import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyUtils;
import org.workcraft.plugins.xbm.*;

public class SignalPropertyDescriptors {

    public static final String BULLET_PREFIX = "  " + PropertyUtils.BULLET_TEXT + " ";

    public static PropertyDescriptor typeProperty(final VisualXbm visualXbm, final XbmSignal xbmSignal) {
        final Xbm xbm = visualXbm.getMathModel();
        final String propertyName = xbm.getName(xbmSignal) + " type";

        return new PropertyDeclaration<XbmSignal, XbmSignal.Type>(xbmSignal, propertyName, XbmSignal.Type.class) {
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
        final String propertyName = BULLET_PREFIX + xbm.getName(targetXbmSignal) + " value";

        return new PropertyDeclaration<XbmState, SignalState>(targetState, propertyName, SignalState.class) {
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
        final String propertyName = BULLET_PREFIX + xbm.getName(targetXbmSignal) + " direction";

        return new PropertyDeclaration<BurstEvent, Burst.Direction>(targetBurstEvent, propertyName, Burst.Direction.class) {
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
