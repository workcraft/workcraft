package org.workcraft.plugins.circuit.expression;

import java.util.Collection;
import java.util.Map;

public interface Expression {
    public boolean isAtomic();
    public String toString();
    public Collection<Literal> getLiterals();
    public Expression eval();
    public Expression eval(Map<String, Boolean> assignments);
}
