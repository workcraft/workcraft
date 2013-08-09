package org.workcraft.plugins.petri;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.dom.ModelDescriptor;

public class PetriNetModule implements Module {

	@Override
	public void init(Framework framework) {
		framework.getPluginManager().registerClass(ModelDescriptor.class, PetriNetModelDescriptor.class);
	}

	@Override
	public String getDescription() {
		return "Petri nets";
	}

}
