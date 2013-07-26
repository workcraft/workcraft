package org.workcraft.plugins.sdfs;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class SDFSModule implements Module {

	@Override
	public void init(Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(ModelDescriptor.class, SDFSModelDescriptor.class);
		p.registerClass(SettingsPage.class, SDFSVisualSettings.class);
	}

	@Override
	public String getDescription() {
		return "Static Data Flow Structures";
	}
}
