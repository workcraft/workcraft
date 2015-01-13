package org.workcraft.plugins.fst;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.fst.interop.DotGExporter;
import org.workcraft.plugins.fst.interop.DotGImporter;
import org.workcraft.plugins.fst.serialisation.DotGSerialiser;
import org.workcraft.plugins.fst.tools.FstToFsmConverterTool;
import org.workcraft.plugins.fst.tools.FstToStgConverterTool;
import org.workcraft.plugins.fst.tools.StgToFstConverterTool;
import org.workcraft.serialisation.ModelSerialiser;

public class FstModule  implements Module {

	@Override
	public void init() {
		final Framework framework = Framework.getInstance();
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(ModelDescriptor.class, FstModelDescriptor.class);

		pm.registerClass(Exporter.class, DotGExporter.class);
		pm.registerClass(Importer.class, DotGImporter.class);

		pm.registerClass(ModelSerialiser.class, DotGSerialiser.class);
		pm.registerClass(Settings.class, FstSettings.class);

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new StgToFstConverterTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new FstToStgConverterTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new FstToFsmConverterTool();
			}
		});
	}

	@Override
	public String getDescription() {
		return "Finite State Transducer";
	}

}
