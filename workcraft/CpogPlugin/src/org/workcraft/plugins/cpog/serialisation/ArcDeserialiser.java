package org.workcraft.plugins.cpog.serialisation;

import org.w3c.dom.Element;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFormulaDeserialiser;
import org.workcraft.plugins.cpog.Arc;
import org.workcraft.serialisation.ReferenceResolver;

public class ArcDeserialiser extends BooleanFormulaDeserialiser<Arc> {

    @Override
    public String getClassName() {
        return Arc.class.getName();
    }

    @Override
    protected void setFormula(Arc deserialisee, BooleanFormula formula) {
        deserialisee.setCondition(formula);
    }

    @Override
    public Arc createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new Arc();
    }

}
