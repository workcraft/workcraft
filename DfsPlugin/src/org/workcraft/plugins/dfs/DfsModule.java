package org.workcraft.plugins.dfs;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.dfs.tools.CheckDataflowDeadlockTool;
import org.workcraft.plugins.dfs.tools.CheckDataflowHazardTool;
import org.workcraft.plugins.dfs.tools.CheckDataflowTool;
import org.workcraft.plugins.dfs.tools.StgGeneratorTool;

public class DfsModule implements Module {

	@Override
	public void init(final Framework framework) {
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new StgGeneratorTool(framework);
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckDataflowDeadlockTool(framework);
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckDataflowHazardTool(framework);
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckDataflowTool(framework);
			}
		});

		pm.registerClass(ModelDescriptor.class, DfsModelDescriptor.class);
		pm.registerClass(SettingsPage.class, DfsSettings.class);
	}

	@Override
	public String getDescription() {
		return "Dataflow Structure";
	}
}
