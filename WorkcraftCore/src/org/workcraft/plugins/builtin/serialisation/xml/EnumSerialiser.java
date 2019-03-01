package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.BasicXMLSerialiser;

public class EnumSerialiser implements BasicXMLSerialiser<Enum> {

    @Override
    public String getClassName() {
        return Enum.class.getName();
    }

    @Override
    public void serialise(Element element, Enum object) throws SerialisationException {
        element.setAttribute("enum-class", object.getClass().getName());
        element.setAttribute("value", object.name());

    }

}
