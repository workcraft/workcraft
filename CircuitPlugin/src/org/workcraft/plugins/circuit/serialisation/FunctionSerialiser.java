package org.workcraft.plugins.circuit.serialisation;

import org.w3c.dom.Element;
import org.workcraft.formula.serialisation.BooleanFormulaSerialiser;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class FunctionSerialiser implements CustomXMLSerialiser<FunctionContact> {

    public static final String RESET_FUNCTION_ATTRIBUTE_NAME = "resetFunction";
    public static final String SET_FUNCTION_ATTRIBUTE_NAME = "setFunction";

    @Override
    public String getClassName() {
        return FunctionContact.class.getName();
    }

    @Override
    public void serialise(Element element, FunctionContact object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) {

        BooleanFormulaSerialiser.writeFormulaAttribute(element, internalReferences,
                object.getSetFunction(), SET_FUNCTION_ATTRIBUTE_NAME);

        BooleanFormulaSerialiser.writeFormulaAttribute(element, internalReferences,
                object.getResetFunction(), RESET_FUNCTION_ATTRIBUTE_NAME);
    }

}
