package org.workcraft.plugins.petri;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.petri.tools.TransitionContractorTool;

public class PetriNetModule implements Module {

	@Override
	public void init() {
		final Framework framework = Framework.getInstance();
		final PluginManager pm = framework.getPluginManager();
		pm.registerClass(ModelDescriptor.class, PetriNetModelDescriptor.class);

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new TransitionContractorTool();
			}
		});
	}

	@Override
	public String getDescription() {
		return "Petri Net";
	}

}
