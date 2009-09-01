package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.framework.serialisation.xml.ReferencingXMLSerialiser;

public class VisualConnectionSerialiser implements ReferencingXMLSerialiser {

	public void serialise(Element element, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences) throws SerialisationException {

		VisualConnection vcon = (VisualConnection) object;

		element.setAttribute("first", internalReferences.getReference(vcon.getFirst()));
		element.setAttribute("second", internalReferences.getReference(vcon.getSecond()));
		element.setAttribute("ref", externalReferences.getReference(vcon.getReferencedConnection()));
	}

	public String getClassName() {
		return VisualConnection.class.getName();
	}
}
