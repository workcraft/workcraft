package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

public class StringSerialiser implements BasicXMLSerialiser {
    public String getClassName() {
        return String.class.getName();
    }

    public void serialise(Element element, Object object)
            throws SerialisationException {
        element.setAttribute("value", (String) object);
    }
}
