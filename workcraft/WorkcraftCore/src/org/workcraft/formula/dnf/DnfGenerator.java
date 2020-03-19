package org.workcraft.formula.dnf;

import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanVisitor;

public class DnfGenerator {

    private static class DnfVisitor implements BooleanVisitor<Dnf> {

        private boolean negation = false;

        @Override
        public Dnf visit(Zero node) {
            return negation ? new Dnf(new DnfClause()) : new Dnf();
        }

        @Override
        public Dnf visit(One node) {
            negation = !negation;
            Dnf result = negation ? new Dnf(new DnfClause()) : new Dnf();
            negation = !negation;
            return result;
        }

        @Override
        public Dnf visit(BooleanVariable variable) {
            return new Dnf(new DnfClause(new Literal(variable, negation)));
        }

        @Override
        public Dnf visit(Not node) {
            negation = !negation;
            try {
                return node.getX().accept(this);
            } finally {
                negation = !negation;
            }
        }

        @Override
        public Dnf visit(And node) {
            return and(node.getX().accept(this), node.getY().accept(this));
        }

        @Override
        public Dnf visit(Or node) {
            return or(node.getX().accept(this), node.getY().accept(this));
        }

        @Override
        public Dnf visit(Xor node) {
            Dnf a = node.getX().accept(this);
            Dnf b = node.getY().accept(this);
            negation = !negation;
            Dnf na = node.getX().accept(this);
            Dnf nb = node.getY().accept(this);
            negation = !negation;
            return or(and(a, nb), and(na, b));
        }

        @Override
        public Dnf visit(Imply node) {
            negation = !negation;
            Dnf x = node.getX().accept(this);
            negation = !negation;
            Dnf y = node.getY().accept(this);
            return or(x, y);
        }

        @Override
        public Dnf visit(Iff node) {
            Dnf a = node.getX().accept(this);
            Dnf b = node.getY().accept(this);
            negation = !negation;
            Dnf na = node.getX().accept(this);
            Dnf nb = node.getY().accept(this);
            negation = !negation;
            return or(and(a, b), and(na, nb));
        }

        private Dnf and(Dnf left, Dnf right) {
            return negation ? addDnf(left, right) : multiplyDnf(left, right);
        }

        private Dnf or(Dnf left, Dnf right) {
            return negation ? multiplyDnf(left, right) : addDnf(left, right);
        }

        private static Dnf addDnf(Dnf left, Dnf right) {
            Dnf result = new Dnf();
            result.add(left);
            result.add(right);
            return simplifyDnf(result);
        }

        private static Dnf multiplyDnf(Dnf left, Dnf right) {
            Dnf result = new Dnf();
            for (DnfClause leftClause : left.getClauses()) {
                for (DnfClause rightClause : right.getClauses()) {
                    boolean foundSameLiteral;
                    boolean clauseDiscarded = false;
                    boolean sameNegation = false;
                    DnfClause newClause = new DnfClause();
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
            return simplifyDnf(result);
        }

        private static Dnf simplifyDnf(Dnf dnf) {
            return new Dnf(ClauseUtils.extractEssentialClauses(dnf));
        }

    }

    public static Dnf generate(BooleanFormula formula) {
        if (formula == null) {
            formula = One.getInstance();
        }
        return formula.accept(new DnfVisitor());
    }

}
