package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.layout.DotLayoutSettings;
import org.workcraft.plugins.layout.DotLayoutTool;
import org.workcraft.plugins.layout.NullLayoutTool;
import org.workcraft.plugins.layout.RandomLayoutSettings;
import org.workcraft.plugins.layout.RandomLayoutTool;
import org.workcraft.plugins.shared.CommonCommentSettings;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.transform.CopyLablesTool;

public class BuiltinTools implements Module {
	@Override
	public void init() {
		final Framework framework = Framework.getInstance();
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(Settings.class, CommonEditorSettings.class);
		pm.registerClass(Settings.class, CommonVisualSettings.class);
		pm.registerClass(Settings.class, CommonSimulationSettings.class);
		pm.registerClass(Settings.class, CommonCommentSettings.class);
		pm.registerClass(Settings.class, CommonDebugSettings.class);

		pm.registerClass(Settings.class, DotLayoutSettings.class);
		pm.registerClass(Settings.class, RandomLayoutSettings.class);

		pm.registerClass(Tool.class, DotLayoutTool.class);
		pm.registerClass(Tool.class, NullLayoutTool.class);
		pm.registerClass(Tool.class, RandomLayoutTool.class);

		pm.registerClass(Tool.class, CopyLablesTool.class);
	}

	@Override
	public String getDescription() {
		return "Built-in tools";
	}

}
