package org.workcraft.plugins.circuit.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class ExpressionUtils {

	public static Expression evalAndSimplify(Expression expression, String name, boolean value) {
		HashMap<String, Boolean> assignments = new HashMap<>();
		assignments.put(name, value);
		Expression result = expression.eval(assignments);

		if (result instanceof Formula) {
			result = simplifyTerms((Formula)result);
		}
		return result;
	}

	private static Expression simplifyTerms(Formula formula) {
		HashSet<String> atomicTermStrings = new HashSet<>();
		for (Expression term: formula.expressions) {
			if (term.isAtomic()) {
				atomicTermStrings.add(term.toString());
			}
		}
		HashSet<String> redundantTerms = new HashSet<>();
		for (Expression term: formula.expressions) {
			if ( !(term instanceof Term) ) continue;
			for (Expression factor: ((Term)term).expressions) {
				String factorString = factor.toString();
				if (factor.isAtomic() && atomicTermStrings.contains(factorString)) {
					redundantTerms.add(factorString);
				}
			}
		}
		LinkedList<Expression> resultTerms = new LinkedList<>();
		for (Expression term: formula.expressions) {
			String termString = term.toString();
			if ( !redundantTerms.contains(termString) ) {
				resultTerms.add(term);
			}
		}
		return new Formula(resultTerms);
	}

}
