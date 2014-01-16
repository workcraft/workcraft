package org.workcraft.plugins.xmas;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.xmas.tools.JsonExport;
import org.workcraft.plugins.xmas.tools.PNetGen;


public class XmasModule implements Module {

	@Override
	public String getDescription() {
		return "xMAS circuit model";
	}

	@Override
	public void init(final Framework framework) {
		PluginManager pm = framework.getPluginManager();

		framework.getPluginManager().registerClass(Tool.class, JsonExport.class);
		framework.getPluginManager().registerClass(Tool.class, PNetGen.class);

		pm.registerClass(ModelDescriptor.class, XmasModelDescriptor.class);
		pm.registerClass(SettingsPage.class, XmasSettings.class);
	}

}
