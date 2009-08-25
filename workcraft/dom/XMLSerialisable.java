package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;

public interface XMLSerialisable {
	public void serialise(Element element, ExternalReferenceResolver referenceResolver);
	public void deserialise(Element element, ReferenceResolver referenceResolver) throws ImportException;
}