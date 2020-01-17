package org.workcraft.plugins.fst;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.fst.utils.FstUtils;

import java.awt.*;
import java.util.List;
import java.util.*;

public class FstPropertyHelper {

    public static Collection<PropertyDescriptor> getSignalProperties(VisualFst visualFst) {
        Collection<PropertyDescriptor> result = new ArrayList<>();
        Fst mathFst = visualFst.getMathModel();
        if (SignalCommonSettings.getGroupByType()) {
            for (Signal.Type type : Signal.Type.values()) {
                List<Signal> signals = new LinkedList<>(mathFst.getSignals(type));
                Collections.sort(signals, Comparator.comparing(visualFst::getMathName));
                for (final Signal signal : signals) {
                    result.add(getSignalProperty(visualFst, signal));
                }
            }
        } else {
            List<Signal> signals = new LinkedList<>(mathFst.getSignals());
            Collections.sort(signals, Comparator.comparing(visualFst::getMathName));
            for (final Signal signal : signals) {
                result.add(getSignalProperty(visualFst, signal));
            }
        }
        return result;
    }

    public static PropertyDescriptor getSignalProperty(VisualFst fst, Signal signal) {
        String signalName = fst.getMathName(signal);
        Signal.Type type = signal.getType();
        Color color = FstUtils.getTypeColor(type);

        Action leftAction = new Action(PropertyHelper.BULLET_SYMBOL,
                () -> signal.setType(type.toggle()),
                "Toggle type of signal '" + signalName + "'");

        Action rightAction = new Action(PropertyHelper.SEARCH_SYMBOL,
                () -> {
                    fst.selectNone();
                    fst.addToSelection(fst.getVisualEvents(signal));
                }, "Select all events of signal '" + signalName + "'");

        return new PropertyDeclaration<>(TextAction.class, "Signal " + signalName,
                value -> {
                    String newName = value.getText();
                    Fst mathFst = fst.getMathModel();
                    Node node = mathFst.getNodeByReference(newName);
                    Collection<SignalEvent> events = mathFst.getSignalEvents(signal);
                    if (node == null) {
                        fst.setMathName(signal, newName);
                    } else if (node instanceof Signal) {
                        Signal existingSignal = (Signal) node;
                        for (SignalEvent event : events) {
                            event.setSymbol(existingSignal);
                        }
                    } else {
                        throw new FormatException("Node '" + value + "' already exists and it is not a signal.");
                    }
                },
                () -> new TextAction(signalName).setLeftAction(leftAction).setRightAction(rightAction).setForeground(color)
        ).setSpan();
    }

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
