package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.xml.AllowPropertySerialisation;
import org.workcraft.framework.serialisation.xml.XMLSerialiser;

@AllowPropertySerialisation
public class DoubleSerialiser implements XMLSerialiser{
	public String getClassName() {
		return double.class.getName();
	}

	public void serialise(Element element, Object object,
			ExternalReferenceResolver incomingReferences)
			throws ExportException {
		element.setAttribute("value", ((Double)object).toString());
	}
}
