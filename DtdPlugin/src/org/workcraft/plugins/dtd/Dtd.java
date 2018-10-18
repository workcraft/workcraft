package org.workcraft.plugins.dtd;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.NamePropertyDescriptor;
import org.workcraft.plugins.dtd.utils.DtdUtils;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

import java.util.Collection;

@VisualClass(org.workcraft.plugins.dtd.VisualDtd.class)
public class Dtd extends AbstractMathModel {

    public Dtd() {
        this(null, null);
    }

    public Dtd(Container root, References refs) {
        super(root, refs);
    }

    public Collection<Signal> getSignals() {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class);
    }

    public Collection<Signal> getSignals(final Signal.Type type) {
        return Hierarchy.getDescendantsOfType(getRoot(), Signal.class,
                signal -> (signal != null) && (signal.getType() == type));
    }

    public Collection<TransitionEvent> getTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), TransitionEvent.class);
    }

    public Collection<TransitionEvent> getTransitions(final Signal signal) {
        return Hierarchy.getDescendantsOfType(getRoot(), TransitionEvent.class,
                transition -> (transition != null) && (transition.getSignal() == signal));
    }

    public Signal.State getPreviousState(Event event) {
        Signal signal = event.getSignal();
        for (Node node: getPreset(event)) {
            if (node instanceof TransitionEvent) {
                TransitionEvent transition = (TransitionEvent) node;
                if (transition.getSignal() == signal) {
                    TransitionEvent.Direction direction = transition.getDirection();
                    return DtdUtils.getNextState(direction);
                }
            }
        }
        return signal.getInitialState();
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node instanceof Event) {
            properties.removeByName(NamePropertyDescriptor.PROPERTY_NAME);
        }
        return properties;
    }

}
