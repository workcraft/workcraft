package org.workcraft.serialisation;

import java.util.Set;

public interface References extends ReferenceProducer, ReferenceResolver {
    Set<Object> getObjects();
    Set<String> getReferences();
}
