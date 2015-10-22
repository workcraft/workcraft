package org.workcraft.plugins.circuit.expression;

import java.util.LinkedList;
import java.util.List;

public class ExpressionUtils {

	// Extract SET term from an expression SET + seq * RESET'
	public static String extactSetExpression(String expression, String seqLiteral, char termDelimiter, char factorDelimiter) {
		String result = null;
		for (String term: ExpressionUtils.getTerms(expression, termDelimiter)) {
			if ( !isResetTerm(term, seqLiteral, factorDelimiter)) {
				if (result == null) {
					result = "";
				} else {
					result += "+";
				}
				result += term;
			}
		}
		return result;
	}

	// Extract RESET term from an expression SET + seq * RESET'
	public static String extactResetExpression(String expression, String seqLiteral, char termDelimiter, char factorDelimiter) {
		String result = null;
		for (String term: ExpressionUtils.getTerms(expression, termDelimiter)) {
			if (isResetTerm(term, seqLiteral, factorDelimiter)) {
				if (result == null) {
					result = "";
				} else {
					result += termDelimiter;
				}
				result += removeTermLiteral(term, seqLiteral, factorDelimiter);
			}
		}
		if (result != null) {
			List<String> terms = ExpressionUtils.getTerms(result, termDelimiter);
			if (terms.size() == 1) {
				result = ExpressionUtils.negateTerm(result);
			} else {
				result = ExpressionUtils.negateExpression(result);
			}
		}
		return result;
	}

	private static List<String> getTerms(String expression, char termDelimiter) {
		List<String> result = new LinkedList<>();
		int b = 0;
		String term = "";
		for (int i = 0; i < expression.length(); i++){
			char c = expression.charAt(i);
			if (c == ' ') continue;
			if ((c == termDelimiter) && (b == 0)) {
				result.add(term);
				term = "";
			} else {
				term += c;
				if (c == '(') b++;
				if (c == ')') b--;
			}
		}
		if (b == 0) {
			result.add(term);
		}
		return result;
	}

	private static String negateExpression(String expression) {
		return "!(" + expression + ")";
	}

	private static String negateTerm(String expression) {
		String result = null;
		if (expression != null) {
			if (expression.startsWith("!")) {
				result = expression.substring(1, expression.length());
			} else if (expression.endsWith("'")) {
				result = expression.substring(0, expression.length() - 1);
			} else if (expression.startsWith("(")) {
				result = "!" + expression;
			} else {
				result = "!(" + expression + ")";
			}
		}
		return result;
	}

	private static boolean isResetTerm(String term, String seqLiteral, char factorDelimiter) {
		boolean result = false;
		if (term.startsWith(seqLiteral + factorDelimiter)) {
			result = true;
		} else if (term.endsWith(factorDelimiter + seqLiteral)) {
			result = true;
		} else if (term.contains(factorDelimiter + seqLiteral + factorDelimiter)) {
			result = true;
		}
		return result;
	}

	private static String removeTermLiteral(String term, String literal, char factorDelimiter) {
		String result = null;
		if (term.startsWith(literal + factorDelimiter)) {
			result = term.replaceAll(literal + factorDelimiter, "");
		} else if (term.endsWith(factorDelimiter + literal)) {
			result = term.replaceAll(factorDelimiter + literal, "");
		} else if (term.contains(factorDelimiter + literal + factorDelimiter)) {
			result = term.replaceAll(factorDelimiter + literal + factorDelimiter, "");
		} else {
			result = term;
		}
		return result;
	}

}
