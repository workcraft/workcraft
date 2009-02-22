package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.LoadFromXMLException;

public interface XMLDeserialiser {
	public void deserialise(Element element) throws LoadFromXMLException;
	public String getTagName();
}
