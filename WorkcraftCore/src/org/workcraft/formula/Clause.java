package org.workcraft.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Clause implements BooleanFormula {

    private List<Literal> literals = new ArrayList<>();

    public Clause() {
    }

    public Clause(Literal... literals) {
        this(Arrays.asList(literals));
    }

    public Clause(List<Literal> literals) {
        this.setLiterals(literals);
    }

    public void setLiterals(List<Literal> literals) {
        this.literals = literals;
    }

    public List<Literal> getLiterals() {
        return literals;
    }

    public void add(List<Literal> list) {
        literals.addAll(list);
    }

    public void add(Literal... arr) {
        literals.addAll(Arrays.asList(arr));
    }

    public abstract <T> T accept(BooleanVisitor<T> visitor);

}
