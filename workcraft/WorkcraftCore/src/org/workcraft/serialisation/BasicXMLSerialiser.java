package org.workcraft.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;

public interface BasicXMLSerialiser<T> extends XMLSerialiser {
    void serialise(Element element, T object) throws SerialisationException;
}
