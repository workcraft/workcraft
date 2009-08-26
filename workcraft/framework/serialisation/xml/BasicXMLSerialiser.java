package org.workcraft.framework.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.SerialisationException;

public interface BasicXMLSerialiser extends XMLSerialiser {
	public void serialise(Element element, Object object) throws SerialisationException;
}