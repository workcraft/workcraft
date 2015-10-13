package org.workcraft.plugins.stg.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToPetriNetConverterTool extends ConversionTool {

	@Override
	public String getDisplayName() {
		return "Petri Net";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		we.captureMemento();
		try {
			final VisualSTG stg = (VisualSTG)we.getModelEntry().getVisualModel();
			final VisualPetriNet pn = new VisualPetriNet(new PetriNet());
			final StgToPetriNetConverter converter = new StgToPetriNetConverter(stg, pn);
			final Framework framework = Framework.getInstance();
			final Workspace workspace = framework.getWorkspace();
			final Path<String> directory = we.getWorkspacePath().getParent();
			final String name = we.getWorkspacePath().getNode();
			final ModelEntry me = new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
			boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());
			workspace.add(directory, name, me, false, openInEditor);
		} finally {
			we.cancelMemento();
		}
	}

}
