package org.workcraft.plugins.fsm.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.workspace.WorkspaceEntry;

public class ReachabilityCheckerTool extends VerificationTool {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Unreachable state";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Fsm;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final Fsm fsm = (Fsm) we.getModelEntry().getMathModel();
        HashSet<State> unreachable = checkReachability(fsm);
        if (unreachable.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "The model does not have unreachable states.",
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(fsm, (Collection) unreachable);
            if (JOptionPane.showConfirmDialog(mainWindow,
                    "The model has unreachable state:\n" + refStr + "\n\nSelect unreachable states?\n",
                    TITLE, JOptionPane.WARNING_MESSAGE + JOptionPane.YES_NO_OPTION) == 0) {

                final ToolboxPanel toolbox = mainWindow.getCurrentToolbox();
                final SelectionTool selectionTool = toolbox.getToolInstance(SelectionTool.class);
                toolbox.selectTool(selectionTool);

                VisualFsm visualFsm = (VisualFsm) we.getModelEntry().getVisualModel();
                visualFsm.selectNone();
                for (VisualState visualState: visualFsm.getVisualStates()) {
                    State state = visualState.getReferencedState();
                    if (unreachable.contains(state)) {
                        visualFsm.addToSelection(visualState);
                    }
                }
            }
        }
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
