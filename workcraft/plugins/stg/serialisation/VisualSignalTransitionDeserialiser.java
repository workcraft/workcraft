package org.workcraft.plugins.stg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.framework.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class VisualSignalTransitionDeserialiser implements
		CustomXMLDeserialiser {

	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {

	}

	public Object initInstance(Element element,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {
		return new VisualSignalTransition ( ((SignalTransition) externalReferenceResolver.getObject(element.getAttribute("ref"))) );
	}

	public String getClassName() {
		return VisualSignalTransition.class.getName();
	}

}
