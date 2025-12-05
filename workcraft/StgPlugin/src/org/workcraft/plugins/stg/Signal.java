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
            return switch (this) {
                case INPUT -> OUTPUT;
                case OUTPUT -> INPUT;
                default -> this;
            };
        }

        public Type toggle() {
            return switch (this) {
                case INPUT -> OUTPUT;
                case OUTPUT -> INTERNAL;
                case INTERNAL -> INPUT;
            };
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
            return switch (this) {
                case HIGH -> LOW;
                case LOW -> HIGH;
                default -> this;
            };
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
        if (o instanceof Signal s) {
            result = name.equals(s.name) && (type == s.type);
        }
        return result;
    }

}
