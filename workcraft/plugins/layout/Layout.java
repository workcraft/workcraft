package org.workcraft.plugins.layout;

import org.workcraft.dom.visual.VisualAbstractGraphModel;
import org.workcraft.framework.exceptions.LayoutFailedException;

public interface Layout {
	public boolean isApplicableTo(Class<?> visualModelClass);
	public void doLayout (VisualAbstractGraphModel visualModel) throws LayoutFailedException;
}
