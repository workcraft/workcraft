package org.workcraft.plugins.dtd;

import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("Signal")
@VisualClass(org.workcraft.plugins.dtd.VisualSignal.class)
public class Signal extends MathNode implements ObservableHierarchy, Container {

    public static final String PROPERTY_NAME = "Name";
    public static final String PROPERTY_TYPE = "Type";
    public static final String PROPERTY_INITIAL_STATE = "Initial state";

    public enum State {
        HIGH("1", "high"),
        LOW("0",  "low"),
        UNSTABLE("*", "unstable"),
        STABLE("?", "stable");

        private final String symbol;
        private final String description;

        State(String symbol, String description) {
            this.symbol = symbol;
            this.description = description;
        }

        public String getSymbol() {
            return symbol;
        }

        public static State fromSymbol(String symbol) {
            for (State item : State.values()) {
                if ((symbol != null) && (symbol.equals(item.symbol))) {
                    return item;
                }
            }
            throw new ArgumentException("Unexpected string: " + symbol);
        }

        @Override
        public String toString() {
            return symbol + " (" + description + ")";
        }

        public State reverse() {
            switch (this) {
            case HIGH: return LOW;
            case LOW: return HIGH;
            case UNSTABLE: return STABLE;
            case STABLE: return UNSTABLE;
            default: return this;
            }
        }
    }

    public enum Type {
        INPUT("input"),
        OUTPUT("output"),
        INTERNAL("internal");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);
    private Type type = Type.OUTPUT;
    private State initialState = State.LOW;

    public Type getType() {
        return type;
    }

    public void setType(Type value) {
        if (type != value) {
            type = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TYPE));
        }
    }

    public State getInitialState() {
        return initialState;
    }

    public void setInitialState(State value) {
        if (initialState != value) {
            initialState = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_INITIAL_STATE));
        }
    }

    @Override
    public Node getParent() {
        return groupImpl.getParent();
    }

    @Override
    public void setParent(Node parent) {
        groupImpl.setParent(parent);
    }

    @Override
    public void addObserver(HierarchyObserver obs) {
        groupImpl.addObserver(obs);
    }

    @Override
    public void removeObserver(HierarchyObserver obs) {
        groupImpl.removeObserver(obs);
    }

    @Override
    public void add(Node node) {
        groupImpl.add(node);
    }

    @Override
    public void add(Collection<Node> nodes) {
        groupImpl.add(nodes);
    }

    @Override
    public void remove(Node node) {
        groupImpl.remove(node);
    }

    @Override
    public void remove(Collection<Node> node) {
        groupImpl.remove(node);
    }

    @Override
    public void reparent(Collection<Node> nodes) {
        groupImpl.reparent(nodes);
    }

    @Override
    public void reparent(Collection<Node> nodes, Container newParent) {
        groupImpl.reparent(nodes, newParent);
    }

    @Override
    public Collection<Node> getChildren() {
        return groupImpl.getChildren();
    }

    @Override
    public void removeAllObservers() {
        groupImpl.removeAllObservers();
    }

}

