package org.workcraft.plugins.serialisation.xml;

import java.awt.Color;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

public class ColorSerialiser implements BasicXMLSerialiser {

    public String getClassName() {
        return Color.class.getName();
    }

    public void serialise(Element element, Object object)
            throws SerialisationException {
        element.setAttribute("rgb", String.format("#%x", ((Color) object).getRGB() & 0xffffff));
    }

}
