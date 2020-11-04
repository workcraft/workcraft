package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.BasicXMLDeserialiser;
import org.workcraft.utils.ParseUtils;

import java.awt.*;

public class ColorDeserialiser implements BasicXMLDeserialiser<Color> {

    @Override
    public String getClassName() {
        return Color.class.getName();
    }

    @Override
    public Color deserialise(Element element) {
        String s = element.getAttribute("argb");
        if (s.isEmpty()) {
            s = element.getAttribute("rgb");
        }
        return ParseUtils.parseColor(s, Color.BLACK);
    }

}
