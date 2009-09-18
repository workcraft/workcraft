package org.workcraft.serialisation;

import java.io.InputStream;

import org.workcraft.Plugin;
import org.workcraft.exceptions.DeserialisationException;

public interface ModelDeserialiser extends Plugin, SerialFormat {
		public String getDescription();
		public DeserialisationResult deserialise (InputStream inputStream, ReferenceResolver externalRefrenceResolver) throws DeserialisationException;
}
