package org.workcraft.plugins.policy.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class BundleDeserialiser implements CustomXMLDeserialiser {

    @Override
    public void finaliseInstance(Element element, Object instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            NodeFinaliser nodeFinaliser) throws DeserialisationException {

        Bundle b = (Bundle) instance;
        String s = element.getAttribute("transitions");
        for (String ref : s.split("\\s*,\\s*")) {
            b.add((BundledTransition) internalReferenceResolver.getObject(ref));
        }
    }

    @Override
    public Object createInstance(Element element,
            ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        return new Bundle();
    }

    @Override
    public void initInstance(Element element, Object instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {
    }

    @Override
    public String getClassName() {
        return Bundle.class.getName();
    }

}