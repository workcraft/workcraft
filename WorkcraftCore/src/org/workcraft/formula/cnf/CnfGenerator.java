package org.workcraft.formula.cnf;

import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanVisitor;

public class CnfGenerator {

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
            return and(node.getX().accept(this), node.getY().accept(this));
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

        private static Cnf addCnf(Cnf left, Cnf right) {
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

        private static Cnf multiplyCnf(Cnf left, Cnf right) {
            Cnf result = new Cnf();
            result.add(left);
            result.add(right);
            return simplifyCnf(result);
        }

        private static Cnf simplifyCnf(Cnf cnf) {
            return new Cnf(ClauseUtils.extractEssentialClauses(cnf));
        }

    }

    public static Cnf generate(BooleanFormula formula) {
        if (formula == null) {
            formula = One.getInstance();
        }
        return formula.accept(new CnfVisitor());
    }

}
