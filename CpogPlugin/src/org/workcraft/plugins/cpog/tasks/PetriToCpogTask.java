package org.workcraft.plugins.cpog.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.PetriToCpogSettings;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.untangling.PetriToCpogConverter;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriToCpogTask implements Task<PetriToCpogResult> {

    private final WorkspaceEntry we;

    // conversion-related variables
    private final PetriToCpogSettings settings;

    public PetriToCpogTask(WorkspaceEntry we, PetriToCpogSettings settings) {
        this.we = we;
        this.settings = settings;
    }

    @Override
    public Result<? extends PetriToCpogResult> run(
            ProgressMonitor<? super PetriToCpogResult> monitor) {

        PetriToCpogResult result;
        Outcome outcome;

        // reading Petri net from workspace
        VisualPetriNet petri = (VisualPetriNet) (we.getModelEntry().getVisualModel());

        // instantiating converter
        PetriToCpogConverter converter = new PetriToCpogConverter(petri);

        // get the partial orders from the Petri net introduced
        VisualCpog cpog = converter.run(settings);

        // checking that conversion process terminated correctly
        if (cpog == null) {
            result = new PetriToCpogResult("Conversion failed.", null);
            outcome = Outcome.FAILED;
        } else {
            result = new PetriToCpogResult("Conversion terminated correctly.", cpog);
            outcome = Outcome.FINISHED;

            final Framework framework = Framework.getInstance();
            final Workspace workspace = framework.getWorkspace();
            final Path<String> directory = we.getWorkspacePath().getParent();
            final ModelEntry me = new ModelEntry(new CpogDescriptor(), cpog);
            boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
            workspace.addWork(directory, we.getTitle().concat("_cpog"), me, false, openInEditor);
        }

        return new Result<PetriToCpogResult>(outcome, result);

    }

}
