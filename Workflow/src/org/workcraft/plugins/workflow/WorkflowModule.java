package org.workcraft.plugins.workflow;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.dom.ModelDescriptor;

public class WorkflowModule implements Module {
	@Override
	public void init(Framework framework) {
		framework.getPluginManager().registerClass(ModelDescriptor.class, WorkflowModelDescriptor.class);
	}

	@Override
	public String getDescription() {
		return "Workflow";
	}
}
