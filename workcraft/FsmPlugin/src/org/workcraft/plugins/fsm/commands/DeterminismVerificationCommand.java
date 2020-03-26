package org.workcraft.plugins.fsm.commands;

import java.util.HashMap;
import java.util.HashSet;

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
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.utils.FsmUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class DeterminismVerificationCommand extends AbstractVerificationCommand implements ScriptableCommand<Boolean> {

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
        final Fsm fsm = WorkspaceUtils.getAs(we, Fsm.class);
        HashSet<State> nondeterministicStates = checkDeterminism(fsm);
        if (nondeterministicStates.isEmpty()) {
            DialogUtils.showInfo("The model is deterministic.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(fsm, nondeterministicStates, SizeHelper.getWrapLength());
            String msg = "The model has non-deterministic states:\n" + refStr + "\n\nSelect non-deterministic states?\n";
            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualFsm visualFsm = WorkspaceUtils.getAs(we, VisualFsm.class);
                SelectionHelper.selectByReferencedComponents(visualFsm, nondeterministicStates);
            }
        }
        return nondeterministicStates.isEmpty();
    }

    private HashSet<State> checkDeterminism(final Fsm fsm) {
        HashSet<State> nondeterministicStates = new HashSet<>();
        HashMap<State, HashSet<Event>> stateEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);
        for (State state: stateEvents.keySet()) {
            HashSet<Symbol> symbols = new HashSet<>();
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
