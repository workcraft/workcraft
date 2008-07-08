package org.workcraft.framework.util;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DocumentFormatException;


public interface XmlSerializable {
	public void fromXmlDom(Element element) throws DocumentFormatException;
	public Element toXmlDom(Element parent_element);
}