package org.workcraft.dom.visual;

import org.workcraft.dom.ReferenceResolver;

public interface VisualReferenceResolver extends ReferenceResolver {
	public VisualComponent getComponentByRefID(int ID);
}
