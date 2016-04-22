package org.workcraft.plugins.dtd;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.dtd.Signal.Type;
import org.workcraft.plugins.dtd.propertydescriptors.DirectionPropertyDescriptor;
import org.workcraft.plugins.dtd.propertydescriptors.SignalTypePropertyDescriptor;
import org.workcraft.plugins.dtd.propertydescriptors.TransitionPropertyDescriptor;
import org.workcraft.plugins.dtd.propertydescriptors.TypePropertyDescriptor;
import org.workcraft.plugins.pog.Pog;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@VisualClass(org.workcraft.plugins.dtd.VisualDtd.class)
public class Dtd extends Pog {

    public Dtd() {
        this(null, null);
    }

    public Dtd(Container root, References refs) {
        super(root, new HierarchicalUniqueNameReferenceManager(refs) {
            @Override
            public String getPrefix(Node node) {
                if (node instanceof Signal) return "x";
                if (node instanceof Transition) return "e";
                return super.getPrefix(node);
            }
        });
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

    public Transition createTransition(Signal signal) {
        Transition transition = new Transition(signal);
        getRoot().add(transition);
        return transition;
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

    final public Collection<Transition> getTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            LinkedList<PropertyDescriptor> signalDescriptors = new LinkedList<>();
            for (final Signal signal: getSignals()) {
                signalDescriptors.add(new SignalTypePropertyDescriptor(this, signal));
            }
            properties.addSorted(signalDescriptors);
        } else if (node instanceof Transition) {
            LinkedList<PropertyDescriptor> transitionDescriptors = new LinkedList<>();
            Transition transition = (Transition) node;
            transitionDescriptors.add(new TransitionPropertyDescriptor(this, transition));
            Signal signal = transition.getSignal();
            transitionDescriptors.add(new TypePropertyDescriptor(signal));
            transitionDescriptors.add(new DirectionPropertyDescriptor(transition));
            properties.addSorted(transitionDescriptors);
            properties.removeByName(Transition.PROPERTY_SYMBOL);
        }
        return properties;
    }

}
