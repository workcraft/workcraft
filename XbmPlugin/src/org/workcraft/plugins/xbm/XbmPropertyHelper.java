package org.workcraft.plugins.xbm;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class XbmPropertyHelper {

    private static final String TOGGLE_SYMBOL = Character.toString((char) 0x21C5);
    private static final String HIGH_SYMBOL = Character.toString((char) 0x21C8);
    private static final String LOW_SYMBOL = Character.toString((char) 0x21CA);
    private static final String NEW_SYMBOL = Character.toString((char) 0x2217);
    private static final String RISING_SYMBOL = Character.toString((char) 0x002B);
    private static final String FALLING_SYMBOL = Character.toString((char) 0x2013);

    public static PropertyDescriptor getBurstProperty(XbmState state, String name, XbmSignal.Type type) {
        return new ActionListDeclaration(name)
                .addAction(TOGGLE_SYMBOL, () -> toggleSignalsByType(state, type), "Toggle " + type + "  burst signals")
                .addAction(HIGH_SYMBOL, () -> setSignalsToValueByType(state, type, SignalState.HIGH), "Set " + type + " burst signals high")
                .addAction(LOW_SYMBOL, () -> setSignalsToValueByType(state, type, SignalState.LOW), "Set " + type + " burst signals low");
    }

    public static PropertyDescriptor getBurstDirectionProperty(Xbm xbm, BurstEvent burstEvent, String name, XbmSignal.Type type) {
        return new ActionListDeclaration(name)
                .addAction(NEW_SYMBOL, () -> xbm.createSignal(null, type), "Create " + type + " signal in the bust")
                .addAction(TOGGLE_SYMBOL, () -> toggleDirectionsByType(burstEvent, type), "Toggle " + type + " burst signals")
                .addAction(RISING_SYMBOL, () -> setDirectionsToValueByType(burstEvent, type, Direction.PLUS), "Rise " + type + " burst signals")
                .addAction(FALLING_SYMBOL, () -> setDirectionsToValueByType(burstEvent, type, Direction.MINUS), "Fall " + type + " burst signals");
    }

    private static void toggleSignalsByType(XbmState state, XbmSignal.Type type) {
        Map<XbmSignal, SignalState> encodingMap = new HashMap<>(state.getEncoding());
        for (Map.Entry<XbmSignal, SignalState> entry : encodingMap.entrySet()) {
            if (entry.getKey().getType() == type) {
                state.addOrChangeSignalValue(entry.getKey(), entry.getValue().toggle());
            }
        }
    }

    private static void setSignalsToValueByType(XbmState state, XbmSignal.Type type, SignalState value) {
        Map<XbmSignal, SignalState> encodingMap = new HashMap<>(state.getEncoding());
        for (Map.Entry<XbmSignal, SignalState> entry: encodingMap.entrySet()) {
            if ((entry.getKey().getType() == type) && (entry.getValue() != value)) {
                state.addOrChangeSignalValue(entry.getKey(), value);
            }
        }
    }

    private static void toggleDirectionsByType(BurstEvent burstEvent, XbmSignal.Type type) {
        Map<XbmSignal, Direction> directionMap = new HashMap<>(burstEvent.getBurst().getDirection());
        for (Map.Entry<XbmSignal, Direction> entry : directionMap.entrySet()) {
            if (entry.getKey().getType() == type) {
                burstEvent.addOrChangeSignalDirection(entry.getKey(), entry.getValue().toggle());
            }
        }
    }

    private static void setDirectionsToValueByType(BurstEvent burstEvent, XbmSignal.Type type, Direction value) {
        Map<XbmSignal, Direction> directionMap = new HashMap<>(burstEvent.getBurst().getDirection());
        for (Map.Entry<XbmSignal, Direction> entry : directionMap.entrySet()) {
            if (entry.getKey().getType() == type && (entry.getValue() != value)) {
                burstEvent.addOrChangeSignalDirection(entry.getKey(), value);
            }
        }
    }

    public static PropertyDescriptor getSignalTypeProperty(final VisualXbm xbm, final XbmSignal xbmSignal) {
        return new PropertyDeclaration<>(XbmSignal.Type.class,
                xbm.getMathName(xbmSignal) + " type",
                xbmSignal::setType, xbmSignal::getType);
    }

    public static PropertyDescriptor getSignalValueProperty(final VisualXbm xbm, final XbmState xbmState, final XbmSignal xbmSignal) {
        return new PropertyDeclaration<>(SignalState.class,
                PropertyHelper.BULLET_PREFIX + xbm.getMathName(xbmSignal) + " value",
                value -> xbmState.addOrChangeSignalValue(xbmSignal, value),
                () -> xbmState.getEncoding().get(xbmSignal));
    }

    public static PropertyDescriptor getSignalDirectionProperty(final VisualXbm xbm, final BurstEvent burstEvent, final XbmSignal xbmSignal) {
        return new PropertyDeclaration<Direction>(Direction.class,
                PropertyHelper.BULLET_PREFIX + xbm.getMathName(xbmSignal) + " direction",
                value -> burstEvent.addOrChangeSignalDirection(xbmSignal, value),
                () -> burstEvent.getBurst().getDirection().get(xbmSignal)) {

            @Override
            public Map<Direction, String> getChoice() {
                Map<Direction, String> result = new HashMap<>();
                for (Map.Entry<Direction, String> entry: super.getChoice().entrySet()) {
                    result.put(entry.getKey(), entry.getValue() + " " + Direction.getPostfix(entry.getKey()));
                }
                return result;
            }
        };

    }

    public static PropertyDescriptor getSignalNameProperty(final Xbm xbm, final XbmSignal xbmSignal) {
        String signalName = xbm.getName(xbmSignal);
        return new PropertyDeclaration<>(TextAction.class,
                signalName + " name",
                value -> {
                    String newSignalName = value.getText();
                    Node node = xbm.getNodeByReference(newSignalName);
                    Matcher matcher = XbmSignal.VALID_SIGNAL_NAME.matcher(newSignalName);
                    if ((node == null) && matcher.find()) {
                        String origName = xbmSignal.getName();
                        for (BurstEvent event: xbm.getBurstEvents()) {
                            for (String ref: event.getConditionalMapping().keySet()) {
                                if (ref.equals(origName)) {
                                    boolean mapValue = event.getConditionalMapping().get(origName);
                                    event.getConditionalMapping().put(newSignalName, mapValue);
                                    event.getConditionalMapping().remove(origName);
                                    break;
                                }
                            }
                        }
                        xbm.setName(xbmSignal, newSignalName);
                        xbmSignal.setName(newSignalName);
                    } else if (!matcher.find()) {
                        throw new ArgumentException(value + " is not a valid node name.");
                    } else if (xbmSignal != node) {
                        throw new ArgumentException("Node " + value + " already exists.");
                    }
                },
                () -> new TextAction(signalName, new Action(PropertyHelper.CLEAR_SYMBOL,
                        () -> xbm.removeSignal(xbmSignal), "Remove signal '" + signalName + "'")
                ));
    }

}