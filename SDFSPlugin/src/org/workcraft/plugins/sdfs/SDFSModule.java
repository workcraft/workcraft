package org.workcraft.plugins.sdfs;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.sdfs.tools.CheckDataflowDeadlockTool;
import org.workcraft.plugins.sdfs.tools.CheckDataflowHazardTool;
import org.workcraft.plugins.sdfs.tools.CheckDataflowTool;
import org.workcraft.plugins.sdfs.tools.STGGeneratorTool;

public class SDFSModule implements Module {

	@Override
	public void init(final Framework framework) {
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new STGGeneratorTool(framework.getWorkspace());
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckDataflowDeadlockTool(framework, framework.getWorkspace());
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckDataflowHazardTool(framework, framework.getWorkspace());
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckDataflowTool(framework, framework.getWorkspace());
			}
		});

		pm.registerClass(ModelDescriptor.class, SDFSModelDescriptor.class);
		pm.registerClass(SettingsPage.class, SDFSVisualSettings.class);
	}

	@Override
	public String getDescription() {
		return "Static Data Flow Structures";
	}
}
