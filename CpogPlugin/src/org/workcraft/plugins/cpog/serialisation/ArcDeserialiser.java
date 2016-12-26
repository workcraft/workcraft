package org.workcraft.plugins.cpog.serialisation;

import org.w3c.dom.Element;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFunctionDeserialiser;
import org.workcraft.plugins.cpog.Arc;
import org.workcraft.serialisation.ReferenceResolver;

public class ArcDeserialiser extends BooleanFunctionDeserialiser {
    @Override
    public String getClassName() {
        return Arc.class.getName();
    }

    @Override
    protected void setFormula(Object deserialisee, BooleanFormula formula) {
        ((Arc) deserialisee).setCondition(formula);
    }

    @Override
    public Object createInstance(Element element,
            ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        return new Arc();
    }

}
