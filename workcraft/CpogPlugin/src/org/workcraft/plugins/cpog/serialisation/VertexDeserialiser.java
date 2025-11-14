package org.workcraft.plugins.cpog.serialisation;

import org.w3c.dom.Element;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFormulaDeserialiser;
import org.workcraft.plugins.cpog.Vertex;
import org.workcraft.serialisation.ReferenceResolver;

public class VertexDeserialiser extends BooleanFormulaDeserialiser<Vertex> {

    @Override
    public String getClassName() {
        return Vertex.class.getName();
    }

    @Override
    protected void setFormula(Vertex deserialisee, BooleanFormula formula) {
        deserialisee.setCondition(formula);
    }

    @Override
    public Vertex createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new Vertex();
    }

}
