package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.graph.Symbol;

@DisplayName("Signal")
@VisualClass(org.workcraft.plugins.dtd.VisualSignal.class)
public class Signal extends Symbol {

    public static final String PROPERTY_TYPE = "Type";
    public static final String PROPERTY_INITIAL_STATE = "Initial state";

    public enum State {
        HIGH("1 (high)"),
        LOW("0 (low)"),
        UNSTABLE("* (unstable)"),
        STABLE("? (stable)");

        private final String name;

        State(String name) {
            this.name = name;
        }

        public static State fromString(String s) {
            for (State item : State.values()) {
                if ((s != null) && (s.equals(item.name))) {
                    return item;
                }
            }
            throw new ArgumentException("Unexpected string: " + s);
        }

        @Override
        public String toString() {
            return name;
        }

        public State reverse() {
            switch (this) {
            case HIGH: return LOW;
            case LOW: return HIGH;
            case UNSTABLE: return STABLE;
            case STABLE: return UNSTABLE;
            }
            return null;
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

        public static Type fromString(String s) {
            for (Type item : Type.values()) {
                if ((s != null) && (s.equals(item.name))) {
                    return item;
                }
            }
            throw new ArgumentException("Unexpected string: " + s);
        }

        @Override
        public String toString() {
            return name;
        }
    }

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

}

