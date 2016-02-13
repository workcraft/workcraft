package org.workcraft.plugins.xmas.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.xmas.components.VisualXmasConnection;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class CircuitConnectionDeserializer implements CustomXMLDeserialiser {

    @Override
    public Object createInstance(Element element,
            ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new VisualXmasConnection();
    }

    @Override
    public void finaliseInstance(Element element, Object instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            NodeFinaliser nodeFinaliser) throws DeserialisationException {

    /*    VisualCircuitConnection vc = (VisualCircuitConnection) instance;

        vc.setVisualConnectionDependencies(
                (VisualComponent) externalReferenceResolver.getObject(element.getAttribute("first")),
                (VisualComponent) externalReferenceResolver.getObject(element.getAttribute("second")),
                new Polyline(vc),
                (CircuitConnection) externalReferenceResolver.getObject(element.getAttribute("ref"))
                ); */

    }

    @Override
    public void initInstance(Element element, Object instance,
            ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {

    }

    @Override
    public String getClassName() {
        return VisualXmasConnection.class.getName();
    }

}
