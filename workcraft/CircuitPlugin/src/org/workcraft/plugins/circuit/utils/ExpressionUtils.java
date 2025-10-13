package org.workcraft.plugins.circuit.utils;

import org.workcraft.formula.*;
import org.workcraft.formula.bdd.BddManager;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.formula.workers.CleverBooleanWorker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class ExpressionUtils {

    private static final char TERM_DELIMITER = '+';
    private static final char FACTOR_DELIMITER = '*';
    private static final char NEGATION_DELIMITER = '!';
    private static final String ESCAPED_FACTOR_DELIMITER = "\\" + FACTOR_DELIMITER;

    public static String extractSetFunction(String expression, String seqLiteral) {
        return extractFunction(expression, seqLiteral, false, ExpressionUtils::extractHeuristicSetFunction);
    }

    public static String extractResetFunction(String expression, String seqLiteral) {
        return extractFunction(expression, seqLiteral, true, ExpressionUtils::extractHeuristicResetFunction);
    }

    // Extract SET term from an expression SET + seq * RESET'
    public static String extractHeuristicSetFunction(String expression, String seqLiteral) {
        StringBuilder result = new StringBuilder();
        for (String term: getTerms(expression)) {
            if (!isResetTerm(term, seqLiteral)) {
                if (!result.isEmpty()) {
                    result.append(TERM_DELIMITER);
                }
                result.append(term);
            }
        }
        return result.toString();
    }

    // Extract RESET term from an expression SET + seq * RESET'
    public static String extractHeuristicResetFunction(String expression, String seqLiteral) {
        StringBuilder result = new StringBuilder();
        for (String term: getTerms(expression)) {
            if (isResetTerm(term, seqLiteral)) {
                if (!result.isEmpty()) {
                    result.append(TERM_DELIMITER);
                }
                result.append(removeTermLiteral(term, seqLiteral));
            }
        }
        return negateExpression(result.toString());
    }

    private static List<String> getTerms(String expression) {
        List<String> result = new LinkedList<>();
        int bracketCount = 0;
        StringBuilder term = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == ' ') continue;
            if ((c == TERM_DELIMITER) && (bracketCount == 0)) {
                result.add(term.toString());
                term = new StringBuilder();
            } else {
                term.append(c);
                if (c == '(') bracketCount++;
                if (c == ')') bracketCount--;
            }
        }
        if (bracketCount == 0) {
            result.add(term.toString());
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

        Pattern boundaryPattern = Pattern.compile('^' + literal + '$'
                + '|' + '^' + literal + ESCAPED_FACTOR_DELIMITER
                + '|' + ESCAPED_FACTOR_DELIMITER + literal + '$');

        Pattern middlePattern = Pattern.compile(ESCAPED_FACTOR_DELIMITER + literal + ESCAPED_FACTOR_DELIMITER);

        return middlePattern.matcher(
                boundaryPattern.matcher(term).replaceAll("")
        ).replaceAll("" + FACTOR_DELIMITER);
    }

    private static String extractFunction(String expression, String seqLiteral,
            boolean seqValue, BiFunction<String, String, String> heuristic) {

        FreeVariable seqVariable = new FreeVariable(seqLiteral);
        Map<String, BooleanVariable> literalToVariableMap = new HashMap<>();
        literalToVariableMap.put(seqLiteral, seqVariable);

        BooleanFormula resultFormula = null;
        try {
            BooleanFormula formula = BooleanFormulaParser.parse(expression,
                    literal -> literalToVariableMap.computeIfAbsent(literal, FreeVariable::new),
                    CleverBooleanWorker.getInstance());

            if (formula != null) {
                BooleanFormula exactFormula = seqValue
                        ? FormulaUtils.replaceOne(new Not(formula), seqVariable)
                        : FormulaUtils.replaceZero(formula, seqVariable);

                if (exactFormula != null) {
                    String heuristicExpression = heuristic.apply(expression, seqLiteral);
                    BooleanFormula heuristicFormula = BooleanFormulaParser.parse(heuristicExpression,
                            literal -> literalToVariableMap.computeIfAbsent(literal, FreeVariable::new),
                            CleverBooleanWorker.getInstance());

                    if (heuristicFormula == null) {
                        resultFormula = exactFormula;
                    } else {
                        BddManager bddManager = new BddManager();
                        resultFormula = bddManager.isEquivalent(heuristicFormula, exactFormula) ? heuristicFormula : exactFormula;
                    }
                }
            }
        } catch (ParseException ignored) {
        }
        return StringGenerator.toString(resultFormula, StringGenerator.Style.GENLIB);
    }

}
