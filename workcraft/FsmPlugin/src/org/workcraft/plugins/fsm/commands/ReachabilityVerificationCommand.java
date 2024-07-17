package org.workcraft.plugins.fsm.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.utils.FsmUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class ReachabilityVerificationCommand
        extends AbstractVerificationCommand implements ScriptableCommand<Boolean> {

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
    public void run(WorkspaceEntry we) {
        execute(we);
    }

    @Override
    public final Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final Fsm fsm = WorkspaceUtils.getAs(we, Fsm.class);
        Set<State> unreachableStates = calcUnreachableStates(fsm);
        if (unreachableStates.isEmpty()) {
            DialogUtils.showInfo("The model does not have unreachable states.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsWrapString(fsm, unreachableStates);
            String message = "The model has unreachable state:\n" + refStr;
            String question = "\n\nSelect unreachable states?\n";
            Framework framework = Framework.getInstance();
            if (framework.isInGuiMode()
                    && DialogUtils.showConfirmWarning(message, question, TITLE, true)) {

                MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualFsm visualFsm = WorkspaceUtils.getAs(we, VisualFsm.class);
                SelectionHelper.selectByReferencedComponents(visualFsm, unreachableStates);
            }
        }
        return unreachableStates.isEmpty();
    }

    private Set<State> calcUnreachableStates(final Fsm fsm) {
        Map<State, Set<Event>> stateEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);

        Set<State> visited = new HashSet<>();
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

        Set<State> unreachableStates = new HashSet<>(fsm.getStates());
        unreachableStates.removeAll(visited);
        return unreachableStates;
    }

}
