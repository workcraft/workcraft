package org.workcraft.dom;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualModelListener;

public interface Model {
	public MathModel getMathModel();
	public VisualModel getVisualModel();

	public String getTitle();
	public String getDisplayName();
}
