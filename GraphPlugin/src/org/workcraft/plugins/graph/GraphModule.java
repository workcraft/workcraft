package org.workcraft.plugins.graph;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.tools.AbstractContractorTool;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphModule implements Module {

	@Override
	public void init() {
		final Framework framework = Framework.getInstance();
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new AbstractContractorTool() {
					@Override
					public boolean isApplicableTo(WorkspaceEntry we) {
						return we.getModelEntry().getMathModel() instanceof Graph;
					}
				};
			}
		});

		pm.registerClass(ModelDescriptor.class, GraphModelDescriptor.class);
	}

	@Override
	public String getDescription() {
		return "Directed Graph";
	}
}
