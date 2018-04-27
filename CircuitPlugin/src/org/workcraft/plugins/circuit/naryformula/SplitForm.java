package org.workcraft.plugins.circuit.naryformula;

import java.util.List;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.formula.Nf;

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

}
