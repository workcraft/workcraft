package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Plugin;
import org.workcraft.PluginManager;
import org.workcraft.plugins.interop.DotExporter;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.SVGExporter;
import org.workcraft.plugins.petrify.PSExporter;

public class Exporters implements Plugin {

	@Override
	public Class<?>[] getPluginClasses() {
		return new Class<?>[]{
				DotExporter.class,
				DotGExporter.class,
				SVGExporter.class,
		};
	}

	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(PSExporter.class, new Initialiser(){public Object create(){return new PSExporter(framework);}});
	}

}
