package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.MathNode;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.xml.XMLSerialiser;
import org.workcraft.util.XmlUtil;

public class MathNodeSerialiser implements XMLSerialiser {
	public String getClassName() {
		return MathNode.class.getName();
	}

	public void serialise(Element element, Object object, ExternalReferenceResolver incomingReferences) {
		MathNode node = (MathNode)object;
		XmlUtil.writeIntAttr(element, "ID", node.getID());
	}
}