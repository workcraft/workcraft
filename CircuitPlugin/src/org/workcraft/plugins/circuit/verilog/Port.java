package org.workcraft.plugins.circuit.verilog;

public class Port {
    public enum Type {
        INPUT("input"),
        OUTPUT("output");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public final String name;
    public final Type type;

    public Port(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public boolean isInput() {
        return type == Type.INPUT;
    }

    public boolean isOutput() {
        return type == Type.OUTPUT;
    }

}
