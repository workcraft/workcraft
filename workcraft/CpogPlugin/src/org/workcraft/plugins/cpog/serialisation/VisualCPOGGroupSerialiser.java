package org.workcraft.plugins.cpog.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.cpog.Encoding;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VariableState;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.utils.XmlUtils;

import java.util.Map;

public class VisualCPOGGroupSerialiser implements CustomXMLSerialiser<VisualScenario> {

    @Override
    public String getClassName() {
        return VisualScenario.class.getName();
    }

    @Override
    public void serialise(Element element, VisualScenario object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) {

        Encoding encoding = object.getEncoding();
        Map<Variable, VariableState> states = encoding.getStates();

        for (Variable var : states.keySet()) {
            VariableState state = states.get(var);

            Element subelement = XmlUtils.createChildElement("encoding", element);
            subelement.setAttribute("variable", externalReferences.getReference(var));
            subelement.setAttribute("state", state.name());
        }
    }

}
