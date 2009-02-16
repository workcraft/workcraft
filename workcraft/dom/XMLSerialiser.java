package org.workcraft.dom;

import org.w3c.dom.Element;

public interface XMLSerialiser {
	public void serialise(Element element);
	public String getTagName();
}
