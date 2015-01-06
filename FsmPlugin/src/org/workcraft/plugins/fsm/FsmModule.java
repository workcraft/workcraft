package org.workcraft.plugins.fsm;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.tools.AbstractContractorTool;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.fsm.interop.DotGExporter;
import org.workcraft.plugins.fsm.interop.DotGImporter;
import org.workcraft.plugins.fsm.serialisation.DotGSerialiser;
import org.workcraft.plugins.fsm.serialisation.EventDeserialiser;
import org.workcraft.plugins.fsm.serialisation.EventSerialiser;
import org.workcraft.plugins.fsm.tools.DeadlockCheckerTool;
import org.workcraft.plugins.fsm.tools.DeterminismCheckerTool;
import org.workcraft.plugins.fsm.tools.FsmToPetriNetConverterTool;
import org.workcraft.plugins.fsm.tools.PetriNetToFsmConverterTool;
import org.workcraft.plugins.fsm.tools.ReachabilityCheckerTool;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;
import org.workcraft.workspace.WorkspaceEntry;

public class FsmModule  implements Module {

	@Override
	public void init() {
		final Framework framework = Framework.getInstance();
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(ModelDescriptor.class, FsmModelDescriptor.class);

		pm.registerClass(Exporter.class, DotGExporter.class);
		pm.registerClass(Importer.class, DotGImporter.class);

		pm.registerClass(ModelSerialiser.class, DotGSerialiser.class);

		pm.registerClass(XMLSerialiser.class, EventSerialiser.class);
		pm.registerClass(XMLDeserialiser.class, EventDeserialiser.class);

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new FsmToPetriNetConverterTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new DeadlockCheckerTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new DeterminismCheckerTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new ReachabilityCheckerTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new PetriNetToFsmConverterTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new AbstractContractorTool() {
					@Override
					public boolean isApplicableTo(WorkspaceEntry we) {
						return we.getModelEntry().getMathModel() instanceof Fsm;
					}
				};
			}
		});
	}

	@Override
	public String getDescription() {
		return "Finite State Machine";
	}

}
