package org.workcraft.plugins.cpog.serialisation;

import java.util.Map;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.cpog.Encoding;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VariableState;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class VisualCPOGGroupSerialiser implements CustomXMLSerialiser {
    @Override
    public String getClassName() {
        return VisualScenario.class.getName();
    }

    @Override
    public void serialise(Element element, Object object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException {
        Encoding encoding = ((VisualScenario) object).getEncoding();

        Map<Variable, VariableState> states = encoding.getStates();

        for (Variable var : states.keySet()) {
            VariableState state = states.get(var);

            Element subelement = element.getOwnerDocument().createElement("encoding");
            subelement.setAttribute("variable", externalReferences.getReference(var));
            subelement.setAttribute("state", state.name());

            element.appendChild(subelement);
        }
    }
}
