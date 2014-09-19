package org.workcraft.plugins.fsm;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.fsm.tools.PetriNetGeneratorTool;

public class FsmModule  implements Module {

	@Override
	public void init(final Framework framework) {
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new PetriNetGeneratorTool(framework);
			}
		});

		pm.registerClass(ModelDescriptor.class, FsmModelDescriptor.class);
	}

	@Override
	public String getDescription() {
		return "Finite State Machine";
	}

}
