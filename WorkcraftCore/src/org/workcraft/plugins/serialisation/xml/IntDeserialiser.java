package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class IntDeserialiser implements BasicXMLDeserialiser<Integer> {

    @Override
    public String getClassName() {
        return int.class.getName();
    }

    @Override
    public Integer deserialise(Element element) {
        return Integer.parseInt(element.getAttribute("value"));
    }

}
