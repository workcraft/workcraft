package org.workcraft.plugins.wtg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class GuardDeserialiser implements BasicXMLDeserialiser<Guard> {

    @Override
    public String getClassName() {
        return Guard.class.getName();
    }

    @Override
    public Guard deserialise(Element element) {
        String guardString = element.getAttribute("guard");
        return Guard.createFromString(guardString);
    }

}