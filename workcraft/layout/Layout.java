package org.workcraft.layout;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.LayoutFailedException;
import org.workcraft.framework.plugins.Plugin;

public interface Layout extends Plugin {
	public String getDisplayName();
	public boolean isApplicableTo (VisualModel model);
	public void doLayout (VisualModel model) throws LayoutFailedException;
}