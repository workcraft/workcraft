package org.workcraft.serialisation;

import java.util.Set;

public interface ReferenceResolver {
    Object getObject(String reference);
    Set<Object> getObjects();
}
