package org.workcraft.plugins.xbm.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.xbm.BurstEvent;
import org.workcraft.plugins.xbm.VisualBurstEvent;
import org.workcraft.plugins.xbm.VisualXbm;
import org.workcraft.plugins.xbm.Xbm;
import org.workcraft.plugins.xbm.XbmSignal;
import org.workcraft.plugins.xbm.Burst;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class DistinguishabilityConstraintVerification extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Distinguishability Constraint";
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final Xbm xbm = WorkspaceUtils.getAs(we, Xbm.class);
        Set<BurstEvent> commonConditionals = findNonMutuallyExclusiveConditionals(xbm);
        Set<BurstEvent> subsetCompulsories = findSubsetCompulsoryEdges(xbm);
        String msg = "";
        if (commonConditionals.isEmpty() && subsetCompulsories.isEmpty()) {
            DialogUtils.showInfo("This model holds the distinguishability constraint property.", TITLE);
        } else if (!commonConditionals.isEmpty() && subsetCompulsories.isEmpty()) {
            msg = "The distinguishability constraint property was violated due to common conditionals found in the following bursts:\n" + getBurstEventsAsString(xbm, commonConditionals);
        } else if (commonConditionals.isEmpty() && !subsetCompulsories.isEmpty()) {
            msg = "The distinguishability constraint property was violated due to compulsory edges found as a subset in the following bursts:\n" + getBurstEventsAsString(xbm, subsetCompulsories);
        } else {
            msg = "The distinguishability constraint property was violated due to non-mutually exclusive conditionals  in the following bursts:\n" + getBurstEventsAsString(xbm, commonConditionals) +
                         "\nAnd compulsory edges found as a subset:\n" + getBurstEventsAsString(xbm, subsetCompulsories);
        }

        if (!msg.isEmpty()) {
            msg += "\n\nSelect conflicting input bursts?\n";
            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                VisualXbm visualXbm = WorkspaceUtils.getAs(we, VisualXbm.class);
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                HashSet<BurstEvent> allBurstEvents = new HashSet<>();
                allBurstEvents.addAll(commonConditionals);
                allBurstEvents.addAll(subsetCompulsories);
                visualXbm.selectNone();
                for (VisualBurstEvent vBurstEvent: Hierarchy.getDescendantsOfType(visualXbm.getRoot(), VisualBurstEvent.class)) {
                    BurstEvent burstEvent = vBurstEvent.getReferencedConnection();
                    if (allBurstEvents.contains(burstEvent)) {
                        visualXbm.addToSelection(vBurstEvent);
                    }
                }
            }
        }
        return commonConditionals.isEmpty();
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Xbm.class);
    }

    private Set<BurstEvent> findNonMutuallyExclusiveConditionals(Xbm xbm) {
        Collection<BurstEvent> burstEvents = xbm.getBurstEvents();
        Set<BurstEvent> result = new LinkedHashSet<>();
        for (BurstEvent first: burstEvents) {
            for (BurstEvent second: burstEvents) {
                if (first != second && first.hasConditional() && second.hasConditional() &&
                        first.getBurst().getFrom() == second.getBurst().getFrom() && first.getConditional().equals(second.getConditional())) {

                    result.add(first);
                    result.add(second);
                }
            }
        }
        return result;
    }

    private Set<BurstEvent> findSubsetCompulsoryEdges(Xbm xbm) {
        Collection<BurstEvent> burstEvents = xbm.getBurstEvents();
        Set<BurstEvent> result = new LinkedHashSet<>();
        for (BurstEvent first: burstEvents) {
            for (BurstEvent second : burstEvents) {
                if (first != second && !first.hasConditional() && !second.hasConditional() && first.getBurst().getFrom() == second.getBurst().getFrom()) {
                    Set<XbmSignal> firstBurstInputs = first.getBurst().getSignals(XbmSignal.Type.INPUT);
                    boolean isASubset = true;
                    for (XbmSignal input: firstBurstInputs) {
                        if (!second.getBurst().getDirection().keySet().contains(input) || (second.getBurst().getDirection().get(input) != Burst.Direction.UNSTABLE && first.getBurst().getDirection().get(input) != second.getBurst().getDirection().get(input))) {
                            isASubset = false;
                        }
                    }
                    if (isASubset) {
                        result.add(first);
                        result.add(second);
                    }
                }
            }
        }
        return result;
    }

    private static String getBurstEventsAsString(Xbm xbm, Set<BurstEvent> burstEvents) {
        String result = "";
        for (BurstEvent event: burstEvents) {
            if (!result.isEmpty()) {
                result += ", ";
            }
            result += xbm.getNodeReference(event.getBurst().getFrom()) + "->" + xbm.getNodeReference(event.getBurst().getTo());
        }
        return result;
    }
}
