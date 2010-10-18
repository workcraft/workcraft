package org.workcraft.plugins.desij;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.Tool;
import org.workcraft.plugins.desij.tools.Decomposition;
import org.workcraft.plugins.desij.tools.DesiJCustomFunction;
import org.workcraft.plugins.desij.tools.DesiJDummyContraction;

public class DesiJModule implements Module {

	@Override
	public void init(Framework framework) {
		framework.getPluginManager().registerClass(Tool.class, Decomposition.class, framework);
		framework.getPluginManager().registerClass(Tool.class, DesiJCustomFunction.class, framework);
		framework.getPluginManager().registerClass(Tool.class, DesiJDummyContraction.class, framework);
	}

	@Override
	public String getDescription() {
		return "DesiJ tool support";
	}
}
