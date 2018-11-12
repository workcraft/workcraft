package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

import java.awt.*;

public class ColorSerialiser implements BasicXMLSerialiser<Color> {

    @Override
    public String getClassName() {
        return Color.class.getName();
    }

    @Override
    public void serialise(Element element, Color object) throws SerialisationException {
        element.setAttribute("rgb", String.format("#%x", object.getRGB() & 0xffffff));
    }

}
