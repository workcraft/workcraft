package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

public class StringSerialiser implements BasicXMLSerialiser<String> {

    @Override
    public String getClassName() {
        return String.class.getName();
    }

    @Override
    public void serialise(Element element, String object) throws SerialisationException {
        element.setAttribute("value", object);
    }

}
