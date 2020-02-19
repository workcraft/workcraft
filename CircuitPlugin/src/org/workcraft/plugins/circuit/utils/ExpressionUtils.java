package org.workcraft.plugins.circuit.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class ExpressionUtils {
    private static final char TERM_DELIMITER = '+';
    private static final char FACTOR_DELIMITER = '*';
    private static final char NEGATION_DELIMITER = '!';

    // Extract SET term from an expression SET + seq * RESET'
    public static String extactSetExpression(String expression, String seqLiteral) {
        String result = "";
        for (String term: getTerms(expression)) {
            if (!isResetTerm(term, seqLiteral)) {
                if (!result.isEmpty()) {
                    result += TERM_DELIMITER;
                }
                result += term;
            }
        }
        return result;
    }

    // Extract RESET term from an expression SET + seq * RESET'
    public static String extactResetExpression(String expression, String seqLiteral) {
        String result = "";
        for (String term: getTerms(expression)) {
            if (isResetTerm(term, seqLiteral)) {
                if (!result.isEmpty()) {
                    result += TERM_DELIMITER;
                }
                result += removeTermLiteral(term, seqLiteral);
            }
        }
        return negateExpression(result);
    }

    private static List<String> getTerms(String expression) {
        List<String> result = new LinkedList<>();
        int bracketCount = 0;
        String term = "";
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == ' ') continue;
            if ((c == TERM_DELIMITER) && (bracketCount == 0)) {
                result.add(term);
                term = "";
            } else {
                term += c;
                if (c == '(') bracketCount++;
                if (c == ')') bracketCount--;
            }
        }
        if (bracketCount == 0) {
            result.add(term);
        }
        return result;
    }

    private static String negateExpression(String expression) {
        if (expression.isEmpty()) {
            return "";
        }
        if (expression.contains("" + TERM_DELIMITER) || expression.contains("" + FACTOR_DELIMITER)) {
            return NEGATION_DELIMITER + "(" + expression + ")";
        }
        if (expression.startsWith("!")) {
            return expression.substring(1);
        }
        if (expression.endsWith("'")) {
            return expression.substring(0, expression.length() - 1);
        }
        return "!" + expression;
    }

    private static boolean isResetTerm(String term, String literal) {
        return term.startsWith(literal + FACTOR_DELIMITER)
              || term.endsWith(FACTOR_DELIMITER + literal)
              || term.contains(FACTOR_DELIMITER + literal + FACTOR_DELIMITER)
              || term.equals(literal);
    }

    private static String removeTermLiteral(String term, String literal) {
        String result = term;
        if (result.startsWith(literal + FACTOR_DELIMITER)) {
            String pattern = Pattern.quote(literal + FACTOR_DELIMITER);
            result = result.replaceAll(pattern, "");
        }
        if (result.endsWith(FACTOR_DELIMITER + literal)) {
            String pattern = Pattern.quote(FACTOR_DELIMITER + literal);
            result = result.replaceAll(pattern, "");
        }
        if (result.contains(FACTOR_DELIMITER + literal + FACTOR_DELIMITER)) {
            String pattern = Pattern.quote(FACTOR_DELIMITER + literal + FACTOR_DELIMITER);
            result = result.replaceAll(pattern, "" + FACTOR_DELIMITER);
        }
        if (result.equals(literal)) {
            result = "";
        }
        return result;
    }

}
