package org.workcraft.plugins.petri;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.petri.tools.TestTool;

public class PetriNetModule implements Module {

	@Override
	public void init(Framework framework) {
		framework.getPluginManager().registerClass(ModelDescriptor.class, PetriNetModelDescriptor.class);
		framework.getPluginManager().registerClass(Tool.class, TestTool.class);
	}

	@Override
	public String getDescription() {
		return "Petri Net";
	}

}
