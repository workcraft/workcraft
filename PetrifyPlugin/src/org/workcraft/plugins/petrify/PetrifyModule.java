package org.workcraft.plugins.petrify;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.tools.PetrifyCscConflictResolution;
import org.workcraft.plugins.petrify.tools.PetrifySynthesisComplexGate;
import org.workcraft.plugins.petrify.tools.PetrifyDummyContraction;
import org.workcraft.plugins.petrify.tools.PetrifySynthesisGeneralisedCelement;
import org.workcraft.plugins.petrify.tools.PetrifySynthesisTechnologyMapping;
import org.workcraft.plugins.petrify.tools.PetrifyUntoggle;
import org.workcraft.plugins.petrify.tools.ShowSg;
import org.workcraft.plugins.shared.PetrifyUtilitySettings;

public class PetrifyModule implements Module {

	@Override
	public void init(Framework framework) {
		PluginManager p = framework.getPluginManager();

		p.registerClass(Exporter.class, PSExporter.class, framework);
		p.registerClass(Settings.class, PetrifyUtilitySettings.class);

		p.registerClass(Tool.class, PetrifyUntoggle.class, framework);
		p.registerClass(Tool.class, PetrifyCscConflictResolution.class, framework);
		p.registerClass(Tool.class, PetrifySynthesisComplexGate.class, framework);
		p.registerClass(Tool.class, PetrifySynthesisGeneralisedCelement.class, framework);
		p.registerClass(Tool.class, PetrifySynthesisTechnologyMapping.class, framework);
		p.registerClass(Tool.class, PetrifyDummyContraction.class, framework);
		p.registerClass(Tool.class, ShowSg.class, framework);
	}

	@Override
	public String getDescription() {
		return "Petrify tool support";
	}
}
