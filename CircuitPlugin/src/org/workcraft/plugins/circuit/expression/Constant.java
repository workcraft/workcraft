package org.workcraft.plugins.circuit.expression;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        return (value ? "1" : "0");
    }

    @Override
    public Collection<Literal> getLiterals() {
        return Arrays.asList();
    }

    @Override
    public Expression eval() {
        return eval(new HashMap<String, Boolean>());
    }

    @Override
    public Expression eval(Map<String, Boolean> assignments) {
        return new Constant(value);
    }

}