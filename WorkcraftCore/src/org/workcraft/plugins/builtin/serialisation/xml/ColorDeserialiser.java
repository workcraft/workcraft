package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.BasicXMLDeserialiser;

import java.awt.*;

public class ColorDeserialiser implements BasicXMLDeserialiser<Color> {

    @Override
    public String getClassName() {
        return Color.class.getName();
    }

    @Override
    public Color deserialise(Element element) {
        String s = element.getAttribute("argb");
        boolean hasAlpha = true;
        if (!s.startsWith("#")) {
            hasAlpha = false;
            s = element.getAttribute("rgb");
            if (!s.startsWith("#")) {
                s = "#000000";
            }
        }
        int value = Integer.parseUnsignedInt(s.substring(1), 16);
        return new Color(value, hasAlpha);
    }

}
