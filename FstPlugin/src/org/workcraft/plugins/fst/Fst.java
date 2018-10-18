package org.workcraft.plugins.fst;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fst.observers.SignalConsistencySupervisor;
import org.workcraft.plugins.fst.properties.DirectionPropertyDescriptor;
import org.workcraft.plugins.fst.properties.EventSignalPropertyDescriptor;
import org.workcraft.plugins.fst.properties.SignalTypePropertyDescriptor;
import org.workcraft.plugins.fst.properties.TypePropertyDescriptor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

import java.util.Collection;
import java.util.LinkedList;

public class Fst extends Fsm {

    public Fst() {
        this(null, null);
    }

    public Fst(Container root, References refs) {
        super(root, refs);
        new SignalConsistencySupervisor(this).attach(getRoot());
    }

    @Override
    public boolean isDeterministicSymbol(Symbol symbol) {
        boolean result = false;
        if (symbol instanceof Signal) {
            Signal signal = (Signal) symbol;
            result = signal.getType() != Signal.Type.DUMMY;
        } else {
            result = super.isDeterministicSymbol(symbol);
        }
        return result;
    }

    public Signal createSignal(String name, Signal.Type type) {
        Signal signal = createNode(name, null, Signal.class);
        signal.setType(type);
        return signal;
    }

    public Signal getOrCreateSignal(String name, Signal.Type type) {
        Signal signal = null;
        Node node = getNodeByReference(name);
        if (node == null) {
            signal = createSignal(name, type);
        } else if (node instanceof Signal) {
            signal = (Signal) node;
            if (signal.getType() != type) {
                throw new ArgumentException("Signal '" + name + "' already exists and its type '"
                        + signal.getType() + "' is different from the required \'" + type + "' type.");
            }
        } else {
            throw new ArgumentException("Node '" + name + "' already exists and it is not a signal.");
        }
        return signal;
    }

    public SignalEvent createSignalEvent(State first, State second, Signal symbol) {
        Container container = Hierarchy.getNearestContainer(first, second);
        SignalEvent event = new SignalEvent(first, second, symbol);
        container.add(event);
        return event;
    }

    public final Collection<Signal> getSignals() {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class);
    }

    public final Collection<Signal> getSignals(final Signal.Type type) {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class,
                signal -> (signal != null) && (signal.getType() == type));
    }

    public final Collection<SignalEvent> getSignalEvents() {
        return Hierarchy.getDescendantsOfType(getRoot(), SignalEvent.class);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            for (final Signal signal: getSignals()) {
                SignalTypePropertyDescriptor typeDescriptor = new SignalTypePropertyDescriptor(this, signal);
                properties.insertOrderedByFirstWord(typeDescriptor);
            }
        } else if (node instanceof SignalEvent) {
            LinkedList<PropertyDescriptor> eventDescriptors = new LinkedList<>();
            SignalEvent signalEvent = (SignalEvent) node;
            eventDescriptors.add(new EventSignalPropertyDescriptor(this, signalEvent));
            Signal signal = signalEvent.getSignal();
            eventDescriptors.add(new TypePropertyDescriptor(signal));
            if (signal.hasDirection()) {
                eventDescriptors.add(new DirectionPropertyDescriptor(signalEvent));
            }
            properties.addSorted(eventDescriptors);
            properties.removeByName(Event.PROPERTY_SYMBOL);
        }
        return properties;
    }

}
