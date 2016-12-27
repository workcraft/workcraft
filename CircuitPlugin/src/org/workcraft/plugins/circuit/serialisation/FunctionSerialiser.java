package org.workcraft.plugins.circuit.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.formula.serialisation.BooleanFormulaSerialiser;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class FunctionSerialiser implements CustomXMLSerialiser {
    public static final String RESET_FUNCTION_ATTRIBUTE_NAME = "resetFunction";
    public static final String SET_FUNCTION_ATTRIBUTE_NAME = "setFunction";

    @Override
    public String getClassName() {
        return FunctionContact.class.getName();
    }

    @Override
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
            throws SerialisationException {
        FunctionContact function = (FunctionContact) object;

        BooleanFormulaSerialiser.writeFormulaAttribute(element, internalReferences,
                function.getSetFunction(), SET_FUNCTION_ATTRIBUTE_NAME);

        BooleanFormulaSerialiser.writeFormulaAttribute(element, internalReferences,
                function.getResetFunction(), RESET_FUNCTION_ATTRIBUTE_NAME);
    }

}
