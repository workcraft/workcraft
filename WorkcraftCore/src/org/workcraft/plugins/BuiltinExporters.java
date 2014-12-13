package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.interop.DotExporter;
import org.workcraft.plugins.interop.SVGExporter;

public class BuiltinExporters implements Module {
	@Override
	public void init() {
		final Framework framework = Framework.getInstance();
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(Exporter.class, DotExporter.class);
		pm.registerClass(Exporter.class, SVGExporter.class);
	}

	@Override
	public String getDescription() {
		return "Built-in exporters for Workcraft models";
	}

}
