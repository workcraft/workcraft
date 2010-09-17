package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.interop.DotExporter;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.interop.SVGExporter;
import org.workcraft.plugins.petrify.PSExporter;

public class Exporters implements Module {

	@Override
	public Class<?>[] getPluginClasses() {
		return new Class<?>[]{
				DotExporter.class,
				DotGExporter.class,
				SVGExporter.class,
				DotGImporter.class
		};
	}

	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();

		p.registerClass(Exporter.class, new Initialiser<Exporter>() {
			public Exporter create() {
				return new PSExporter(framework);
			}
		});

	}

}
