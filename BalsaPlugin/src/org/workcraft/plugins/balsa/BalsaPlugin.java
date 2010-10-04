package org.workcraft.plugins.balsa;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.balsa.io.BreezeImporter;
import org.workcraft.plugins.balsa.io.DotExporter;

import tools.ExtractControlSTG;

public class BalsaPlugin implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager pluginManager = framework.getPluginManager();

		pluginManager.registerClass(ModelDescriptor.class, new Initialiser<ModelDescriptor>() {
			@Override
			public ModelDescriptor create() {
				return new ModelDescriptor() {
					@Override
					public String getDisplayName() {
						return "Breeze circuit";
					}

					@Override
					public MathModel createMathModel() {
						return new BalsaCircuit();
					}

					@Override
					public VisualModelDescriptor getVisualModelDescriptor() {
						return null;
					}
				};
			}
		});
		pluginManager.registerClass(Exporter.class, new Initialiser<Exporter>() {
			@Override
			public Exporter create() {
				return new DotExporter();
			}
		});
		pluginManager.registerClass(Importer.class, new Initialiser<Importer>() {
			@Override
			public Importer create() {
				return new BreezeImporter();
			}
		});
		pluginManager.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new ExtractControlSTG(framework);
			}

		});

	};
}
