package org.workcraft.plugins.serialisation.xml;

import java.awt.Color;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class ColorDeserialiser implements BasicXMLDeserialiser {

    public String getClassName() {
        return Color.class.getName();
    }

    public Object deserialise(Element element) throws DeserialisationException {
        String s = element.getAttribute("rgb");
        if (s == null || s.charAt(0) != '#') {
            s = "#000000";
        }
        int rgb = Integer.parseInt(s.substring(1), 16);
        return new Color(rgb, false);

    }

}
