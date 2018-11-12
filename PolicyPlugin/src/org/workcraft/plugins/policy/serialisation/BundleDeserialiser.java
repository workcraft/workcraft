package org.workcraft.plugins.policy.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class BundleDeserialiser implements CustomXMLDeserialiser<Bundle> {

    @Override
    public String getClassName() {
        return Bundle.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, Bundle instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {

        String s = element.getAttribute("transitions");
        for (String ref : s.split("\\s*,\\s*")) {
            instance.add((BundledTransition) internalReferenceResolver.getObject(ref));
        }
    }

    @Override
    public Bundle createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new Bundle();
    }

    @Override
    public void initInstance(Element element, Bundle instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {
    }

}