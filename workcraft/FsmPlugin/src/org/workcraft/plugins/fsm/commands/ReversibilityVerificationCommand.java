package org.workcraft.plugins.fsm.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.utils.FsmUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class ReversibilityVerificationCommand extends AbstractVerificationCommand implements ScriptableCommand<Boolean> {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Reversibility";
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
        HashSet<State> irreversibleStates = checkReversibility(fsm);
        if (irreversibleStates.isEmpty()) {
            DialogUtils.showInfo("The model is reversible.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(fsm, irreversibleStates, SizeHelper.getWrapLength());
            String msg = "The model has irreversible states:\n" + refStr + "\n\nSelect irreversible states?\n";
            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualFsm visualFsm = WorkspaceUtils.getAs(we, VisualFsm.class);
                SelectionHelper.selectByReferencedComponents(visualFsm, irreversibleStates);
            }
        }
        return irreversibleStates.isEmpty();
    }

    private HashSet<State> checkReversibility(final Fsm fsm) {

        State initialState = fsm.getInitialState();
        HashSet<State> forwardStates = getForwardReachableStates(fsm, initialState);
        HashSet<State> backwardStates = getBackwardReachableStates(fsm, initialState);

        HashSet<State> ireversibleStates = new HashSet<>(forwardStates);
        ireversibleStates.removeAll(backwardStates);
        return ireversibleStates;
    }

    private HashSet<State> getForwardReachableStates(final Fsm fsm, State initialState) {
        HashMap<State, HashSet<Event>> stateSuccEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);

        HashSet<State> visitedStates = new HashSet<>();
        Queue<State> queueStates = new LinkedList<>();

        if (initialState != null) {
            queueStates.add(initialState);
        }

        while (!queueStates.isEmpty()) {
            State curState = queueStates.remove();
            if (visitedStates.contains(curState)) continue;
            visitedStates.add(curState);
            for (Event curEvent: stateSuccEvents.get(curState)) {
                State nextState = (State) curEvent.getSecond();
                if (nextState != null) {
                    queueStates.add(nextState);
                }
            }
        }
        return visitedStates;
    }

    private HashSet<State> getBackwardReachableStates(final Fsm fsm, State initialState) {
        HashMap<State, HashSet<Event>> statePrevEvents = FsmUtils.calcStateIncommingEventsMap(fsm);

        HashSet<State> visitedStates = new HashSet<>();
        Queue<State> queueStates = new LinkedList<>();

        if (initialState != null) {
            queueStates.add(initialState);
        }

        while (!queueStates.isEmpty()) {
            State curState = queueStates.remove();
            if (visitedStates.contains(curState)) continue;
            visitedStates.add(curState);
            for (Event curEvent: statePrevEvents.get(curState)) {
                State prevState = (State) curEvent.getFirst();
                if (prevState != null) {
                    queueStates.add(prevState);
                }
            }
        }
        return visitedStates;
    }

}
