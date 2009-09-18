package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;

public class VisualPlaceDeserialiser implements CustomXMLDeserialiser {

	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {

	}

	public Object initInstance(Element element,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {
		return new VisualPlace ( ((Place) externalReferenceResolver.getObject(element.getAttribute("ref"))) );
	}


	public String getClassName() {
		return VisualPlace.class.getName();
	}

}
