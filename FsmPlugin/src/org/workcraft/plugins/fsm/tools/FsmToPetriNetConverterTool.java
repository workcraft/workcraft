package org.workcraft.plugins.fsm.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.petri.PetriNetModelDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class FsmToPetriNetConverterTool implements Tool {

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
		return we.getModelEntry().getMathModel() instanceof Fsm;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualFsm fsm = (VisualFsm)we.getModelEntry().getVisualModel();
		final FsmToPetriNetConverter converter = new FsmToPetriNetConverter(fsm);
		final Framework framework = Framework.getInstance();
		final Workspace workspace = framework.getWorkspace();
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String desiredName = we.getWorkspacePath().getNode();
		final VisualPetriNet pn = converter.getPetriNet();
		final ModelEntry me = new ModelEntry(new PetriNetModelDescriptor(), pn);
		workspace.add(directory, desiredName, me, false, true);
	}
}
