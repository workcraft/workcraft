package org.workcraft.plugins.circuit.expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Negation implements Expression {

	public final Expression expression;

	public Negation(Expression expression) {
        this.expression = expression;
    }

	@Override
	public boolean isAtomic() {
		return true;
	}

    @Override
    public String toString() {
    	if (expression.isAtomic()) {
    		return expression.toString() + "'";
    	} else {
    		return "(" + expression.toString() + ")'";
    	}
    }

	@Override
	public Collection<Literal> getLiterals() {
		return expression.getLiterals();
	}

	@Override
	public Expression eval() {
		return eval(new HashMap<String, Boolean>());
	}

	@Override
	public Expression eval(Map<String, Boolean> assignments) {
		Expression evalExpression = expression.eval(assignments);
		if (evalExpression instanceof Constant) {
			Constant constant = (Constant)evalExpression;
			return new Constant( !constant.value );
		} else {
			return new Negation(evalExpression);
		}
	}

}
