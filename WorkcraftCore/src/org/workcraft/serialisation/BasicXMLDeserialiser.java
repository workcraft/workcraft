package org.workcraft.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;

public interface BasicXMLDeserialiser<T> extends XMLDeserialiser {
    T deserialise(Element element) throws DeserialisationException;
}
