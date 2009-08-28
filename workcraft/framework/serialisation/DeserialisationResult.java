package org.workcraft.framework.serialisation;

import org.workcraft.dom.Model;

public interface DeserialisationResult {
	public Model getModel();
	public ReferenceResolver getReferenceResolver();
}
