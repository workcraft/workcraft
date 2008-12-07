package org.workcraft.plugins.layout;

import org.workcraft.dom.Model;
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
		if (Model.class.isAssignableFrom(visualModelClass))
			return true;
		else
			return false;

	}
}