package org.workcraft.plugins.stg;

public class Signal {

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

        public Type mirror() {
            switch (this) {
            case INPUT: return OUTPUT;
            case OUTPUT: return INPUT;
            default: return this;
            }
        }

        public Type toggle() {
            switch (this) {
            case INPUT: return INTERNAL;
            case OUTPUT: return INPUT;
            case INTERNAL: return OUTPUT;
            default: return this;
            }
        }
    }

    public enum State {
        HIGH("1"),
        LOW("0"),
        UNDEFINED("?");

        private final String name;

        State(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public State toggle() {
            switch (this) {
            case HIGH:
                return LOW;
            case LOW:
                return HIGH;
            default:
                return this;
            }
        }
    }

    public final String name;
    public final Type type;

    public Signal(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public int hashCode() {
        return (name.hashCode() << 1) + type.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Signal) {
            Signal s = (Signal) o;
            result = name.equals(s.name) && type.equals(s.type);
        }
        return result;
    }

}
