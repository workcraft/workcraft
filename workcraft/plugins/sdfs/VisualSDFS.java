package org.workcraft.plugins.sdfs;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.VisualComponentCreationException;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;

public class VisualSDFS extends VisualModel {

	public VisualSDFS(SDFS model)
	throws VisualModelInstantiationException {
		super(model);
		try {
			createDefaultFlatStructure();
		} catch (VisualComponentCreationException e) {
			throw new VisualModelInstantiationException(e);
		} catch (VisualConnectionCreationException e) {
			throw new VisualModelInstantiationException(e);
		}
	}
}