package org.workcraft.plugins.circuit.expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Factor implements Expression {

    public final Expression expression;

    public Factor(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public String toString() {
        return "(" + expression.toString() + ")";
    }

    @Override
    public Collection<Literal> getLiterals() {
        return expression.getLiterals();
    }

    @Override
    public Expression eval() {
        return eval(new HashMap<>());
    }

    @Override
    public Expression eval(Map<String, Boolean> assignments) {
        Expression evalExpression = expression.eval(assignments);
        if (evalExpression instanceof Constant) {
            Constant constant = (Constant) evalExpression;
            return new Constant(constant.value);
        }
        return evalExpression;
    }

}
