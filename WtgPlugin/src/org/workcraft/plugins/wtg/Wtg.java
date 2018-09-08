package org.workcraft.plugins.wtg;

import java.util.Collection;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.plugins.dtd.*;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;

@VisualClass(org.workcraft.plugins.wtg.VisualWtg.class)
public class Wtg extends Dtd {

    public Wtg() {
        this(null, (References) null);
    }

    public Wtg(Container root, References refs) {
        this(root, new HierarchicalUniqueNameReferenceManager(refs) {
            @Override
            public String getPrefix(Node node) {
                if (node instanceof EntryEvent) return Identifier.createInternal("entry");
                if (node instanceof ExitEvent) return Identifier.createInternal("exit");
                if (node instanceof TransitionEvent) return Identifier.createInternal("t");
                if (node instanceof Signal) return "x";
                if (node instanceof State) return "s";
                if (node instanceof Waveform) return "w";
                return super.getPrefix(node);
            }
        });
    }

    public Wtg(Container root, ReferenceManager man) {
        super(root, man);
        new InitialStateSupervisor().attach(getRoot());
    }

    public final Collection<State> getStates() {
        return Hierarchy.getDescendantsOfType(getRoot(), State.class);
    }

    public final Collection<Waveform> getWaveforms() {
        return Hierarchy.getDescendantsOfType(getRoot(), Waveform.class);
    }

    public final Collection<Signal> getSignals(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, Signal.class);
    }

    public final Collection<Event> getEvents(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, Event.class);
    }

    public final Collection<TransitionEvent> getTransitions(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, TransitionEvent.class);
    }

    public final Collection<EntryEvent> getEntries(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, EntryEvent.class);
    }

    public final Collection<ExitEvent> getExits(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, ExitEvent.class);
    }

}
