package org.workcraft.serialisation.xml;

import org.workcraft.exceptions.DeserialisationException;

public interface NodeFinaliser {
    void finaliseInstance(Object instance) throws DeserialisationException;
}
