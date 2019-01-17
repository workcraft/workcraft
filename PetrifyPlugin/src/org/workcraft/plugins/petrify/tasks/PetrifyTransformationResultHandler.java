package org.workcraft.plugins.petrify.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.converters.StgToPetriConverter;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public class PetrifyTransformationResultHandler extends AbstractExtendedResultHandler<PetrifyTransformationOutput, WorkspaceEntry> {
    private final WorkspaceEntry we;
    private final boolean convertToPetriNet;
    private final Collection<Mutex> mutexes;

    public PetrifyTransformationResultHandler(WorkspaceEntry we, boolean convertToPetriNet, Collection<Mutex> mutexes) {
        this.we = we;
        this.convertToPetriNet = convertToPetriNet;
        this.mutexes = mutexes;
    }

    @Override
    public WorkspaceEntry handleResult(final Result<? extends PetrifyTransformationOutput> result) {
        WorkspaceEntry weResult = null;
        PetrifyTransformationOutput output = result.getPayload();
        if (result.getOutcome() == Outcome.SUCCESS) {
            StgModel stgModel = output.getStg();
            MutexUtils.restoreMutexSignals(stgModel, mutexes);
            MutexUtils.restoreMutexPlacesByContext(stgModel, mutexes);
            PetriNetModel model = convertToPetriNet ? convertStgToPetriNet(stgModel) : stgModel;
            ModelDescriptor modelDescriptor = convertToPetriNet ? new PetriNetDescriptor() : new StgDescriptor();
            ModelEntry me = new ModelEntry(modelDescriptor, model);
            Path<String> path = we.getWorkspacePath();
            Framework framework = Framework.getInstance();
            weResult = framework.createWork(me, path);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            if (result.getCause() != null) {
                ExceptionDialog.show(result.getCause());
            } else {
                DialogUtils.showWarning("Transformation failed. Petrify output: \n\n" + output.getErrorsHeadAndTail());
            }
        }
        return weResult;
    }

    private PetriNetModel convertStgToPetriNet(StgModel srcModel) {
        VisualStg stg = new VisualStg((Stg) srcModel);
        VisualPetriNet petri = new VisualPetriNet(new PetriNet());
        StgToPetriConverter converter = new StgToPetriConverter(stg, petri);
        return converter.getDstModel().getPetriNet();
    }

}
