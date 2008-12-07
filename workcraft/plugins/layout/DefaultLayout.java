package org.workcraft.plugins.layout;

import org.workcraft.dom.MathModel;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.layout.Layout;


@DisplayName("Default")
public class DefaultLayout implements Layout {
	@Override
	public void doLayout(VisualModel model) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isApplicableTo(Class<?> visualModelClass) {
		if (MathModel.class.isAssignableFrom(visualModelClass))
			return true;
		else
			return false;

	}
}