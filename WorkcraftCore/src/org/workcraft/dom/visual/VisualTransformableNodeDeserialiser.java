package org.workcraft.dom.visual;

import org.w3c.dom.Element;
import org.workcraft.utils.XmlUtils;

public class VisualTransformableNodeDeserialiser {

    public static void initTransformableNode(Element element, VisualTransformableNode node) {
        Element vnodeElement = XmlUtils.getChildElement(VisualTransformableNode.class.getSimpleName(), element);
        node.setX(readCoord(vnodeElement, "X", 0.0));
        node.setY(readCoord(vnodeElement, "Y", 0.0));
    }

    private static double readCoord(Element element, String name, double defaultValue) {
        String value = element.getAttribute(name);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
