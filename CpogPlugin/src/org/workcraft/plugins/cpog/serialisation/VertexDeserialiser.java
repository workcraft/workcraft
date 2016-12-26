package org.workcraft.plugins.cpog.serialisation;

import org.w3c.dom.Element;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFunctionDeserialiser;
import org.workcraft.plugins.cpog.Vertex;
import org.workcraft.serialisation.ReferenceResolver;

public class VertexDeserialiser extends BooleanFunctionDeserialiser {
    @Override
    public String getClassName() {
        return Vertex.class.getName();
    }

    @Override
    protected void setFormula(Object deserialisee, BooleanFormula formula) {
        ((Vertex) deserialisee).setCondition(formula);
    }

    @Override
    public Object createInstance(Element element,
            ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        return new Vertex();
    }

}
