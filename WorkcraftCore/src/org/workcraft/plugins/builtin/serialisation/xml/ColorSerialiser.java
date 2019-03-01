package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.BasicXMLSerialiser;

import java.awt.*;

public class ColorSerialiser implements BasicXMLSerialiser<Color> {

    @Override
    public String getClassName() {
        return Color.class.getName();
    }

    @Override
    public void serialise(Element element, Color object) {
        int value = object.getRGB();
        if (object.getAlpha() == 0xff) {
            element.setAttribute("rgb", String.format("#%06x",  value & 0xffffff));
        } else {
            element.setAttribute("argb", String.format("#%08x", value));
        }
    }

}
