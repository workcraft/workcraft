package org.workcraft.plugins.fst;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fst.observers.SignalConsistencySupervisor;
import org.workcraft.serialisation.References;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;

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

    public SignalEvent createSignalEvent(State first, State second, Signal signal) {
        Container container = Hierarchy.getNearestContainer(first, second);
        SignalEvent event = new SignalEvent(first, second, signal);
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

    public final Collection<SignalEvent> getSignalEvents(Signal signal) {
        return Hierarchy.getDescendantsOfType(getRoot(), SignalEvent.class, event -> event.getSymbol() == signal);
    }

    @Override
    public void reparentDependencies(Model srcModel, Collection<? extends MathNode> srcChildren) {
        for (MathNode srcNode: srcChildren) {
            if (srcNode instanceof SignalEvent) {
                SignalEvent srcSignalEvent = (SignalEvent) srcNode;
                Signal dstSignal = reparentSignal(srcModel, srcSignalEvent.getSymbol());
                srcSignalEvent.setSymbol(dstSignal);
            }
        }
    }

    private Signal reparentSignal(Model srcModel, Signal srcSignal) {
        Signal dstSignal = null;
        if (srcSignal != null) {
            String signalName = srcModel.getNodeReference(srcSignal);
            Node dstNode = getNodeByReference(signalName);
            if (dstNode instanceof Signal) {
                dstSignal = (Signal) dstNode;
            } else {
                if (dstNode != null) {
                    NameManager nameManager = getReferenceManager().getNameManager(null);
                    signalName = nameManager.getDerivedName(null, signalName);
                }
                dstSignal = createSignal(signalName, srcSignal.getType());
            }
        }
        return dstSignal;
    }

}
