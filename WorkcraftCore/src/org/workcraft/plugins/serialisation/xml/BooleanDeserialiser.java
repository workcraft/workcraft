package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class BooleanDeserialiser implements BasicXMLDeserialiser<Boolean> {

    @Override
    public String getClassName() {
        return boolean.class.getName();
    }

    @Override
    public Boolean deserialise(Element element) {
        return Boolean.parseBoolean(element.getAttribute("value"));
    }

}
