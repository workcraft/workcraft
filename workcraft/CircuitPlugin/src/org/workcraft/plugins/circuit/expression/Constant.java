package org.workcraft.plugins.circuit.expression;

import java.util.*;

public class Constant implements Expression {

    public final boolean value;

    public Constant(boolean value) {
        this.value = value;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public String toString() {
        return value ? "1" : "0";
    }

    @Override
    public Collection<Literal> getLiterals() {
        return List.of();
    }

    @Override
    public Expression eval() {
        return eval(new HashMap<>());
    }

    @Override
    public Expression eval(Map<String, Boolean> assignments) {
        return new Constant(value);
    }

}
