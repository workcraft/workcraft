package org.workcraft.plugins.fst;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fst.Signal.Type;
import org.workcraft.plugins.fst.propertydescriptors.DirectionPropertyDescriptor;
import org.workcraft.plugins.fst.propertydescriptors.EventSignalPropertyDescriptor;
import org.workcraft.plugins.fst.propertydescriptors.SignalTypePropertyDescriptor;
import org.workcraft.plugins.fst.propertydescriptors.TypePropertyDescriptor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@VisualClass(org.workcraft.plugins.fst.VisualFst.class)
public class Fst extends Fsm {

    private final class StateSupervisorExtension extends StateSupervisor {
        @Override
        public void handleEvent(StateEvent e) {
            if (e instanceof PropertyChangedEvent) {
                PropertyChangedEvent pce = (PropertyChangedEvent)e;
                Object sender = e.getSender();
                if ((sender instanceof Signal) && pce.getPropertyName().equals(Signal.PROPERTY_TYPE)) {
                    for (Event event: getEvents((Signal)sender)) {
                        event.sendNotification(new PropertyChangedEvent(event, Signal.PROPERTY_TYPE));
                    }
                }
            }
        }
    }

    public Fst() {
        this(null, null);
    }

    public Fst(Container root, References refs) {
        super(root, new HierarchicalUniqueNameReferenceManager(refs) {
            @Override
            public String getPrefix(Node node) {
                if (node instanceof State) return "s";
                if (node instanceof Event) return "e";
                if (node instanceof Signal) return "x";
                return super.getPrefix(node);
            }
        });

        new StateSupervisorExtension().attach(getRoot());
    }

    @Override
    public boolean isDeterministicSymbol(Symbol symbol) {
        boolean result = false;
        if (symbol instanceof Signal) {
            Signal signal = (Signal)symbol;
            result = signal.getType() != Type.DUMMY;
        } else {
            result = super.isDeterministicSymbol(symbol);
        }
        return result;
    }

    public Signal createSignal(String name, Type type) {
        Signal signal = createNode(name, null, Signal.class);
        signal.setType(type);
        return signal;
    }

    public Signal getOrCreateSignal(String name, Type type) {
        Signal signal = null;
        Node node = getNodeByReference(name);
           if (node == null) {
            signal = createSignal(name, type);
           } else if (node instanceof Signal) {
               signal = (Signal)node;
               if (signal.getType() != type) {
                   throw new ArgumentException("Signal '" + name + "' already exists and its type '"
                           + signal.getType() + "' is different from the required \'" + type +"' type.");
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

    final public Collection<Signal> getSignals() {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class);
    }

    final public Collection<Signal> getSignals(final Type type) {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class, new Func<Signal, Boolean>() {
            @Override
            public Boolean eval(Signal arg) {
                return (arg != null) && (arg.getType() == type);
            }
        });
    }

    final public Collection<SignalEvent> getSignalEvents() {
        return Hierarchy.getDescendantsOfType(getRoot(), SignalEvent.class);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            LinkedList<PropertyDescriptor> signalDescriptors= new LinkedList<>();
            for (final Signal signal: getSignals()) {
                signalDescriptors.add(new SignalTypePropertyDescriptor(this, signal));
            }
            properties.addSorted(signalDescriptors);
        } else if (node instanceof SignalEvent) {
            LinkedList<PropertyDescriptor> eventDescriptors= new LinkedList<>();
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
