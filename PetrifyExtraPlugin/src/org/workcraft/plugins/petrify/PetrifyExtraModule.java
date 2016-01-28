package org.workcraft.plugins.petrify;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.tools.ShowSg;

public class PetrifyExtraModule implements Module {

	@Override
	public void init() {
		final Framework framework = Framework.getInstance();
		PluginManager pm = framework.getPluginManager();
		pm.registerClass(Exporter.class, PSExporter.class);
		pm.registerClass(Settings.class, PetrifyExtraUtilitySettings.class);

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new ShowSg(false);
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new ShowSg(true);
			}
		});
	}

	@Override
	public String getDescription() {
		return "Petrify state graph support";
	}

}
