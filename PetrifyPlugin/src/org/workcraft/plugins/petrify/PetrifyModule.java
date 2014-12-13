package org.workcraft.plugins.petrify;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.tools.PetrifyCscConflictResolution;
import org.workcraft.plugins.petrify.tools.PetrifyDummyContraction;
import org.workcraft.plugins.petrify.tools.PetrifySynthesisComplexGate;
import org.workcraft.plugins.petrify.tools.PetrifySynthesisGeneralisedCelement;
import org.workcraft.plugins.petrify.tools.PetrifySynthesisTechnologyMapping;
import org.workcraft.plugins.petrify.tools.PetrifyUntoggle;
import org.workcraft.plugins.petrify.tools.ShowSg;
import org.workcraft.plugins.shared.PetrifyUtilitySettings;

public class PetrifyModule implements Module {

	@Override
	public void init() {
		final Framework framework = Framework.getInstance();
		PluginManager pm = framework.getPluginManager();

		pm.registerClass(Exporter.class, PSExporter.class);
		pm.registerClass(Settings.class, PetrifyUtilitySettings.class);

		pm.registerClass(Tool.class, PetrifyUntoggle.class);
		pm.registerClass(Tool.class, PetrifyCscConflictResolution.class);
		pm.registerClass(Tool.class, PetrifySynthesisComplexGate.class);
		pm.registerClass(Tool.class, PetrifySynthesisGeneralisedCelement.class);
		pm.registerClass(Tool.class, PetrifySynthesisTechnologyMapping.class);
		pm.registerClass(Tool.class, PetrifyDummyContraction.class);
		pm.registerClass(Tool.class, ShowSg.class);
	}

	@Override
	public String getDescription() {
		return "Petrify tool support";
	}
}
