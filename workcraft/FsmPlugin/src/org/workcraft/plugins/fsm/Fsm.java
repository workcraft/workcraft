package org.workcraft.plugins.fsm;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.fsm.observers.InitialStateSupervisor;
import org.workcraft.plugins.fsm.observers.SymbolConsistencySupervisor;
import org.workcraft.serialisation.References;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;

public class Fsm extends AbstractMathModel {

    public static final String EPSILON_SERIALISATION = "epsilon";

    public Fsm() {
        this(null, null);
    }

    public Fsm(Container root, References refs) {
        super(root, refs);
        new InitialStateSupervisor(this).attach(getRoot());
        new SymbolConsistencySupervisor(this).attach(getRoot());
    }

    public State createState(String name) {
        return createNode(name, null, State.class);
    }

    public State getOrCreateState(String name) {
        State state = null;
        Node node = getNodeByReference(name);
        if (node == null) {
            state = createState(name);
        } else if (node instanceof State) {
            state = (State) node;
        } else {
            throw new ArgumentException("Node '" + name + "' is not a state.");
        }
        return state;
    }

    public Symbol createSymbol(String name) {
        return createNode(name, null, Symbol.class);
    }

    public Symbol getOrCreateSymbol(String name) {
        Symbol symbol = null;
        Node node = getNodeByReference(name);
        if (node == null) {
            symbol = createSymbol(name);
        } else if (node instanceof Symbol) {
            symbol = (Symbol) node;
        } else {
            throw new ArgumentException("Node '" + name + "' already exists and it is not a symbol.");
        }
        return symbol;
    }

    public Event createEvent(State first, State second, Symbol symbol) {
        Event event = new Event(first, second, symbol);
        Container container = Hierarchy.getNearestContainer(first, second);
        container.add(event);
        return event;
    }

    public final Collection<State> getStates() {
        return Hierarchy.getDescendantsOfType(getRoot(), State.class);
    }

    public final Collection<Symbol> getSymbols() {
        return Hierarchy.getDescendantsOfType(getRoot(), Symbol.class);
    }

    public boolean isDeterministicSymbol(Symbol symbol) {
        return symbol != null;
    }

    public final Collection<Event> getEvents() {
        return Hierarchy.getDescendantsOfType(getRoot(), Event.class);
    }

    public final Collection<Event> getEvents(final Symbol symbol) {
        return Hierarchy.getDescendantsOfType(getRoot(), Event.class, event -> event.getSymbol() == symbol);
    }

    public State getInitialState() {
        for (State state: getStates()) {
            if (state.isInitial()) {
                return state;
            }
        }
        return null;
    }

    @Override
    public boolean reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<? extends MathNode> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        reparentDependencies(srcModel, srcChildren);
        return super.reparent(dstContainer, srcModel, srcRoot, srcChildren);
    }

    public void reparentDependencies(Model srcModel, Collection<? extends MathNode> srcChildren) {
        for (MathNode srcNode: srcChildren) {
            if (srcNode instanceof Event srcEvent) {
                Symbol dstSymbol = reparentSymbol(srcModel, srcEvent.getSymbol());
                srcEvent.setSymbol(dstSymbol);
            }
        }
    }

    private Symbol reparentSymbol(Model srcModel, Symbol srcSymbol) {
        Symbol dstSymbol = null;
        if (srcSymbol != null) {
            String symbolName = srcModel.getNodeReference(srcSymbol);
            Node dstNode = getNodeByReference(symbolName);
            if (dstNode instanceof Symbol) {
                dstSymbol = (Symbol) dstNode;
            } else {
                if (dstNode != null) {
                    NameManager nameManager = getReferenceManager().getNameManager(null);
                    symbolName = nameManager.getDerivedName(null, symbolName);
                }
                dstSymbol = createSymbol(symbolName);
            }
        }
        return dstSymbol;
    }

}
