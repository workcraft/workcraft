package org.workcraft.plugins.xbm.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.xbm.*;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class MaximalSetPropertyVerification extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Maximal Set Property Check";
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final Xbm xbm = WorkspaceUtils.getAs(we, Xbm.class);
        HashSet<BurstEvent> commonBursts = findSignalChangesInMoreThanOneBurst(xbm);

        if (commonBursts.isEmpty()) {
            DialogUtils.showInfo("This model holds the maximal set property.", TITLE);
        } else {
            String msg = "The maximal set property was violated due to common signal changes found in the following bursts:\n" + getBurstEventsAsString(xbm, commonBursts)
                    + "\n\nSelect common input bursts?\n";
            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                VisualXbm visualXbm = WorkspaceUtils.getAs(we, VisualXbm.class);
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                visualXbm.selectNone();
                for (VisualBurstEvent vBurstEvent: Hierarchy.getDescendantsOfType(visualXbm.getRoot(), VisualBurstEvent.class)) {
                    BurstEvent burstEvent = vBurstEvent.getReferencedConnection();
                    if (commonBursts.contains(burstEvent)) {
                        visualXbm.addToSelection(vBurstEvent);
                    }
                }
            }
        }
        return commonBursts.isEmpty();
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Xbm.class);
    }

    //An input burst in a given state cannot be a subset of another state
    private HashSet<BurstEvent> findSignalChangesInMoreThanOneBurst(Xbm xbm) {
        Collection<BurstEvent> burstEvents = xbm.getBurstEvents();
        HashSet<BurstEvent> result = new LinkedHashSet<>();
        for (BurstEvent event1: burstEvents) {
            for (BurstEvent event2: burstEvents) {
                if (event1 != event2) {
                    Burst burst1 = event1.getBurst();
                    Burst burst2 = event2.getBurst();
                    Map<XbmSignal, Direction> burst1InputDirection = new LinkedHashMap<>(burst1.getDirections(XbmSignal.Type.INPUT));
                    Map<XbmSignal, Direction> burst2InputDirection = new LinkedHashMap<>(burst2.getDirections(XbmSignal.Type.INPUT));
                    if (burst1.getFrom() == burst2.getFrom() && burst1InputDirection.entrySet().containsAll(burst2InputDirection.entrySet())) {
                        result.add(event1);
                        result.add(event2);
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
