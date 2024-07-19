package org.workcraft.plugins.mpsat_verification.gui;

import org.workcraft.gui.dialogs.ListDataDialog;
import org.workcraft.gui.lists.ColorListCellRenderer;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat_verification.presets.LocalSelfTriggeringDataPreserver;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.WorkspaceUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LocalSelfTriggeringDialog extends ListDataDialog {

    public LocalSelfTriggeringDialog(Window owner, LocalSelfTriggeringDataPreserver userData) {
        super(owner, "Absence of local self-triggering", userData);
    }

    @Override
    public DefaultListCellRenderer getItemListCellRenderer() {
        Stg stg = WorkspaceUtils.getAs(getUserData().getWorkspaceEntry(), Stg.class);
        Set<String> outputSignals = stg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> internalSignals = stg.getSignalReferences(Signal.Type.INTERNAL);
        return new ColorListCellRenderer(signal -> {
            if (outputSignals.contains(signal)) {
                return SignalCommonSettings.getOutputColor();
            } else if (internalSignals.contains(signal)) {
                return SignalCommonSettings.getInternalColor();
            } else {
                return null;
            }
        });
    }

    @Override
    public Collection<String> getItems() {
        Collection<String> result = new HashSet<>();
        Stg stg = WorkspaceUtils.getAs(getUserData().getWorkspaceEntry(), Stg.class);
        result.addAll(getSyntacticSelfTriggeringSignals(stg, stg.getSignalReferences(Signal.Type.OUTPUT)));
        result.addAll(getSyntacticSelfTriggeringSignals(stg, stg.getSignalReferences(Signal.Type.INTERNAL)));
        return result;
    }

    private Set<String> getSyntacticSelfTriggeringSignals(Stg stg, Collection<String> signals) {
        Set<String> result = new HashSet<>();
        for (String signal : signals) {
            Collection<SignalTransition> signalTransitions = stg.getSignalTransitions(signal);
            for (Transition signalTransition : signalTransitions) {
                Collection<Transition> syntacticTriggerTransitions =
                        PetriUtils.getSyntacticTriggerTransitions(stg, signalTransition);

                syntacticTriggerTransitions.remove(signalTransition);
                syntacticTriggerTransitions.retainAll(signalTransitions);
                if (!syntacticTriggerTransitions.isEmpty()) {
                    result.add(signal);
                    break;
                }
            }
        }
        return result;
    }

}
