package org.workcraft.plugins.petrify.tasks;

import java.util.Collection;
import java.util.HashMap;

import org.workcraft.Framework;
import org.workcraft.dom.Connection;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.UniqueReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

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
            MutexUtils.restoreMutexPlacesByContext(stgModel, mutexes);
            PetriNetModel model = convertToPetriNet ? convertStgToPetriNet(stgModel) : stgModel;
            final ModelDescriptor modelDescriptor = convertToPetriNet ? new PetriNetDescriptor() : new StgDescriptor();
            final ModelEntry me = new ModelEntry(modelDescriptor, model);
            final Path<String> path = we.getWorkspacePath();
            final Framework framework = Framework.getInstance();
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
        PetriNet dstModel = new PetriNet();
        HashMap<Node, Node> nodeMap = new HashMap<>();
        for (Place place: srcModel.getPlaces()) {
            Place newPlace = dstModel.createPlace(null, null);
            if (newPlace != null) {
                newPlace.setCapacity(place.getCapacity());
                newPlace.setTokens(place.getTokens());
                nodeMap.put(place, newPlace);
            }
        }

        for (Transition transition: srcModel.getTransitions()) {
            String srcName = srcModel.getName(transition);
            String dstName = convertName(srcModel, dstModel, srcName);
            Transition newTransition = dstModel.createTransition(dstName, null);
            nodeMap.put(transition, newTransition);
        }

        for (Connection connection: srcModel.getConnections()) {
            Node first = nodeMap.get(connection.getFirst());
            Node second = nodeMap.get(connection.getSecond());
            try {
                dstModel.connect(first, second);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }
        return dstModel;
    }

    private String convertName(StgModel srcModel, PetriNet dstModel, String srcName) {
        String candidateName = LabelParser.getTransitionName(srcName);
        candidateName = candidateName.replace("+", "_PLUS").replace("-", "_MINUS").replace("~", "_TOGGLE");

        UniqueReferenceManager refManager
                = (UniqueReferenceManager) dstModel.getReferenceManager();

        NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(dstModel.getRoot());
        NameManager nameManagerer = refManager.getNameManager(namespaceProvider);
        return nameManagerer.getDerivedName(null, candidateName);
    }

}
