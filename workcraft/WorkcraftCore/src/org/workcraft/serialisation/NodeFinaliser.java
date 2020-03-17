package org.workcraft.serialisation;

import org.workcraft.exceptions.DeserialisationException;

public interface NodeFinaliser {
    void finaliseInstance(Object instance) throws DeserialisationException;
}
