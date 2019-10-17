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
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class NonEmptyInputBurstsVerification extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Non-empty Input Bursts";
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }

        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final Xbm xbm = WorkspaceUtils.getAs(we, Xbm.class);
        final Collection<XbmSignal> inputs = xbm.getSignals(XbmSignal.Type.INPUT);
        if (inputs.isEmpty()) {
            DialogUtils.showInfo("There were no input signals to be found in this model.", TITLE);
            return null;
        } else {
            HashSet<BurstEvent> emptyBursts = findEmptyInputBursts(xbm);
            if (emptyBursts.isEmpty()) {
                DialogUtils.showInfo("This model holds the non-empty input bursts property.", TITLE);
            } else {
                String msg = "The non-empty input bursts property was violated to the following bursts having no input changes:\n" + getBurstEventsAsString(xbm, emptyBursts)
                        + "\n\nSelect empty input bursts?\n";
                if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                    VisualXbm visualXbm = WorkspaceUtils.getAs(we, VisualXbm.class);
                    mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);

                    visualXbm.selectNone();
                    for (VisualBurstEvent vBurstEvent: Hierarchy.getDescendantsOfType(visualXbm.getRoot(), VisualBurstEvent.class)) {
                        BurstEvent burstEvent = vBurstEvent.getReferencedConnection();
                        if (emptyBursts.contains(burstEvent)) {
                            visualXbm.addToSelection(vBurstEvent);
                        }
                    }
                }
            }
            return emptyBursts.isEmpty();
        }
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Xbm.class);
    }

    private HashSet<BurstEvent> findEmptyInputBursts(Xbm xbm) {
        Collection<BurstEvent> burstEvents = xbm.getBurstEvents();
        HashSet<BurstEvent> result = new LinkedHashSet<>();
        for (BurstEvent event: burstEvents) {
            boolean isEmptyInputBurst = event.getBurst().getSignals(XbmSignal.Type.INPUT).isEmpty() && !event.hasConditional();
            if (isEmptyInputBurst) {
                result.add(event);
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
