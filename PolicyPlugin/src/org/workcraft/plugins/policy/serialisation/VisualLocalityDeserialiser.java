package org.workcraft.plugins.policy.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.policy.Locality;
import org.workcraft.plugins.policy.VisualLocality;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class VisualLocalityDeserialiser implements CustomXMLDeserialiser {

    @Override
    public String getClassName() {
        return VisualLocality.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, Object instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException {
        VisualLocality visualLocality = (VisualLocality) instance;
        String ref = element.getAttribute("ref");
        Locality locality = (Locality) externalReferenceResolver.getObject(ref);
        visualLocality.setLocality(locality);
    }

    @Override
    public Object createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        String ref = element.getAttribute("ref");
        Locality locality = (Locality) externalReferenceResolver.getObject(ref);
        return new VisualLocality(locality);
    }

    @Override
    public void initInstance(Element element, Object instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {
    }
}
