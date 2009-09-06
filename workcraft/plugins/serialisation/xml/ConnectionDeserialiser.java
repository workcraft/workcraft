package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.framework.serialisation.xml.ReferencingXMLDeserialiser;

public class ConnectionDeserialiser implements ReferencingXMLDeserialiser {
	public void deserialise(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {
		MathConnection con = (MathConnection)instance;
		MathNode first = (MathNode)internalReferenceResolver.getObject(element.getAttribute("first"));
		MathNode second = (MathNode)internalReferenceResolver.getObject(element.getAttribute("second"));

		con.setComponents(first, second);
	}

	public String getClassName() {
		return MathConnection.class.getName();
	}
}
