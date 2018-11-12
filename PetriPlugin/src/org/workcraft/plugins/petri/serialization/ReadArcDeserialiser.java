package org.workcraft.plugins.petri.serialization;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class ReadArcDeserialiser implements CustomXMLDeserialiser<VisualReadArc> {

    @Override
    public String getClassName() {
        return VisualReadArc.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, VisualReadArc instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {

        instance.setDependencies(
                (MathConnection) externalReferenceResolver.getObject(element.getAttribute("refCon1")),
                (MathConnection) externalReferenceResolver.getObject(element.getAttribute("refCon2"))
        );
    }

    @Override
    public VisualReadArc createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new VisualReadArc();
    }

    @Override
    public void initInstance(Element element, VisualReadArc instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {

    }

}
