package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.BasicXMLSerialiser;
import org.workcraft.utils.ColorUtils;

import java.awt.*;

public class ColorSerialiser implements BasicXMLSerialiser<Color> {

    @Override
    public String getClassName() {
        return Color.class.getName();
    }

    @Override
    public void serialise(Element element, Color color) {
        if (color.getAlpha() == 0xff) {
            element.setAttribute("rgb", ColorUtils.getHexRGB(color));
        } else {
            element.setAttribute("argb", ColorUtils.getHexARGB(color));
        }
    }

}
