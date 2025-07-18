package org.workcraft.serialisation;

import java.io.InputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;

public interface ModelDeserialiser extends SerialFormat {
    DeserialisationResult deserialise(InputStream is, ReferenceResolver rr, Model<?, ?> underlyingModel) throws DeserialisationException;
}
