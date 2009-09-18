package org.workcraft.plugins.stg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.ReferencingXMLSerialiser;

public class VisualSignalTransitionSerialiser implements ReferencingXMLSerialiser {
	public void serialise(Element element, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences) throws SerialisationException {
		element.setAttribute("ref", externalReferences.getReference(((VisualSignalTransition)object).getReferencedTransition()));
	}

	public String getClassName() {
		return VisualSignalTransition.class.getName();
	}

}
