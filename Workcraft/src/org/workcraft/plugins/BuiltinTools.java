package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.layout.DotLayout;
import org.workcraft.plugins.layout.DotLayoutSettings;
import org.workcraft.plugins.layout.NullLayout;
import org.workcraft.plugins.layout.RandomLayout;
import org.workcraft.plugins.layout.RandomLayoutSettings;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class BuiltinTools implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(Tool.class, DotLayout.class, framework);
		p.registerClass(Tool.class, NullLayout.class);
		p.registerClass(Tool.class, RandomLayout.class);

		p.registerClass(SettingsPage.class, DotLayoutSettings.class);
		p.registerClass(SettingsPage.class, RandomLayoutSettings.class);
		p.registerClass(SettingsPage.class, CommonVisualSettings.class);
	}

	@Override
	public String getDescription() {
		return "Built-in tools";
	}
}
