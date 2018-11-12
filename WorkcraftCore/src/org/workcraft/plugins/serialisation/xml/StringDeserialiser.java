package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class StringDeserialiser implements BasicXMLDeserialiser<String> {

    @Override
    public String getClassName() {
        return String.class.getName();
    }

    @Override
    public String deserialise(Element element) {
        return element.getAttribute("value");
    }

}
