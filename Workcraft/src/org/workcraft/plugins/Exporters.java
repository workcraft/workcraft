package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.interop.DotExporter;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.interop.SVGExporter;
import org.workcraft.plugins.petrify.PSExporter;

public class Exporters implements Module {

	@Override
	public Class<?>[] getPluginClasses() {
		return new Class<?>[]{
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

		p.registerClass(Exporter.class, DotExporter.class);
		p.registerClass(Exporter.class, DotGExporter.class);
		p.registerClass(Exporter.class, SVGExporter.class);
		p.registerClass(Importer.class, DotGImporter.class);

	}

}
