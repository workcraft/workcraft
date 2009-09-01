package org.workcraft.plugins.stg;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.framework.serialisation.xml.CustomXMLDeserialiser;

public class VisualSTGDeserialiser implements CustomXMLDeserialiser {

	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {

	}

	public Object initInstance(Element element,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {
		try {
			return new VisualSTG((STG)externalReferenceResolver.getObject("$model"));
		} catch (VisualModelInstantiationException e) {
			throw new DeserialisationException(e);
		}
	}

	public String getClassName() {
		return VisualSTG.class.getName();
	}
}
