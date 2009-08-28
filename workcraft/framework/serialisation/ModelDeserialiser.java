package org.workcraft.framework.serialisation;

import java.io.InputStream;

import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.plugins.Plugin;

public interface ModelDeserialiser extends Plugin, SerialFormat {
		public String getDescription();
		public DeserialisationResult deserialise (InputStream inputStream, ReferenceResolver externalRefrenceResolver) throws DeserialisationException;
}
