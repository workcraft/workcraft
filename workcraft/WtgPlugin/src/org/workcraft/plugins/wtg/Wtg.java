package org.workcraft.plugins.wtg;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.wtg.observers.GuardNameSupervisor;
import org.workcraft.plugins.wtg.observers.InitialStateSupervisor;
import org.workcraft.plugins.wtg.observers.SignalTypeConsistencySupervisor;
import org.workcraft.plugins.wtg.utils.WtgUtils;
import org.workcraft.serialisation.References;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Wtg extends Dtd {

    public Wtg() {
        this(null, null);
    }

    public Wtg(Container root, References refs) {
        super(root, refs);
        new InitialStateSupervisor().attach(getRoot());
        new SignalTypeConsistencySupervisor(this).attach(getRoot());
        new GuardNameSupervisor(this).attach(getRoot());
    }

    @Override
    public void validateConnection(MathNode first, MathNode second) throws InvalidConnectionException {
        super.validateConnection(first, second);

        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }

        if ((first instanceof Waveform) && (second instanceof Waveform)) {
            throw new InvalidConnectionException("Cannot directly connect waveforms.");
        }

        if ((first instanceof State) && (second instanceof State)) {
            throw new InvalidConnectionException("Cannot directly connect states.");
        }

        if ((first instanceof State) && (second instanceof Waveform)) {
            for (Connection connection : getConnections(second)) {
                if ((connection.getFirst() != first) && (connection.getSecond() == second)) {
                    throw new InvalidConnectionException("Waveform cannot have more than one preceding state.");
                }
            }
        }

        if ((first instanceof Waveform) && (second instanceof State)) {
            for (Connection connection : getConnections(second)) {
                if ((connection.getFirst() == first) && (connection.getSecond() != second)) {
                    throw new InvalidConnectionException("Waveform cannot have more than one succeeding state.");
                }
            }
        }
        if ((first instanceof TransitionEvent) && (second instanceof TransitionEvent)) {
            Signal firstSignal = ((TransitionEvent) first).getSignal();
            Signal secondSignal = ((TransitionEvent) second).getSignal();
            Node firstWaveform = firstSignal.getParent();
            Node secondWaveform = secondSignal.getParent();
            if (firstWaveform != secondWaveform) {
                throw new InvalidConnectionException("Cannot connect events from different waveforms.");
            }
        }
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

    public Collection<String> getSignalNames(Signal.Type type) {
        Set<String> result = new HashSet<>();
        for (Signal signal : getSignals(type)) {
            result.add(getName(signal));
        }
        return result;
    }

    public Signal.Type getSignalType(String signalName) {
        if (getSignalNames(Signal.Type.INPUT).contains(signalName)) {
            return Signal.Type.INPUT;
        } else if (getSignalNames(Signal.Type.OUTPUT).contains(signalName)) {
            return Signal.Type.OUTPUT;
        } else if (getSignalNames(Signal.Type.INTERNAL).contains(signalName)) {
            return Signal.Type.INTERNAL;
        }
        return null;
    }

    public final State getInitialState() {
        for (State state: getStates()) {
            if (state.isInitial()) {
                return state;
            }
        }
        return null;
    }

    @Override
    public void anonymise() {
        setTitle("");
        HashSet<String> takenNames = new HashSet<>(getSignalNames());
        for (MathNode node : Hierarchy.getDescendantsOfType(getRoot(), MathNode.class)) {
            String name = getName(node);
            if ((name != null) && !Identifier.hasInternalPrefix(name) && !(node instanceof Signal)) {
                getReferenceManager().setDefaultName(node);
                takenNames.add(getName(node));
            }
        }
        NameManager nameManager = getReferenceManager().getNameManager(null);
        String prefix = nameManager.getPrefix(new Signal());
        int count = 0;
        for (String oldName : getSignalNames()) {
            String newName;
            do {
                newName = prefix + count++;
            } while (takenNames.contains(newName));
            WtgUtils.renameSignal(this, oldName, newName);
            takenNames.add(newName);
        }
    }

}
