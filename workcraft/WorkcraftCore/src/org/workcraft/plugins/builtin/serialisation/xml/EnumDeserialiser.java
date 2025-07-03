package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.BasicXMLDeserialiser;

public class EnumDeserialiser implements BasicXMLDeserialiser<Enum<?>> {

    @Override
    public String getClassName() {
        return Enum.class.getName();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Enum<?> deserialise(Element element) throws DeserialisationException {
        try {
            Class<? extends Enum> cls = Class.forName(element.getAttribute("enum-class")).asSubclass(Enum.class);
            return Enum.valueOf(cls, element.getAttribute("value"));
        } catch (ClassNotFoundException e) {
            throw new DeserialisationException(e);
        }
    }

}
