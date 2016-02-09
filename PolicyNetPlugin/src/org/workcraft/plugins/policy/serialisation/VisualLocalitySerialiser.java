package org.workcraft.plugins.policy.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.policy.Locality;
import org.workcraft.plugins.policy.VisualLocality;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class VisualLocalitySerialiser implements CustomXMLSerialiser{

    @Override
    public String getClassName()
    {
        return VisualLocality.class.getName();
    }

    @Override
    public void serialise(Element element, Object object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException
    {
        VisualLocality visualLocality = (VisualLocality)object;
        Locality locality = visualLocality.getLocality();
        String ref = externalReferences.getReference(locality);
        element.setAttribute("ref", ref);
    }

}