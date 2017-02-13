package org.workcraft.plugins.dtd;

import java.util.Collection;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.dtd.Signal.State;
import org.workcraft.plugins.dtd.Signal.Type;
import org.workcraft.plugins.dtd.propertydescriptors.DirectionPropertyDescriptor;
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

    public Collection<Signal> getSignals() {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class);
    }

    public Collection<Signal> getSignals(final Type type) {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class, new Func<Signal, Boolean>() {
            @Override
            public Boolean eval(Signal arg) {
                return (arg != null) && (arg.getType() == type);
            }
        });
    }

    public Collection<Transition> getTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
    }

    public State getBeforeState(Transition transition) {
        Signal signal = transition.getSignal();
        for (Connection connection: getConnections(transition)) {
            if (connection.getSecond() != transition) continue;
            Node fromNode = connection.getFirst();
            if (fromNode == signal) {
                return signal.getInitialState();
            } else if (fromNode instanceof Transition) {
                Transition fromTransition = (Transition) fromNode;
                if (fromTransition.getSignal() == signal) {
                    return fromTransition.getNextState();
                }
            }
        }
        return null;
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node instanceof Transition) {
            Transition transition = (Transition) node;
            properties.add(new DirectionPropertyDescriptor(transition));
        }
        return properties;
    }

}
