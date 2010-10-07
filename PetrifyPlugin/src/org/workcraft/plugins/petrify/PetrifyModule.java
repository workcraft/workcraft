package org.workcraft.plugins.petrify;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.tools.PetrifyComplexGateSynthesis;
import org.workcraft.plugins.petrify.tools.PetrifyDummyContraction;
import org.workcraft.plugins.petrify.tools.ShowSg;

public class PetrifyModule implements Module {

	@Override
	public void init(Framework framework) {
		PluginManager p = framework.getPluginManager();

		p.registerClass(Exporter.class, PSExporter.class, framework);

		p.registerClass(Tool.class, PetrifyComplexGateSynthesis.class, framework);
		p.registerClass(Tool.class, PetrifyDummyContraction.class, framework);
		p.registerClass(Tool.class, ShowSg.class, framework);
	}

	@Override
	public String getDescription() {
		return "Petrify tool support";
	}
}
