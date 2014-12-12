package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.tools.CscResolutionTool;
import org.workcraft.plugins.mpsat.tools.MpsatConformationChecker;
import org.workcraft.plugins.mpsat.tools.MpsatConsistencyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatCscChecker;
import org.workcraft.plugins.mpsat.tools.MpsatCustomPropertyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatDeadlockChecker;
import org.workcraft.plugins.mpsat.tools.MpsatNormalcyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatPersistencyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesisComplexGate;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesisGeneralisedCelement;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesisStandardCelement;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesisTechnologyMapping;
import org.workcraft.plugins.mpsat.tools.MpsatUscChecker;
import org.workcraft.plugins.pcomp.PcompUtilitySettings;
import org.workcraft.plugins.pcomp.tools.PcompTool;

public class MpsatModule implements Module {

	@Override
	public void init() {
		final Framework framework = Framework.getInstance();
		PluginManager pm = framework.getPluginManager();

		pm.registerClass(Tool.class, CscResolutionTool.class);
		pm.registerClass(Tool.class, MpsatSynthesisComplexGate.class);
		pm.registerClass(Tool.class, MpsatSynthesisGeneralisedCelement.class);
		pm.registerClass(Tool.class, MpsatSynthesisStandardCelement.class);
		pm.registerClass(Tool.class, MpsatSynthesisTechnologyMapping.class);
		pm.registerClass(Tool.class, MpsatDeadlockChecker.class);
		pm.registerClass(Tool.class, MpsatConsistencyChecker.class);
		pm.registerClass(Tool.class, MpsatPersistencyChecker.class);
		pm.registerClass(Tool.class, MpsatNormalcyChecker.class);
		pm.registerClass(Tool.class, MpsatCscChecker.class);
		pm.registerClass(Tool.class, MpsatUscChecker.class);
		pm.registerClass(Tool.class, MpsatConformationChecker.class);
		pm.registerClass(Tool.class, MpsatCustomPropertyChecker.class);

		pm.registerClass(Settings.class, MpsatUtilitySettings.class);
		pm.registerClass(Settings.class, PunfUtilitySettings.class);

		pm.registerClass(Tool.class, PcompTool.class);
		pm.registerClass(Settings.class, PcompUtilitySettings.class);
	}

	@Override
	public String getDescription() {
		return "Punf, MPSat and PComp tool support";
	}
}
