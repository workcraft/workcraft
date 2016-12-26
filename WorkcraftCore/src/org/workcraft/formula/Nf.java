package org.workcraft.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Nf<C> implements BooleanFormula {

    private List<C> clauses = new ArrayList<>();

    public Nf() {
    }

    public Nf(C... clauses) {
        this(Arrays.asList(clauses));
    }

    public Nf(List<C> clauses) {
        this.clauses = new ArrayList<C>(clauses);
    }

    public void setClauses(List<C> clauses) {
        this.clauses = new ArrayList<C>(clauses);
    }

    public List<C> getClauses() {
        return clauses;
    }

    public void add(List<C> list) {
        clauses.addAll(list);
    }

    public void add(C... arr) {
        clauses.addAll(Arrays.asList(arr));
    }

    public abstract <T> T accept(BooleanVisitor<T> visitor);

    public void add(Nf<C> nf) {
        add(nf.getClauses());
    }
}
