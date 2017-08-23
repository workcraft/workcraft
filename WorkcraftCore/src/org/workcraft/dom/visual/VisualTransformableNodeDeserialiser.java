package org.workcraft.dom.visual;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtils;

public class VisualTransformableNodeDeserialiser {

    public static void initTransformableNode(Element element, VisualTransformableNode node) {
        Element vnodeElement = XmlUtils.getChildElement(VisualTransformableNode.class.getSimpleName(), element);
        node.setX(XmlUtils.readDoubleAttr(vnodeElement, "X", 0));
        node.setY(XmlUtils.readDoubleAttr(vnodeElement, "Y", 0));
    }

}
