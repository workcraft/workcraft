package org.workcraft.plugins.circuit.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFunctionDeserialiser;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

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
        String processedSetString = NamespaceHelper.convertLegacyFlatnameSeparators(setString);
        BooleanFormula setFormula = BooleanFunctionDeserialiser.parseFormula(processedSetString, internalReferenceResolver);
        instance.setSetFunction(setFormula);

        String resetString = element.getAttribute(FunctionSerialiser.RESET_FUNCTION_ATTRIBUTE_NAME);
        String processedResetString = NamespaceHelper.convertLegacyFlatnameSeparators(resetString);
        BooleanFormula resetFormula = BooleanFunctionDeserialiser.parseFormula(processedResetString, internalReferenceResolver);
        instance.setResetFunction(resetFormula);
    }

    @Override
    public void initInstance(Element element, FunctionContact instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {
    }

}
