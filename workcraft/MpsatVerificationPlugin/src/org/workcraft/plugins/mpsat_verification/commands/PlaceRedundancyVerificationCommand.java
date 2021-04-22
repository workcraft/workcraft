package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class PlaceRedundancyVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements NodeTransformer, ScriptableDataCommand<Boolean, Collection<String>> {

    private static final String PLACE_REDUNDANCY_NAMES_REPLACEMENT =
            "/* insert place names for redundancy check */"; // For example: "p1", "<a+;b->

    private static final String PLACE_REDUNDANCY_REACH =
            "// Checks whether the given set of places can be removed from the net without affecting its behaviour, in the\n" +
            "// sense that no transition can be disabled solely because of the absence of tokens on any of these places.\n" +
            "let\n" +
            "    PNAMES = {" + PLACE_REDUNDANCY_NAMES_REPLACEMENT + "\"\"} \\ {\"\"},\n" +
            "    PL = gather pn in PNAMES { P pn } +\n" +
            "        gather p in PLACES s.t. exists pn in PNAMES { (name p)[..len pn] = pn + \"@\" } { p }\n" +
            "{\n" +
            "    exists t in post PL {\n" +
            "        ~@t\n" +
            "        &\n" +
            "        forall p in pre t \\ PL { $p }\n" +
            "    }\n" +
            "}\n";

    @Override
    public String getDisplayName() {
        return "Redundancy of selected places [MPSat]";
    }

    @Override
    public String getPopupName() {
        return "Check place redundancy";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualPlace) || (node instanceof VisualImplicitPlaceArc);
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

    @Override
    public void run(WorkspaceEntry we) {
        VisualModel model = we.getModelEntry().getVisualModel();
        Collection<String> data = new ArrayList<>();
        for (VisualNode node : model.getSelection()) {
            if ((node instanceof VisualPlace) || (node instanceof VisualImplicitPlaceArc)) {
                String placeRef = model.getMathReference(node);
                if (placeRef != null) {
                    data.add(placeRef);
                }
            }
        }
        if (data.isEmpty()) {
            DialogUtils.showWarning("At least one place must be selected for redundancy check.");
            return;
        }
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
        run(we, data, monitor);
    }

    @Override
    public void run(WorkspaceEntry we, Collection<String> data, ProgressMonitor monitor) {
        if (data.isEmpty()) {
            DialogUtils.showWarning("No places specified for redundancy check.");
            monitor.isFinished(Result.cancel());
            return;
        }

        String str = data.stream().map(ref -> "\"" + ref + "\", ").collect(Collectors.joining());
        String reach = PLACE_REDUNDANCY_REACH.replace(PLACE_REDUNDANCY_NAMES_REPLACEMENT, str);

        VerificationParameters verificationParameters = new VerificationParameters("Place redundancy",
                VerificationMode.REACHABILITY_REDUNDANCY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {

            @Override
            public String getPropertyCheckMessage(boolean propertyHolds) {
                return "The selected places are " + (propertyHolds ? "redundant." : "essential.");
            }
        };

        TaskManager manager = Framework.getInstance().getTaskManager();
        VerificationChainTask task = new VerificationChainTask(we, verificationParameters);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
    }

    @Override
    public Collection<String> deserialiseData(String data) {
        return TextUtils.splitWords(data);
    }

    @Override
    public Boolean execute(WorkspaceEntry we, Collection<String> data) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, false);
        run(we, data, monitor);
        return monitor.waitForHandledResult();
    }

}
