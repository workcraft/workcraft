package org.workcraft.plugins.stg.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriNetToStgConverterTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Signal Transition Graph";
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNet;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualPetriNet pn = (VisualPetriNet)we.getModelEntry().getVisualModel();
		final VisualSTG stg = new VisualSTG(new STG());
		final PetriNetToStgConverter converter = new PetriNetToStgConverter(pn, stg);
		final Framework framework = Framework.getInstance();
		final Workspace workspace = framework.getWorkspace();
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String name = we.getWorkspacePath().getNode();
		final ModelEntry me = new ModelEntry(new StgDescriptor(), converter.getDstModel());
		boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());
		workspace.add(directory, name, me, false, openInEditor);
	}

}
