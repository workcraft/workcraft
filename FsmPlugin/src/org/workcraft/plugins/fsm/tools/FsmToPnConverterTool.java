package org.workcraft.plugins.fsm.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetModelDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class FsmToPnConverterTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Petri Net";
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		MathModel mathModel = we.getModelEntry().getMathModel();
		return (mathModel.getClass().equals(Fsm.class));
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualFsm fsm = (VisualFsm)we.getModelEntry().getVisualModel();
		final VisualPetriNet pn = new VisualPetriNet(new PetriNet());
		final FsmToPnConverter converter = new FsmToPnConverter(fsm, pn);
		final Framework framework = Framework.getInstance();
		final Workspace workspace = framework.getWorkspace();
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String desiredName = we.getWorkspacePath().getNode();
		final ModelEntry me = new ModelEntry(new PetriNetModelDescriptor(), converter.getDstModel());
		workspace.add(directory, desiredName, me, false, true);
	}
}
