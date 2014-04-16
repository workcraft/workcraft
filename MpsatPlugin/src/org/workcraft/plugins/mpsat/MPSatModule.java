package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.mpsat.tools.CscResolutionTool;
import org.workcraft.plugins.mpsat.tools.CustomPropertyMpsatChecker;
import org.workcraft.plugins.mpsat.tools.MpsatConsistencyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatCscChecker;
import org.workcraft.plugins.mpsat.tools.MpsatDeadlockChecker;
import org.workcraft.plugins.mpsat.tools.MpsatPersistencyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesis;
import org.workcraft.plugins.mpsat.tools.MpsatUscChecker;
import org.workcraft.plugins.pcomp.PcompUtilitySettings;
import org.workcraft.plugins.pcomp.tools.PcompTool;

public class MPSatModule implements Module {

	@Override
	public void init(Framework framework) {
		PluginManager p = framework.getPluginManager();

		p.registerClass(Tool.class, CscResolutionTool.class, framework);
		p.registerClass(Tool.class, MpsatSynthesis.class, framework);
		p.registerClass(Tool.class, MpsatDeadlockChecker.class, framework);
		p.registerClass(Tool.class, MpsatConsistencyChecker.class, framework);
		p.registerClass(Tool.class, MpsatPersistencyChecker.class, framework);
		p.registerClass(Tool.class, MpsatCscChecker.class, framework);
		p.registerClass(Tool.class, MpsatUscChecker.class, framework);
		p.registerClass(Tool.class, CustomPropertyMpsatChecker.class, framework);

		p.registerClass(SettingsPage.class, MpsatUtilitySettings.class);
		p.registerClass(SettingsPage.class, PunfUtilitySettings.class);

		p.registerClass(Tool.class, PcompTool.class, framework);
		p.registerClass(SettingsPage.class, PcompUtilitySettings.class);
	}

	@Override
	public String getDescription() {
		return "Punf, MPSat and PComp tool support";
	}
}
