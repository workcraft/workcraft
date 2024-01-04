package org.workcraft.plugins.circuit.expression;

import java.util.*;

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
        return List.of(this);
    }

    @Override
    public Expression eval() {
        return eval(new HashMap<>());
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
