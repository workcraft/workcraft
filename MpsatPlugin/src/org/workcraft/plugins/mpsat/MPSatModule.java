package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.mpsat.tools.CscResolutionTool;
import org.workcraft.plugins.mpsat.tools.CustomPropertyMpsatChecker;
import org.workcraft.plugins.mpsat.tools.MpsatDeadlockChecker;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesis;
import org.workcraft.plugins.pcomp.PcompUtilitySettings;
import org.workcraft.plugins.pcomp.tools.PcompTool;

public class MPSatModule implements Module {

	@Override
	public void init(Framework framework) {
		framework.getPluginManager().registerClass(Tool.class, CscResolutionTool.class, framework);
		framework.getPluginManager().registerClass(Tool.class, MpsatSynthesis.class, framework);
		framework.getPluginManager().registerClass(Tool.class, MpsatDeadlockChecker.class, framework);
		framework.getPluginManager().registerClass(Tool.class, CustomPropertyMpsatChecker.class, framework);

		framework.getPluginManager().registerClass(SettingsPage.class, MpsatUtilitySettings.class);
		framework.getPluginManager().registerClass(SettingsPage.class, PunfUtilitySettings.class);

		framework.getPluginManager().registerClass(Tool.class, PcompTool.class, framework);
		framework.getPluginManager().registerClass(SettingsPage.class, PcompUtilitySettings.class);
	}

	@Override
	public String getDescription() {
		return "Punf, MPSat and PComp tool support";
	}
}
