package org.workcraft.plugins.cpog.serialisation;

import org.w3c.dom.Element;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFormulaDeserialiser;
import org.workcraft.plugins.cpog.RhoClause;
import org.workcraft.serialisation.ReferenceResolver;

public class RhoClauseDeserialiser extends BooleanFormulaDeserialiser<RhoClause> {

    @Override
    public String getClassName() {
        return RhoClause.class.getName();
    }

    @Override
    public RhoClause createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new RhoClause();
    }

    @Override
    protected void setFormula(RhoClause deserialisee, BooleanFormula formula) {
        deserialisee.setFormula(formula);
    }

}
