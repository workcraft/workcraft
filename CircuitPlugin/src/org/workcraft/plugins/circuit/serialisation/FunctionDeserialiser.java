package org.workcraft.plugins.circuit.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.serialisation.BooleanFunctionDeserialiser;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class FunctionDeserialiser implements CustomXMLDeserialiser {
    @Override
    public String getClassName() {
        return FunctionContact.class.getName();
    }

    @Override
    public Object createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        return new FunctionContact();
    }

    @Override
    public void finaliseInstance(Element element, Object instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            NodeFinaliser nodeFinaliser) throws DeserialisationException {
        FunctionContact function = (FunctionContact) instance;
        function.setSetFunction(
                BooleanFunctionDeserialiser.readFormulaFromAttribute(element, internalReferenceResolver, FunctionSerialiser.SET_FUNCTION_ATTRIBUTE_NAME));
        function.setResetFunction(
                BooleanFunctionDeserialiser.readFormulaFromAttribute(element, internalReferenceResolver, FunctionSerialiser.RESET_FUNCTION_ATTRIBUTE_NAME));
    }

    @Override
    public void initInstance(Element element, Object instance,
            ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {
    }
}
