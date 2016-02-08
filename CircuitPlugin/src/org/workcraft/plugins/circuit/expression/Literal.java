package org.workcraft.plugins.circuit.expression;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Literal implements Expression {

    public String name;

    public Literal(String name) {
        this.name = name;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Collection<Literal> getLiterals() {
        return Arrays.asList(this);
    }

    @Override
    public Expression eval() {
        return eval(new HashMap<String, Boolean>());
    }

    @Override
    public Expression eval(Map<String, Boolean> assignments) {
        Boolean value = assignments.get(name);
        if (value != null) {
            return new Constant(value);
        } else {
            return new Literal(name);
        }
    }

}
