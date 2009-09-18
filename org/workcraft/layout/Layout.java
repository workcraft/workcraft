package org.workcraft.layout;

import org.workcraft.Plugin;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.LayoutFailedException;

public interface Layout extends Plugin {
	public boolean isApplicableTo (VisualModel model);
	public void doLayout (VisualModel model) throws LayoutFailedException;
}