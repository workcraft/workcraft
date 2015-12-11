package org.workcraft.plugins.fst;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.cpog.tools.PnToCpogTool;
import org.workcraft.plugins.fst.interop.DotGExporter;
import org.workcraft.plugins.fst.interop.DotGImporter;
import org.workcraft.plugins.fst.serialisation.DotGSerialiser;
import org.workcraft.plugins.fst.tools.FsmToFstConverterTool;
import org.workcraft.plugins.fst.tools.FstToFsmConverterTool;
import org.workcraft.plugins.fst.tools.FstToStgConverterTool;
import org.workcraft.plugins.fst.tools.PnToFsmConverterTool;
import org.workcraft.plugins.fst.tools.StgToFstConverterTool;
import org.workcraft.serialisation.ModelSerialiser;

public class FstModule  implements Module {

	@Override
	public String getDescription() {
		return "Finite State Transducer";
	}

	@Override
	public void init() {
		initPluginManager();
		initCompatibilityManager();
	}

	private void initPluginManager() {
		final Framework framework = Framework.getInstance();
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(ModelDescriptor.class, FstDescriptor.class);

		pm.registerClass(Exporter.class, DotGExporter.class);
		pm.registerClass(Importer.class, DotGImporter.class);

		pm.registerClass(ModelSerialiser.class, DotGSerialiser.class);
		pm.registerClass(Settings.class, FstSettings.class);

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new StgToFstConverterTool(false);
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new StgToFstConverterTool(true);
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
				return new PnToFsmConverterTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new FsmToFstConverterTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new FstToFsmConverterTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new PnToCpogTool();
			}
		});
	}

	private void initCompatibilityManager() {
		final Framework framework = Framework.getInstance();
		final CompatibilityManager cm = framework.getCompatibilityManager();

		cm.registerMetaReplacement(
				"<descriptor class=\"org.workcraft.plugins.fst.FstModelDescriptor\"/>",
				"<descriptor class=\"org.workcraft.plugins.fst.FstDescriptor\"/>");
	}

}
