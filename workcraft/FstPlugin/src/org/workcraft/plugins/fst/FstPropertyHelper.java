package org.workcraft.plugins.fst;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.utils.SortUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FstPropertyHelper {

    public static Collection<PropertyDescriptor> getSignalProperties(VisualFst visualFst) {
        Collection<PropertyDescriptor> result = new ArrayList<>();
        Fst mathFst = visualFst.getMathModel();
        if (SignalCommonSettings.getGroupByType()) {
            for (Signal.Type type : Signal.Type.values()) {
                if (type == Signal.Type.DUMMY) continue;
                List<Signal> signals = new LinkedList<>(mathFst.getSignals(type));
                signals.sort((s1, s2) -> SortUtils.compareNatural(
                        visualFst.getMathName(s1), visualFst.getMathName(s2)));

                for (final Signal signal : signals) {
                    result.add(getSignalProperty(visualFst, signal));
                }
            }
        } else {
            List<Signal> signals = new LinkedList<>(mathFst.getSignals());
            signals.sort((s1, s2) -> SortUtils.compareNatural(
                    visualFst.getMathName(s1), visualFst.getMathName(s2)));

            for (final Signal signal : signals) {
                if (signal.getType() == Signal.Type.DUMMY) continue;
                result.add(getSignalProperty(visualFst, signal));
            }
        }
        return result;
    }

    private static PropertyDescriptor getSignalProperty(VisualFst fst, Signal signal) {
        String signalName = fst.getMathName(signal);
        Signal.Type type = signal.getType();

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
                        throw new FormatException("Node '" + newName + "' already exists and it is not a signal.");
                    }
                    for (SignalEvent event : events) {
                        event.sendNotification(new PropertyChangedEvent(event, Event.PROPERTY_SYMBOL));
                    }
                },
                () -> new TextAction(signalName).setLeftAction(leftAction)
                        .setRightAction(rightAction).setForeground(type.getColor())
        ).setSpan();
    }

    public static PropertyDescriptor getEventSignalProperty(Fst fst, SignalEvent signalEvent) {
        return new PropertyDeclaration<>(String.class, "Signal",
                value -> {
                    Identifier.validate(value);
                    Signal signal = null;
                    if (!value.isEmpty()) {
                        Node node = fst.getNodeByReference(value);
                        if (node instanceof Signal) {
                            signal = (Signal) node;
                        } else {
                            Signal oldSignal = signalEvent.getSymbol();
                            Signal.Type type = oldSignal.getType();
                            signal = fst.createSignal(value, type);
                        }
                    }
                    if (signal != null) {
                        signalEvent.setSymbol(signal);
                    }
                },
                () -> {
                    Signal signal = signalEvent.getSymbol();
                    return signal == null ? null : fst.getName(signal);
                })
                .setCombinable().setTemplatable();
    }

    public static PropertyDescriptor getSignalTypeProperty(Signal signal, String description) {
        return new PropertyDeclaration<>(Signal.Type.class, description,
                signal::setType, signal::getType).setCombinable().setTemplatable();
    }

    public static PropertyDescriptor getEventDirectionProperty(SignalEvent signalEvent) {
        return new PropertyDeclaration<>(SignalEvent.Direction.class, SignalEvent.PROPERTY_DIRECTION,
                signalEvent::setDirection, signalEvent::getDirection).setCombinable().setTemplatable();
    }

}
