package org.workcraft.plugins.wtg;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.wtg.observers.InitialStateSupervisor;
import org.workcraft.plugins.wtg.observers.SignalTypeConsistencySupervisor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@VisualClass(org.workcraft.plugins.wtg.VisualWtg.class)
public class Wtg extends Dtd {

    public Wtg() {
        this(null, null);
    }

    public Wtg(Container root, References refs) {
        super(root, refs);
        new InitialStateSupervisor().attach(getRoot());
        new SignalTypeConsistencySupervisor(this).attach(getRoot());
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

    public Collection<String> getSignalNames() {
        Set<String> result = new HashSet<>();
        for (Signal signal : getSignals()) {
            result.add(getName(signal));
        }
        return result;
    }

    public final State getInitialState() {
        for (State state: getStates()) {
            if (state.isInitial()) {
                return state;
            }
        }
        return null;
    }

}
