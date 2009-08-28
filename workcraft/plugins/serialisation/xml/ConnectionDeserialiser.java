package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.framework.serialisation.xml.ReferencingXMLDeserialiser;

public class ConnectionDeserialiser implements ReferencingXMLDeserialiser {
	public void deserialise(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {
		Connection con = (Connection)instance;
		Component first = (Component)internalReferenceResolver.getObject(element.getAttribute("first"));
		Component second = (Component)internalReferenceResolver.getObject(element.getAttribute("second"));

		con.setComponents(first, second);
	}

	public String getClassName() {
		return Connection.class.getName();
	}
}
