package org.workcraft.serialisation;

import java.util.Set;

public interface References extends ReferenceProducer, ReferenceResolver {
	public Set<Object> getObjects();
	public Set<String> getReferences();
}
