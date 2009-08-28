package org.workcraft.plugins.serialisation;

import java.io.InputStream;
import java.util.UUID;

import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.DeserialisationResult;
import org.workcraft.framework.serialisation.ModelDeserialiser;
import org.workcraft.framework.serialisation.ReferenceResolver;

public class XMLDeserialiser implements ModelDeserialiser {
	public DeserialisationResult deserialise(InputStream inputStream,
			ReferenceResolver previousStepReferenceResolver)
			throws DeserialisationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public UUID getFormatUUID() {
		// TODO Auto-generated method stub
		return null;
	}

}
