package org.workcraft.dom;

import org.w3c.dom.Element;

public interface XMLSerialisable {
	public void serialise(Element element);
	public String getTagName();
}
