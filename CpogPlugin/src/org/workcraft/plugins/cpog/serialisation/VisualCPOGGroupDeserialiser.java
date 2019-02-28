package org.workcraft.plugins.cpog.serialisation;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.plugins.cpog.Encoding;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VariableState;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;

public class VisualCPOGGroupDeserialiser implements CustomXMLDeserialiser<VisualScenario> {

    @Override
    public String getClassName() {
        return VisualScenario.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, VisualScenario instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {

        Encoding encoding = instance.getEncoding();
        NodeList subelements = element.getElementsByTagName("encoding");

        for (int i = 0; i < subelements.getLength(); i++) {
            Element subelement = (Element) subelements.item(i);

            Variable var = (Variable) externalReferenceResolver.getObject(subelement.getAttribute("variable"));
            VariableState state = Enum.valueOf(VariableState.class, subelement.getAttribute("state"));

            encoding.setState(var, state);
        }
    }

    @Override
    public VisualScenario createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new VisualScenario();
    }

    @Override
    public void initInstance(Element element, VisualScenario instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {

    }

}
