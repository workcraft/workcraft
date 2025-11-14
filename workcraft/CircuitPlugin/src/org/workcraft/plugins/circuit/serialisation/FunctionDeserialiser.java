package org.workcraft.plugins.circuit.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFormulaDeserialiser;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;
import org.workcraft.serialisation.ReferenceResolver;

public class FunctionDeserialiser implements CustomXMLDeserialiser<FunctionContact> {

    @Override
    public String getClassName() {
        return FunctionContact.class.getName();
    }

    @Override
    public FunctionContact createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new FunctionContact();
    }

    @Override
    public void finaliseInstance(Element element, FunctionContact instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException {

        String setString = element.getAttribute(FunctionSerialiser.SET_FUNCTION_ATTRIBUTE_NAME);
        BooleanFormula setFormula = BooleanFormulaDeserialiser.parseFormula(setString, internalReferenceResolver);
        instance.setSetFunction(setFormula);

        String resetString = element.getAttribute(FunctionSerialiser.RESET_FUNCTION_ATTRIBUTE_NAME);
        BooleanFormula resetFormula = BooleanFormulaDeserialiser.parseFormula(resetString, internalReferenceResolver);
        instance.setResetFunction(resetFormula);
    }

    @Override
    public void initInstance(Element element, FunctionContact instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {
    }

}
