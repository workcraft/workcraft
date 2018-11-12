package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

import java.awt.*;

public class ColorDeserialiser implements BasicXMLDeserialiser<Color> {

    @Override
    public String getClassName() {
        return Color.class.getName();
    }

    @Override
    public Color deserialise(Element element) {
        String s = element.getAttribute("rgb");
        if (s == null || s.charAt(0) != '#') {
            s = "#000000";
        }
        int rgb = Integer.parseInt(s.substring(1), 16);
        return new Color(rgb, false);

    }

}
