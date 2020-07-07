package org.workcraft.plugins.circuit.naryformula;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.Nf;
import org.workcraft.formula.visitors.BooleanVisitor;

import java.util.List;

public class SplitForm extends Nf<BooleanFormula> {

    public SplitForm() {
    }

    public SplitForm(BooleanFormula... clauses) {
        super(clauses);
    }

    public SplitForm(List<BooleanFormula> clauses) {
        super(clauses);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        SplitForm result = new SplitForm();
        return result.accept(visitor);
    }

    public long countLevels() {
        return getClauses().stream().filter(f -> !(f instanceof BooleanVariable)).count();
    }

}
