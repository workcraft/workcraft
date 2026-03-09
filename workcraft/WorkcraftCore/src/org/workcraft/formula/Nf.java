package org.workcraft.formula;

import org.workcraft.formula.visitors.BooleanVisitor;

import java.util.ArrayList;
import java.util.List;

public abstract class Nf<C> implements BooleanFormula {

    private final List<C> clauses = new ArrayList<>();

    public Nf() {
    }

    public Nf(C clause) {
        this.clauses.add(clause);
    }

    public Nf(List<C> clauses) {
        this.clauses.addAll(clauses);
    }

    public List<C> getClauses() {
        return clauses;
    }

    public void addClause(C clause) {
        clauses.add(clause);
    }

    private void addClauses(List<C> clauseList) {
        clauses.addAll(clauseList);
    }

    public void add(Nf<C> nf) {
        addClauses(nf.getClauses());
    }

    public void addAll(List<? extends Nf<C>> nfs) {
        for (Nf<C> nf : nfs) {
            add(nf);
        }
    }

    @Override
    public abstract <T> T accept(BooleanVisitor<T> visitor);

}
