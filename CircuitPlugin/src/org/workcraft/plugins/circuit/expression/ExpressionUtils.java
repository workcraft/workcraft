package org.workcraft.plugins.circuit.expression;

import java.util.HashMap;

public class ExpressionUtils {

	public static Expression evalExpression(Expression formula, String name, boolean value) {
		HashMap<String, Boolean> assignments = new HashMap<>();
		assignments.put(name, true);
		return formula.eval(assignments);
	}

}
