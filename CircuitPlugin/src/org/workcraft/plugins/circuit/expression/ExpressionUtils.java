package org.workcraft.plugins.circuit.expression;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class ExpressionUtils {
    private static final char TERM_DELIMITER = '+';
    private static final char FACTOR_DELIMITER = '*';
    private static final char NEGATION_DELIMITER = '!';

    // Extract SET term from an expression SET + seq * RESET'
    public static String extactSetExpression(String expression, String seqLiteral) {
        String result = null;
        for (String term: ExpressionUtils.getTerms(expression)) {
            if (!isResetTerm(term, seqLiteral)) {
                if (result == null) {
                    result = "";
                } else {
                    result += TERM_DELIMITER;
                }
                result += term;
            }
        }
        return result;
    }

    // Extract RESET term from an expression SET + seq * RESET'
    public static String extactResetExpression(String expression, String seqLiteral) {
        String result = null;
        for (String term: ExpressionUtils.getTerms(expression)) {
            if (isResetTerm(term, seqLiteral)) {
                if (result == null) {
                    result = "";
                } else {
                    result += TERM_DELIMITER;
                }
                result += removeTermLiteral(term, seqLiteral);
            }
        }
        if (result == null) {
            result = "";
        } else {
            result = ExpressionUtils.negateExpression(result);
        }
        return result;
    }

    private static List<String> getTerms(String expression) {
        List<String> result = new LinkedList<>();
        int b = 0;
        String term = "";
        for (int i = 0; i < expression.length(); i++){
            char c = expression.charAt(i);
            if (c == ' ') continue;
            if ((c == TERM_DELIMITER) && (b == 0)) {
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
        String result = null;
        if (expression.contains(""+TERM_DELIMITER) || expression.contains(""+FACTOR_DELIMITER)) {
            result= NEGATION_DELIMITER + "(" + expression + ")";
        } else {
            if (expression.startsWith("!")) {
                result = expression.substring(1, expression.length());
            } else if (expression.endsWith("'")) {
                result = expression.substring(0, expression.length() - 1);
            } else {
                result = "!" + expression;
            }
        }
        return result;
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
            result = result.replaceAll(pattern, "");
        }
        if (result.equals(literal)) {
            result = "";
        }
        return result;
    }

}
