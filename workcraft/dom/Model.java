package org.workcraft.dom;

import org.workcraft.dom.visual.VisualModel;

public interface Model {
	public MathModel getMathModel();
	public VisualModel getVisualModel();

	public String getTitle();
	public String getDisplayName();
}
