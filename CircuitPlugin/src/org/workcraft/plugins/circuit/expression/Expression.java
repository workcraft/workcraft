package org.workcraft.plugins.circuit.expression;

import java.util.Collection;
import java.util.Map;

public interface Expression {
    boolean isAtomic();
    String toString();
    Collection<Literal> getLiterals();
    Expression eval();
    Expression eval(Map<String, Boolean> assignments);
}
