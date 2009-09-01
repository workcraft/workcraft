package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.framework.serialisation.xml.ReferencingXMLDeserialiser;

public class VisualConnectionDeserialiser implements ReferencingXMLDeserialiser {
	public String getClassName() {
		return VisualConnection.class.getName();
	}

	public void deserialise(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver)
	throws DeserialisationException {
		VisualConnection vcon = (VisualConnection)instance;

		vcon.setVisualConnection(
				(VisualComponent)internalReferenceResolver.getObject(element.getAttribute("first")),
				(VisualComponent)internalReferenceResolver.getObject(element.getAttribute("second")),
				(Connection)externalReferenceResolver.getObject(element.getAttribute("ref"))
		);
	}
}
