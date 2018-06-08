package org.workcraft.plugins.circuit.verilog;

import org.workcraft.util.Pair;

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

    public static class Range extends Pair<Integer, Integer> {
        public Range(Integer first, Integer second) {
            super(first, second);
        }

        public int getMin() {
            return Math.min(getFirst(), getSecond());
        }

        public int getMax() {
            return Math.max(getFirst(), getSecond());
        }

        @Override
        public String toString() {
            return "[" + Integer.toString(getFirst()) + ":" + Integer.toString(getSecond()) + "]";
        }
    }

    public final String name;
    public final Type type;
    public final Range range;

    public Port(String name, Type type) {
        this(name, type, null);
    }

    public Port(String name, Type type, Range range) {
        this.name = name;
        this.type = type;
        this.range = range;
    }

    public boolean isInput() {
        return type == Type.INPUT;
    }

    public boolean isOutput() {
        return type == Type.OUTPUT;
    }

}
