package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.Connection;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyTransformationResultHandler extends DummyProgressMonitor<PetrifyTransformationResult> {
    private final WorkspaceEntry we;
    private final boolean convertResultStgToPetriNet;
    private WorkspaceEntry result;

    public PetrifyTransformationResultHandler(WorkspaceEntry we) {
        this(we, true);
    }

    public PetrifyTransformationResultHandler(WorkspaceEntry we, boolean convertResultStgToPetriNet) {
        this.we = we;
        this.convertResultStgToPetriNet = convertResultStgToPetriNet;
        this.result = null;
    }

    @Override
    public void finished(final Result<? extends PetrifyTransformationResult> result, String description) {
        final Framework framework = Framework.getInstance();
        Path<String> path = we.getWorkspacePath();
        if (result.getOutcome() == Outcome.FINISHED) {
            StgModel stgModel = result.getReturnValue().getResult();
            PetriNetModel model = convertResultStgToPetriNet ? stgModel : convertStgToPetriNet(stgModel);
            final Path<String> directory = path.getParent();
            final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
            final ModelDescriptor modelDescriptor = convertResultStgToPetriNet ? new StgDescriptor() : new PetriNetDescriptor();
            final ModelEntry me = new ModelEntry(modelDescriptor, model);
            this.result = framework.createWork(me, directory, name);
        } else if (result.getOutcome() == Outcome.FAILED) {
            MainWindow mainWindow = framework.getMainWindow();
            if (result.getCause() == null) {
                PetrifyTransformationResult returnValue = result.getReturnValue();
                Result<? extends ExternalProcessResult> petrifyResult = returnValue.getPetrifyResult();
                ExternalProcessResult petrifyReturnValue = petrifyResult.getReturnValue();
                JOptionPane.showMessageDialog(mainWindow,
                        "Petrify output: \n\n" + petrifyReturnValue.getErrorsHeadAndTail(),
                        "Transformation failed", JOptionPane.WARNING_MESSAGE);
            } else {
                ExceptionDialog.show(mainWindow, result.getCause());
            }
        }
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

        HierarchicalUniqueNameReferenceManager refManager
                = (HierarchicalUniqueNameReferenceManager) dstModel.getReferenceManager();

        NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(dstModel.getRoot());
        NameManager nameManagerer = refManager.getNameManager(namespaceProvider);
        return nameManagerer.getDerivedName(null, candidateName);
    }

    public WorkspaceEntry getResult() {
        return result;
    }

}
