package org.workcraft.plugins.dtd;

import java.util.Collection;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.NamePropertyDescriptor;
import org.workcraft.plugins.dtd.Signal.Type;
import org.workcraft.plugins.dtd.propertydescriptors.SignalTypePropertyDescriptor;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;

@VisualClass(org.workcraft.plugins.dtd.VisualDtd.class)
public class Dtd extends Graph {

    public Dtd() {
        this(null, (References) null);
    }

    public Dtd(Container root, References refs) {
        this(root, new HierarchicalUniqueNameReferenceManager(refs) {
            @Override
            public String getPrefix(Node node) {
                if (node instanceof Signal) return "x";
                if (node instanceof Transition) return Identifier.createInternal("e");
                return super.getPrefix(node);
            }
        });
    }

    public Dtd(Container root, ReferenceManager man) {
        super(root, man);
    }

    @Override
    public boolean keepUnusedSymbols() {
        return true;
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

    public final Collection<Signal> getSignals() {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class);
    }

    public final Collection<Signal> getSignals(final Type type) {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class, new Func<Signal, Boolean>() {
            @Override
            public Boolean eval(Signal arg) {
                return (arg != null) && (arg.getType() == type);
            }
        });
    }

    public final Collection<Transition> getTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            for (final Signal signal: getSignals()) {
                SignalTypePropertyDescriptor typeDescriptor = new SignalTypePropertyDescriptor(this, signal);
                properties.insertOrderedByFirstWord(typeDescriptor);
            }
        } else if (node instanceof Transition) {
            properties.removeByName(Transition.PROPERTY_SYMBOL);
            properties.removeByName(NamePropertyDescriptor.PROPERTY_NAME);
        }
        return properties;
    }

}
