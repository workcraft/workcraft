package org.workcraft.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;

public interface NodeSerialiser {
    void serialise(Element parentElement, Object object) throws SerialisationException;
}
