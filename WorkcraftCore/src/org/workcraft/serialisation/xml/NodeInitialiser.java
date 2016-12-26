package org.workcraft.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;

public interface NodeInitialiser {
    Object initInstance(Element element, Object ... constructorParameters) throws DeserialisationException;
}
