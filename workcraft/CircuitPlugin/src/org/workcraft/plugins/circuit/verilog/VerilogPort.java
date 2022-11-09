package org.workcraft.plugins.circuit.verilog;

import org.workcraft.types.Pair;

public class VerilogPort {

    public final String name;
    public final Type type;
    public final Range range;

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

    public static class Range extends Pair<Integer, Integer> {
        public Range(Integer topIndex, Integer bottomIndex) {
            super(topIndex, bottomIndex);
        }

        public int getTopIndex() {
            return getFirst();
        }

        public int getBottomIndex() {
            return getSecond();
        }

        public int getStep() {
            return Integer.compare(getTopIndex(), getBottomIndex());
        }

        public int getSize() {
            return Math.abs(getTopIndex() - getBottomIndex()) + 1;
        }

        @Override
        public String toString() {
            return "[" + getTopIndex() + ":" + getBottomIndex() + "]";
        }
    }

    public VerilogPort(String name, Type type, Range range) {
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

    public boolean isInternal() {
        return type == Type.INTERNAL;
    }

}
