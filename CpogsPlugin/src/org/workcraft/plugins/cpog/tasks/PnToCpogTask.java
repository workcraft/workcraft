package org.workcraft.plugins.cpog.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.PnToCpogSettings;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.untangling.PnToCpogConverter;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PnToCpogTask implements Task<PnToCpogResult>{

	private final WorkspaceEntry we;

	// conversion-related variables
	private VisualPetriNet pn;
	private PnToCpogSettings settings;
	private VisualCPOG cpog;

	public PnToCpogTask(WorkspaceEntry we, PnToCpogSettings settings) {
		this.we = we;
		this.settings = settings;
	}


	@Override
	public Result<? extends PnToCpogResult> run(
			ProgressMonitor<? super PnToCpogResult> monitor) {

		PnToCpogResult result;
		Outcome outcome;

		// reading Petri net from workspace
		pn = (VisualPetriNet)(we.getModelEntry().getVisualModel());

		// instantiating converter
		PnToCpogConverter converter = new PnToCpogConverter(pn);

		// get the partial orders from the Petri net introduced
		cpog = converter.run(settings);

		// checking that conversion process terminated correctly
		if (cpog == null){
			result = new PnToCpogResult("Conversion failed.", null);
			outcome = Outcome.FAILED;
		}else{
			result = new PnToCpogResult("Conversion terminated correctly.", cpog);
			outcome = Outcome.FINISHED;

			final Framework framework = Framework.getInstance();
			final Workspace workspace = framework.getWorkspace();
			final Path<String> directory = we.getWorkspacePath().getParent();
			final ModelEntry me = new ModelEntry(new CpogDescriptor(), cpog);
			boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());
			workspace.add(directory, we.getTitle().concat("_cpog"), me, false, openInEditor);
		}

		return new Result<PnToCpogResult>(outcome, result);

	}

}
