package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.layout.DotLayoutSettings;
import org.workcraft.plugins.layout.DotLayoutTool;
import org.workcraft.plugins.layout.NullLayoutTool;
import org.workcraft.plugins.layout.RandomLayoutSettings;
import org.workcraft.plugins.layout.RandomLayoutTool;
import org.workcraft.plugins.shared.CommonCommentSettings;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.transform.CopyLablesTool;

public class BuiltinTools implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();

		p.registerClass(SettingsPage.class, CommonEditorSettings.class);
		p.registerClass(SettingsPage.class, CommonVisualSettings.class);
		p.registerClass(SettingsPage.class, CommonCommentSettings.class);

		p.registerClass(SettingsPage.class, DotLayoutSettings.class);
		p.registerClass(SettingsPage.class, RandomLayoutSettings.class);

		p.registerClass(Tool.class, DotLayoutTool.class, framework);
		p.registerClass(Tool.class, NullLayoutTool.class);
		p.registerClass(Tool.class, RandomLayoutTool.class);

		p.registerClass(Tool.class, CopyLablesTool.class);
	}

	@Override
	public String getDescription() {
		return "Built-in tools";
	}

}
