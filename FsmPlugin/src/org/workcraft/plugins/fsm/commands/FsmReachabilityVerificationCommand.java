package org.workcraft.plugins.fsm.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class FsmReachabilityVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Unreachable state";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Fsm.class);
    }

    @Override
    public final Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final Fsm fsm = WorkspaceUtils.getAs(we, Fsm.class);
        HashSet<State> unreachableStates = checkReachability(fsm);
        if (unreachableStates.isEmpty()) {
            DialogUtils.showInfo("The model does not have unreachable states.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(fsm, unreachableStates, SizeHelper.getWrapLength());
            String msg = "The model has unreachable state:\n" + refStr + "\n\nSelect unreachable states?\n";
            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualFsm visualFsm = WorkspaceUtils.getAs(we, VisualFsm.class);
                SelectionHelper.selectByReferencedComponents(visualFsm, unreachableStates);
            }
        }
        return unreachableStates.isEmpty();
    }

    private HashSet<State> checkReachability(final Fsm fsm) {
        HashMap<State, HashSet<Event>> stateEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);

        HashSet<State> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();

        State initialState = fsm.getInitialState();
        if (initialState != null) {
            queue.add(initialState);
        }

        while (!queue.isEmpty()) {
            State curState = queue.remove();
            if (visited.contains(curState)) continue;
            visited.add(curState);
            for (Event curEvent: stateEvents.get(curState)) {
                State nextState = (State) curEvent.getSecond();
                if (nextState != null) {
                    queue.add(nextState);
                }
            }
        }

        HashSet<State> unreachableStates = new HashSet<>(fsm.getStates());
        unreachableStates.removeAll(visited);
        return unreachableStates;
    }

}
