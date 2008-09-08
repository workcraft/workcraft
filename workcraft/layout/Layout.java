package org.workcraft.layout;

import org.workcraft.dom.visual.VisualAbstractGraphModel;
import org.workcraft.framework.exceptions.LayoutFailedException;
import org.workcraft.framework.plugins.Plugin;

public interface Layout extends Plugin {
	public boolean isApplicableTo(Class<?> visualModelClass);
	public void doLayout (VisualAbstractGraphModel visualModel) throws LayoutFailedException;
}
