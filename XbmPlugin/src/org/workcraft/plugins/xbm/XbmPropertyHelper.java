package org.workcraft.plugins.xbm;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.*;

import java.util.Map;
import java.util.regex.Matcher;

public class XbmPropertyHelper {

    private static final String TOGGLE_SYMBOL = Character.toString((char) 0x21C5);
    private static final String HIGH_SYMBOL = Character.toString((char) 0x21C8);
    private static final String LOW_SYMBOL = Character.toString((char) 0x21CA);

    public static PropertyDescriptor getBurstProperty(XbmState state, String name, XbmSignal.Type type) {
        return new ActionListDeclaration(name)
                .addAction(TOGGLE_SYMBOL, () -> toggleSignalsByType(state, type))
                .addAction(HIGH_SYMBOL, () -> setSignalsToValueByType(state, type, SignalState.HIGH))
                .addAction(LOW_SYMBOL, () -> setSignalsToValueByType(state, type, SignalState.LOW));
    }

    private static void toggleSignalsByType(XbmState state, XbmSignal.Type type) {
        for (Map.Entry<XbmSignal, SignalState> entry: state.getEncoding().entrySet()) {
            if (entry.getKey().getType() == type) {
                state.addOrChangeSignalValue(entry.getKey(), entry.getValue().toggle());
            }
        }
    }

    private static void setSignalsToValueByType(XbmState state, XbmSignal.Type type, SignalState value) {
        for (Map.Entry<XbmSignal, SignalState> entry: state.getEncoding().entrySet()) {
            if ((entry.getKey().getType() == type) && (entry.getValue() != value)) {
                state.addOrChangeSignalValue(entry.getKey(), value);
            }
        }
    }

    public static PropertyDescriptor getSignalTypeProperty(final VisualXbm xbm, final XbmSignal xbmSignal) {
        return new PropertyDeclaration<>(XbmSignal.Type.class,
                xbm.getMathName(xbmSignal) + " type",
                xbmSignal::setType, xbmSignal::getType);
    }

    public static PropertyDescriptor getStateValueProperty(final VisualXbm xbm, final XbmState xbmState, final XbmSignal xbmSignal) {
        return new PropertyDeclaration<>(SignalState.class,
                PropertyHelper.BULLET_PREFIX + xbm.getMathName(xbmState) + " type",
                (value) -> xbmState.addOrChangeSignalValue(xbmSignal, value),
                () -> xbmState.getEncoding().get(xbmSignal));
    }

    public static PropertyDescriptor getBurstDirectionProperty(final VisualXbm xbm, final BurstEvent burstEvent, final XbmSignal xbmSignal) {
        return new PropertyDeclaration<>(Burst.Direction.class,
                PropertyHelper.BULLET_PREFIX + xbm.getMathName(xbmSignal) + " direction",
                (value) -> burstEvent.addOrChangeSignalDirection(xbmSignal, value),
                () -> burstEvent.getBurst().getDirection().get(xbmSignal));
    }

    public static PropertyDescriptor getSignalNameProperty(final Xbm xbm, final XbmSignal xbmSignal) {
        return new PropertyDeclaration<>(TextAction.class,
                xbm.getName(xbmSignal) + " name",
                (value) -> {
                    String newName = value.getText();
                    Node node = xbm.getNodeByReference(newName);
                    Matcher matcher = XbmSignal.VALID_SIGNAL_NAME.matcher(newName);
                    if ((node == null) && matcher.find()) {
                        String origName = xbmSignal.getName();
                        for (BurstEvent event: xbm.getBurstEvents()) {
                            for (String ref: event.getConditionalMapping().keySet()) {
                                if (ref.equals(origName)) {
                                    boolean mapValue = event.getConditionalMapping().get(origName);
                                    event.getConditionalMapping().put(newName, mapValue);
                                    event.getConditionalMapping().remove(origName);
                                    break;
                                }
                            }
                        }
                        xbm.setName(xbmSignal, newName);
                        xbmSignal.setName(newName);
                    } else if (!matcher.find()) {
                        throw new ArgumentException(value + " is not a valid node name.");
                    } else if (xbmSignal != node) {
                        throw new ArgumentException("Node " + value + " already exists.");
                    }
                },
                () -> new TextAction(xbm.getName(xbmSignal),
                        new Action(org.workcraft.gui.properties.PropertyHelper.CLEAR_SYMBOL, () -> xbm.removeSignal(xbmSignal))));
    }

}