package org.workcraft.plugins.fsm.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.AbstractVerificationCommand;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.util.MessageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class FsmReversibilityVerificationCommand extends AbstractVerificationCommand {

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
    public final void run(WorkspaceEntry we) {
        final Fsm fsm = WorkspaceUtils.getAs(we, Fsm.class);
        HashSet<State> irreversibleStates = checkReversibility(fsm);
        if (irreversibleStates.isEmpty()) {
            MessageUtils.showInfo("The model is reversible.", TITLE);
        } else {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            String refStr = ReferenceHelper.getNodesAsString(fsm, (Collection) irreversibleStates, 50);
            if (JOptionPane.showConfirmDialog(mainWindow,
                    "The model has irreversible states:\n" + refStr + "\n\nSelect irreversible states?\n",
                    TITLE, JOptionPane.WARNING_MESSAGE + JOptionPane.YES_NO_OPTION) == 0) {

                VisualFsm visualFsm = WorkspaceUtils.getAs(we, VisualFsm.class);
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                SelectionHelper.selectByReferencedComponents(visualFsm, (HashSet) irreversibleStates);
            }
        }
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
