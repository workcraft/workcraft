package org.workcraft.plugins.circuit.expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Term implements Expression {

    public final List<Expression> expressions;

    public Term(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Expression expression: expressions) {
            if (!first) {
                result.append("*");
            }
            result.append(expression.toString());
            first = false;
        }
        return result.toString();
    }

    @Override
    public Collection<Literal> getLiterals() {
        Collection<Literal> result = new HashSet<>();
        for (Expression expression: expressions) {
            result.addAll(expression.getLiterals());
        }
        return result;
    }

    @Override
    public Expression eval() {
        return eval(new HashMap<>());
    }

    @Override
    public Expression eval(Map<String, Boolean> assignments) {
        List<Expression> evalExpressions = new LinkedList<>();
        for (Expression expression: expressions) {
            Expression evalExpression = expression.eval(assignments);
            if (evalExpression instanceof Constant constant) {
                if (!constant.value) {
                    return new Constant(false);
                }
            } else {
                evalExpressions.add(evalExpression);
            }
        }
        if (evalExpressions.isEmpty()) {
            return new Constant(true);
        } else if (evalExpressions.size() == 1) {
            return evalExpressions.get(0);
        } else {
            return new Term(evalExpressions);
        }
    }

}
