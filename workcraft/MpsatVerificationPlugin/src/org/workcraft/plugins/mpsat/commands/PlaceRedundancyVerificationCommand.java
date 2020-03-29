package org.workcraft.plugins.mpsat.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashSet;
import java.util.stream.Collectors;

public class PlaceRedundancyVerificationCommand extends AbstractVerificationCommand implements NodeTransformer {

    private static final String REACH_PLACE_REDUNDANCY_NAMES =
            "/* insert place names for redundancy check */"; // For example: "p1", "<a+;b->

    private static final String REACH_PLACE_REDUNDANCY =
            "// Checks whether the given set of places can be removed from the net without affecting its behaviour, in the\n" +
            "// sense that no transition can be disabled solely because of the absence of tokens on any of these places.\n" +
            "let\n" +
            "    PNAMES = {" + REACH_PLACE_REDUNDANCY_NAMES + "\"\"} \\ {\"\"},\n" +
            "    PL = gather pn in PNAMES { P pn }\n" +
            "{\n" +
            "    exists t in TRANSITIONS {\n" +
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
        if (getSelectedPlaces(we).isEmpty()) {
            DialogUtils.showWarning("At least one place must be selected for redundancy check.");
            return;
        }
        super.run(we);
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        HashSet<String> placeNames = getSelectedPlaces(we);
        String str = placeNames.stream().map(ref -> "\"" + ref + "\", ").collect(Collectors.joining());
        String reachPlaceRedundancy = REACH_PLACE_REDUNDANCY.replace(REACH_PLACE_REDUNDANCY_NAMES, str);
        return new VerificationParameters("Place redundancy",
                VerificationMode.REACHABILITY_REDUNDANCY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reachPlaceRedundancy, true);
    }

    protected HashSet<String> getSelectedPlaces(WorkspaceEntry we) {
        VisualModel model = we.getModelEntry().getVisualModel();
        HashSet<String> placeNames = new HashSet<>();
        for (VisualNode node: model.getSelection()) {
            if ((node instanceof VisualPlace) || (node instanceof VisualImplicitPlaceArc)) {
                String placeName = model.getMathReference(node);
                if (placeName != null) {
                    placeNames.add(placeName);
                }
            }
        }
        return placeNames;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        // No transformation applied.
    }

}
