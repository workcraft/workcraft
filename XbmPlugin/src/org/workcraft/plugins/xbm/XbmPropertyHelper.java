package org.workcraft.plugins.xbm;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.*;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;

import java.awt.*;
import java.util.List;
import java.util.*;
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

    public static Collection<PropertyDescriptor> getSignalProperties(Xbm xbm) {
        final Collection<PropertyDescriptor> result = new LinkedList<>();
        if (SignalCommonSettings.getGroupByType()) {
            for (XbmSignal.Type type : XbmSignal.Type.values()) {
                java.util.List<XbmSignal> signals = new ArrayList<>(xbm.getSignals(type));
                Collections.sort(signals, Comparator.comparing(XbmSignal::getName));
                for (final XbmSignal signal : signals) {
                    result.add(getSignalProperty(xbm, signal));
                }
            }
        } else {
            List<XbmSignal> signals = new ArrayList<>(xbm.getSignals());
            Collections.sort(signals, Comparator.comparing(XbmSignal::getName));
            for (final XbmSignal signal : signals) {
                result.add(getSignalProperty(xbm, signal));
            }
        }
        return result;
    }

    public static ActionDeclaration getCreateSignalProperty(VisualXbm visualXbm) {
        return new ActionDeclaration("Create signal",
                () -> {
                    visualXbm.getMathModel().createSignal(null, XbmSignal.DEFAULT_SIGNAL_TYPE);
                    visualXbm.sendNotification(new ModelModifiedEvent(visualXbm));
                });
    }


    public static PropertyDescriptor getSignalProperty(final Xbm xbm, final XbmSignal xbmSignal) {
        String signalName = xbm.getName(xbmSignal);

        Action leftAction = new Action(PropertyHelper.BULLET_SYMBOL,
                () -> xbmSignal.setType(xbmSignal.getType().toggle()), "Toggle type of signal '" + signalName + "'");

        Action rightAction = new Action(PropertyHelper.CLEAR_SYMBOL,
                () -> xbm.removeSignal(xbmSignal), "Remove signal '" + signalName + "'");

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
                () -> new TextAction(signalName).setLeftAction(leftAction).setRightAction(rightAction)
                        .setForeground(getSignalColor(xbmSignal))
        ).setSpan();
    }

    private static Color getSignalColor(XbmSignal xbmSignal) {
        switch (xbmSignal.getType()) {
        case INPUT:
            return SignalCommonSettings.getInputColor();
        case OUTPUT:
            return SignalCommonSettings.getOutputColor();
        case CONDITIONAL:
            return SignalCommonSettings.getInternalColor();
        }
        return SignalCommonSettings.getDummyColor();
    }


    public static List<PropertyDescriptor> getSignalValueProperties(final VisualXbm visualxbm, final XbmState state) {
        final List<PropertyDescriptor> result = new LinkedList<>();
        final Xbm mathXbm = visualxbm.getMathModel();
        final Set<XbmSignal> inputs = new LinkedHashSet<>(mathXbm.getSignals(XbmSignal.Type.INPUT));
        final Set<XbmSignal> outputs = new LinkedHashSet<>(mathXbm.getSignals(XbmSignal.Type.OUTPUT));
        if (!inputs.isEmpty()) {
            result.add(getBurstProperty(state, "Input burst", XbmSignal.Type.INPUT));
            for (XbmSignal i: inputs) {
                result.add(getSignalValueProperty(visualxbm, state, i));
            }
        }
        if (!outputs.isEmpty()) {
            result.add(getBurstProperty(state, "Output burst", XbmSignal.Type.OUTPUT));
            for (XbmSignal o: outputs) {
                result.add(getSignalValueProperty(visualxbm, state, o));
            }
        }
        return result;
    }

    public static List<PropertyDescriptor> getSignalDirectionProperties(final VisualXbm visualxbm, final BurstEvent burstEvent) {
        final List<PropertyDescriptor> result = new LinkedList<>();
        final Xbm mathXbm = visualxbm.getMathModel();
        final Set<XbmSignal> inputs = new LinkedHashSet<>(mathXbm.getSignals(XbmSignal.Type.INPUT));
        final Set<XbmSignal> outputs = new LinkedHashSet<>(mathXbm.getSignals(XbmSignal.Type.OUTPUT));

        result.add(getBurstDirectionProperty(mathXbm, burstEvent, "Input burst", XbmSignal.Type.INPUT));

        if (!inputs.isEmpty()) {
            for (XbmSignal i: inputs) {
                result.add(getSignalDirectionProperty(visualxbm, burstEvent, i));
            }
        }

        result.add(getBurstDirectionProperty(mathXbm, burstEvent, "Output burst", XbmSignal.Type.OUTPUT));

        if (!outputs.isEmpty()) {
            for (XbmSignal o: outputs) {
                result.add(getSignalDirectionProperty(visualxbm, burstEvent, o));
            }
        }
        return result;
    }

    public static PropertyDescriptor getConditionalProperty(final BurstEvent event) {
        return new PropertyDeclaration<>(String.class, BurstEvent.PROPERTY_CONDITIONAL,
                event::setConditional, event::getConditional)
                .setCombinable().setTemplatable();
    }

}
