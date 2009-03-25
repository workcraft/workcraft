package org.workcraft.plugins.balsa;

import org.w3c.dom.Element;
import org.workcraft.dom.XMLSerialisation;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.framework.VisualNodeSerialiser;
import org.workcraft.util.XmlUtil;

public class VisualTransformableNodeSerialiser implements VisualNodeSerialiser {

	public void serialise(VisualNode node, Element element) {
		final VisualTransformableNode tn = (VisualTransformableNode)node;

		XMLSerialisation serialisation = new XMLSerialisation();
		serialisation.addSerialiser(
		new XMLSerialiser(){
			public String getTagName() {
				return VisualTransformableNode.class.getSimpleName();
			}
			public void serialise(Element element) {
				XmlUtil.writeDoubleAttr(element, "X", tn.getX());
				XmlUtil.writeDoubleAttr(element, "Y", tn.getY());
			}
		});
		serialisation.serialise(element);
	}

}
