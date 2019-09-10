package org.workcraft.formula.utils;

import org.workcraft.formula.Clause;
import org.workcraft.formula.Literal;
import org.workcraft.formula.Nf;
import org.workcraft.utils.SetUtils;

import java.util.*;

public class ClauseUtils {

    // Drop all the repeated and absorbed clauses
    public static <T extends Clause> List<T> extractEssentialClauses(Nf<T> nf) {
        List<T> result = new ArrayList<>();

        Map<T, HashSet<String>> clauseToLiteralsMap = new HashMap<>();
        for (T clause : nf.getClauses()) {
            if (clause.getLiterals().size() == 0) {
                result.add(clause);
                return result;
            }

            HashSet<String> literals = new HashSet<>();
            for (Literal literal : clause.getLiterals()) {
                literals.add(literal.getVariable().getLabel() + (literal.getNegation() ? "'" : ""));
            }
            clauseToLiteralsMap.put(clause, literals);
        }

        for (T leftClause : clauseToLiteralsMap.keySet()) {
            for (T rightClause : clauseToLiteralsMap.keySet()) {
                if (leftClause == rightClause) continue;

                HashSet<String> leftLiterals = clauseToLiteralsMap.get(leftClause);
                if (leftLiterals == null) break;

                HashSet<String> rightLiterals = clauseToLiteralsMap.get(rightClause);
                if (rightLiterals == null) continue;

                if (SetUtils.isFirstSmaller(leftLiterals, rightLiterals, true)) {
                    clauseToLiteralsMap.put(rightClause, null);
                } else if (SetUtils.isFirstSmaller(rightLiterals, leftLiterals, false)) {
                    clauseToLiteralsMap.put(leftClause, null);
                }
            }
        }

        for (T clause : clauseToLiteralsMap.keySet()) {
            if (clauseToLiteralsMap.get(clause) != null) {
                result.add(clause);
            }
        }
        return result;
    }

    public static Set<Set<String>> getLiteralSets(Nf<? extends Clause> nf) {
        Set<Set<String>> result = new HashSet<>();
        for (Clause clause : nf.getClauses()) {
            result.add(getLiteralSet(clause));
        }
        return result;
    }

    public static Set<String> getLiteralSet(Clause clause) {
        Set<String> result = new HashSet<>();
        for (Literal literal : clause.getLiterals()) {
            result.add(literal.getVariable().getLabel() + (literal.getNegation() ? "'" : ""));
        }
        return result;
    }

}
