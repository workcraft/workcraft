package org.workcraft.plugins.policy.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.policy.Locality;
import org.workcraft.plugins.policy.VisualLocality;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;

public class VisualLocalityDeserialiser implements CustomXMLDeserialiser<VisualLocality> {

    @Override
    public String getClassName() {
        return VisualLocality.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, VisualLocality instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {

        String ref = element.getAttribute("ref");
        Locality locality = (Locality) externalReferenceResolver.getObject(ref);
        instance.setLocality(locality);
    }

    @Override
    public VisualLocality createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        String ref = element.getAttribute("ref");
        Locality locality = (Locality) externalReferenceResolver.getObject(ref);
        return new VisualLocality(locality);
    }

    @Override
    public void initInstance(Element element, VisualLocality instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {

    }

}
