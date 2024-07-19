package org.workcraft.plugins.fsm.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.fsm.utils.FsmUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeterminismVerificationCommand
        extends AbstractVerificationCommand implements ScriptableCommand<Boolean> {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Determinism";
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
    public Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        Fsm fsm = WorkspaceUtils.getAs(we, Fsm.class);
        Set<State> nondeterministicStates = calcNondeterministicStates(fsm);
        if (nondeterministicStates.isEmpty()) {
            DialogUtils.showInfo("The model is deterministic.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsWrapString(fsm, nondeterministicStates);
            String message = "The model has non-deterministic states:\n" + refStr;
            String question = "\n\nSelect non-deterministic states?\n";
            Framework framework = Framework.getInstance();
            if (framework.isInGuiMode()
                    && DialogUtils.showConfirmWarning(message, question, TITLE, true)) {

                MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualFsm visualFsm = WorkspaceUtils.getAs(we, VisualFsm.class);
                SelectionHelper.selectByReferencedComponents(visualFsm, nondeterministicStates);
            }
        }
        return nondeterministicStates.isEmpty();
    }

    private Set<State> calcNondeterministicStates(final Fsm fsm) {
        Set<State> nondeterministicStates = new HashSet<>();
        Map<State, Set<Event>> stateEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);
        for (State state: stateEvents.keySet()) {
            Set<Symbol> symbols = new HashSet<>();
            for (Event event: stateEvents.get(state)) {
                Symbol symbol = event.getSymbol();
                if (!fsm.isDeterministicSymbol(symbol) || symbols.contains(symbol)) {
                    nondeterministicStates.add(state);
                } else {
                    symbols.add(symbol);
                }
            }
        }
        return nondeterministicStates;
    }

}
