package org.workcraft.plugins.xmas;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.xmas.tools.JsonExport;
import org.workcraft.plugins.xmas.tools.PNetGen;


public class XmasModule implements Module {

	@Override
	public String getDescription() {
		return "xMAS circuit model";
	}

	@Override
	public void init() {
		initPluginManager();
		initCompatibilityManager();
	}

	private void initPluginManager() {
		final Framework framework = Framework.getInstance();
		PluginManager pm = framework.getPluginManager();

		pm.registerClass(Tool.class, JsonExport.class);
		pm.registerClass(Tool.class, PNetGen.class);

		pm.registerClass(ModelDescriptor.class, XmasDescriptor.class);
		pm.registerClass(Settings.class, XmasSettings.class);
	}

	private void initCompatibilityManager() {
		final Framework framework = Framework.getInstance();
		final CompatibilityManager cm = framework.getCompatibilityManager();

		cm.registerMetaReplacement(
				"<descriptor class=\"org.workcraft.plugins.xmas.XmasModelDescriptor\"/>",
				"<descriptor class=\"org.workcraft.plugins.xmas.XmasDescriptor\"/>");
	}

}
