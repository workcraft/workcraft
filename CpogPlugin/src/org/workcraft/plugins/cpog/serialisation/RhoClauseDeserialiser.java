package org.workcraft.plugins.cpog.serialisation;

import org.w3c.dom.Element;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFunctionDeserialiser;
import org.workcraft.plugins.cpog.RhoClause;
import org.workcraft.serialisation.ReferenceResolver;

public class RhoClauseDeserialiser extends BooleanFunctionDeserialiser {
    @Override
    public String getClassName() {
        return RhoClause.class.getName();
    }

    @Override
    public Object createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        return new RhoClause();
    }

    @Override
    protected void setFormula(Object deserialisee, BooleanFormula formula) {
        ((RhoClause) deserialisee).setFormula(formula);
    }
}
