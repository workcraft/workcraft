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

public class ReversibilityVerificationCommand
        extends AbstractVerificationCommand implements ScriptableCommand<Boolean> {

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
        Set<State> irreversibleStates = calcIrreversibleStates(fsm);
        if (irreversibleStates.isEmpty()) {
            DialogUtils.showInfo("The model is reversible.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsWrapString(fsm, irreversibleStates);
            String message = "The model has irreversible states:\n" + refStr;
            String question = "\n\nSelect irreversible states?\n";
            Framework framework = Framework.getInstance();
            if (framework.isInGuiMode()
                    && DialogUtils.showConfirmWarning(message, question, TITLE, true)) {

                MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualFsm visualFsm = WorkspaceUtils.getAs(we, VisualFsm.class);
                SelectionHelper.selectByReferencedComponents(visualFsm, irreversibleStates);
            }
        }
        return irreversibleStates.isEmpty();
    }

    private Set<State> calcIrreversibleStates(final Fsm fsm) {
        State initialState = fsm.getInitialState();
        Set<State> forwardStates = getForwardReachableStates(fsm, initialState);
        Set<State> backwardStates = getBackwardReachableStates(fsm, initialState);

        Set<State> irreversibleStates = new HashSet<>(forwardStates);
        irreversibleStates.removeAll(backwardStates);
        return irreversibleStates;
    }

    private Set<State> getForwardReachableStates(final Fsm fsm, State initialState) {
        Map<State, Set<Event>> stateSuccEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);

        Set<State> visitedStates = new HashSet<>();
        Queue<State> queueStates = new ArrayDeque<>();

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

    private Set<State> getBackwardReachableStates(final Fsm fsm, State initialState) {
        Map<State, Set<Event>> statePrevEvents = FsmUtils.calcStateIncommingEventsMap(fsm);

        Set<State> visitedStates = new HashSet<>();
        Queue<State> queueStates = new ArrayDeque<>();

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
