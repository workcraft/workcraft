package org.workcraft.plugins.wtg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class GuardDeserialiser implements BasicXMLDeserialiser {

    public String getClassName() {
        return Guard.class.getName();
    }

    public Object deserialise(Element element) throws DeserialisationException {
        String guardString = element.getAttribute("guard");
        return Guard.createFromString(guardString);
    }

}