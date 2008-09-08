package org.workcraft.plugins.layout;

import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.visual.VisualAbstractGraphModel;
import org.workcraft.layout.Layout;


@DisplayName("Default")
public class DefaultLayout implements Layout {
	@Override
	public void doLayout(VisualAbstractGraphModel model) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isApplicableTo(Class<?> visualModelClass) {
		if (AbstractGraphModel.class.isAssignableFrom(visualModelClass))
			return true;
		else
			return false;

	}
}