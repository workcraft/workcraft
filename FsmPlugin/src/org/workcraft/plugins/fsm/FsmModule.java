package org.workcraft.plugins.fsm;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.tools.AbstractContractorTool;
import org.workcraft.plugins.fsm.tools.PetriNetGeneratorTool;
import org.workcraft.workspace.WorkspaceEntry;

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

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new AbstractContractorTool(framework) {
					@Override
					public boolean isApplicableTo(WorkspaceEntry we) {
						return we.getModelEntry().getMathModel() instanceof Fsm;
					}
				};
			}
		});

		pm.registerClass(ModelDescriptor.class, FsmModelDescriptor.class);
	}

	@Override
	public String getDescription() {
		return "Finite State Machine";
	}

}
