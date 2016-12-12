package org.workcraft.plugins.fsm.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class DeterminismCheckerTool extends VerificationTool {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Determinism";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, Fsm.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final Fsm fsm = (Fsm) we.getModelEntry().getMathModel();
        HashSet<State> nondeterministicStates = checkDeterminism(fsm);
        if (nondeterministicStates.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "The model is deterministic.",
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(fsm, (Collection) nondeterministicStates);
            if (JOptionPane.showConfirmDialog(mainWindow,
                    "The model has non-deterministic state:\n" + refStr + "\n\nSelect non-deterministic states?\n",
                    TITLE, JOptionPane.WARNING_MESSAGE + JOptionPane.YES_NO_OPTION) == 0) {

                VisualFsm visualFsm = (VisualFsm) we.getModelEntry().getVisualModel();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                SelectionHelper.selectByReferencedComponents(visualFsm, (HashSet) nondeterministicStates);
            }
        }
        return we;
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
