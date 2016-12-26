package org.workcraft.dom.visual;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public class VisualTransformableNodeDeserialiser {

    public static void initTransformableNode(Element element, VisualTransformableNode node) {
        Element vnodeElement = XmlUtil.getChildElement(VisualTransformableNode.class.getSimpleName(), element);
        node.setX(XmlUtil.readDoubleAttr(vnodeElement, "X", 0));
        node.setY(XmlUtil.readDoubleAttr(vnodeElement, "Y", 0));
    }

}
