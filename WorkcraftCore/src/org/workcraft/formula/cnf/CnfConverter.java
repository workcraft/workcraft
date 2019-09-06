package org.workcraft.formula.cnf;

import org.workcraft.formula.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CnfConverter {

    private static class CnfVisitor implements BooleanVisitor<Cnf> {

        private boolean negation = false;

        @Override
        public Cnf visit(Zero node) {
            return negation ? new Cnf(new CnfClause()) : new Cnf();
        }

        @Override
        public Cnf visit(One node) {
            negation = !negation;
            Cnf result = negation ? new Cnf(new CnfClause()) : new Cnf();
            negation = !negation;
            return result;
        }

        @Override
        public Cnf visit(BooleanVariable variable) {
            return new Cnf(new CnfClause(new Literal(variable, negation)));
        }

        @Override
        public Cnf visit(Not node) {
            negation = !negation;
            try {
                return node.getX().accept(this);
            } finally {
                negation = !negation;
            }
        }

        @Override
        public Cnf visit(And node) {
            Cnf left = node.getX().accept(this);
            Cnf right = node.getY().accept(this);
            return and(left, right);
        }

        @Override
        public Cnf visit(Or node) {
            return or(node.getX().accept(this), node.getY().accept(this));
        }

        @Override
        public Cnf visit(Xor node) {
            Cnf a = node.getX().accept(this);
            Cnf b = node.getY().accept(this);
            negation = !negation;
            Cnf na = node.getX().accept(this);
            Cnf nb = node.getY().accept(this);
            negation = !negation;
            return or(and(a, nb), and(na, b));
        }

        @Override
        public Cnf visit(Imply node) {
            negation = !negation;
            Cnf x = node.getX().accept(this);
            negation = !negation;
            Cnf y = node.getY().accept(this);
            return or(x, y);
        }

        @Override
        public Cnf visit(Iff node) {
            Cnf a = node.getX().accept(this);
            Cnf b = node.getY().accept(this);
            negation = !negation;
            Cnf na = node.getX().accept(this);
            Cnf nb = node.getY().accept(this);
            negation = !negation;
            return or(and(a, b), and(na, nb));
        }

        private Cnf and(Cnf left, Cnf right) {
            return negation ? addCnf(left, right) : multiplyCnf(left, right);
        }

        private Cnf or(Cnf left, Cnf right) {
            return negation ? multiplyCnf(left, right) : addCnf(left, right);
        }
    }

    public static Cnf generate(BooleanFormula formula) {
        if (formula == null) {
            formula = One.instance();
        }
        return formula.accept(new CnfVisitor());
    }

    private static boolean isFirstSmaller(HashSet<String> set1,
            HashSet<String> set2, boolean equalWins) {

        if (set2.containsAll(set1)) {
            if (set2.size() > set1.size()) return true;
            return equalWins;
        }

        return false;
    }

    // throws out all the repeated and absorbed clauses
    private static Cnf simplifyCnf(Cnf clauses) {
        Cnf result = new Cnf();

        Map<CnfClause, HashSet<String>> testClauses = new HashMap<>();

        for (CnfClause clause: clauses.getClauses()) {

            if (clause.getLiterals().size() == 0) return  new Cnf(new CnfClause());

            HashSet<String> lset = new HashSet<>();

            for (Literal lit: clause.getLiterals()) {
                lset.add(lit.getVariable().getLabel() + (lit.getNegation() ? "'" : ""));
            }

            testClauses.put(clause, lset);
        }

        for (CnfClause cleft: testClauses.keySet()) {
            for (CnfClause cright: testClauses.keySet()) {
                if (cleft == cright) continue;

                if (testClauses.get(cleft) == null) break;
                if (testClauses.get(cright) == null) continue;

                // left to right comparison
                if (isFirstSmaller(testClauses.get(cleft), testClauses.get(cright), true)) {
                    testClauses.put(cright, null);
                } else if (isFirstSmaller(testClauses.get(cright), testClauses.get(cleft), false)) {
                    // right to left comparison
                    testClauses.put(cleft, null);
                }
            }
        }

        for (CnfClause cleft: testClauses.keySet()) {
            if (testClauses.get(cleft) != null) {
                result.addClauses(cleft);
            }
        }
        return result;
    }

    private static Cnf addCnf(Cnf left, Cnf right) {
        Cnf result = new Cnf();
        result.add(left);
        result.add(right);
        return simplifyCnf(result);
    }

    private static Cnf multiplyCnf(Cnf left, Cnf right) {
        Cnf result = new Cnf();
        for (CnfClause leftClause : left.getClauses()) {
            for (CnfClause rightClause : right.getClauses()) {
                boolean foundSameLiteral;
                boolean clauseDiscarded = false;
                boolean sameNegation = false;
                CnfClause newClause = new CnfClause();
                newClause.add(leftClause.getLiterals());

                for (Literal rlit : rightClause.getLiterals()) {
                    foundSameLiteral = false;
                    for (Literal llit : leftClause.getLiterals()) {
                        // TODO: work with 0 and 1 literals
                        if (rlit.getVariable().getLabel().equals(llit.getVariable().getLabel())) {
                            foundSameLiteral = true;
                            sameNegation = llit.getNegation() == rlit.getNegation();
                            break;
                        }
                    }
                    if (!foundSameLiteral) newClause.add(rlit);
                    else if (!sameNegation) {
                        clauseDiscarded = true;
                        break;
                    }
                }
                if (!clauseDiscarded) result.addClauses(newClause);
            }

        }
        return simplifyCnf(result);
    }

}
