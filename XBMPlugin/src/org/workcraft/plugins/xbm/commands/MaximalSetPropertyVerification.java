package org.workcraft.plugins.xbm.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.xbm.*;
import org.workcraft.plugins.xbm.XbmSignal;
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
                    BurstEvent burstEvent = vBurstEvent.getReferencedBurstEvent();
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

    private HashSet<BurstEvent> findSignalChangesInMoreThanOneBurst(Xbm xbm) {
        Collection<BurstEvent> burstEvents = xbm.getBurstEvents();
        HashSet<BurstEvent> result = new LinkedHashSet<>();
        for (BurstEvent event1: burstEvents) {
            for (BurstEvent event2: burstEvents) {
                if (event1 != event2) {
                    Burst b1 = event1.getBurst();
                    Burst b2 = event2.getBurst();
                    Set<Map.Entry<XbmSignal, Burst.Direction>> b1map = new LinkedHashSet<>();
                    Set<Map.Entry<XbmSignal, Burst.Direction>> b2map = new LinkedHashSet<>();
                    for (Map.Entry<XbmSignal, Burst.Direction> entry: b1.getDirection().entrySet()) {
                        XbmSignal xbmSignal = entry.getKey();
                        if (xbmSignal.getType() == XbmSignal.Type.INPUT && (entry.getValue() == Burst.Direction.PLUS || entry.getValue() == Burst.Direction.MINUS)) {
                            b1map.add(entry);
                        }
                    }
                    for (Map.Entry<XbmSignal, Burst.Direction> entry: b2.getDirection().entrySet()) {
                        XbmSignal xbmSignal = entry.getKey();
                        if (xbmSignal.getType() == XbmSignal.Type.INPUT && (entry.getValue() == Burst.Direction.PLUS || entry.getValue() == Burst.Direction.MINUS)) {
                            b2map.add(entry);
                        }
                    }
                    boolean moreThanOneOccurrence = (b1map.containsAll(b2map)) || b2map.containsAll(b1map);
                    if (moreThanOneOccurrence) {
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
