package org.workcraft.dom.visual;

import org.workcraft.dom.ReferenceResolver;

public interface VisualReferenceResolver extends ReferenceResolver {
	public VisualComponent getVisualComponentByID(int ID);
	public VisualConnection getVisualConnectionByID(int ID);
}
