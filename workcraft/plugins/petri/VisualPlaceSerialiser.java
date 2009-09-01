package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.framework.serialisation.xml.ReferencingXMLSerialiser;

public class VisualPlaceSerialiser implements ReferencingXMLSerialiser {

	public void serialise(Element element, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences) throws SerialisationException {
		element.setAttribute("ref", externalReferences.getReference(((VisualPlace)object).getReferencedPlace()));
	}

	public String getClassName() {
		return VisualPlace.class.getName();
	}

}
