package org.workcraft.plugins.graph;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.dom.ModelDescriptor;

public class GraphModule implements Module {

	@Override
	public void init(Framework framework) {
		framework.getPluginManager().registerClass(ModelDescriptor.class, GraphModelDescriptor.class);
	}

	@Override
	public String getDescription() {
		return "Graph";
	}
}
