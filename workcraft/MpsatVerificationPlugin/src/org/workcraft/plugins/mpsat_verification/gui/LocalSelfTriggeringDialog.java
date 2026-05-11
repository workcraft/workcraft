package org.workcraft.plugins.mpsat_verification.gui;

import org.workcraft.gui.dialogs.ListDataDialog;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat_verification.commands.LocalSelfTriggeringVerificationCommand;
import org.workcraft.plugins.mpsat_verification.presets.LocalSelfTriggeringDataPreserver;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.WorkspaceUtils;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LocalSelfTriggeringDialog extends ListDataDialog {

    private Stg stg;
    private Set<String> outputSignals;
    private Set<String> internalSignals;

    public LocalSelfTriggeringDialog(Window owner, LocalSelfTriggeringDataPreserver userData) {
        super(owner, LocalSelfTriggeringVerificationCommand.TITLE, userData);
    }

    @Override
    public Color getItemColorOrNullForInvalid(Object item) {
        if ((item instanceof String str) && outputSignals.contains(str)) {
            return SignalCommonSettings.getOutputColor();
        } else if ((item instanceof String str) && internalSignals.contains(str)) {
            return SignalCommonSettings.getInternalColor();
        } else {
            return super.getItemColorOrNullForInvalid(item);
        }
    }

    @Override
    public Collection<String> getItems() {
        Collection<String> result = new HashSet<>();
        if (stg == null) {
            stg = WorkspaceUtils.getAs(getUserData().getWorkspaceEntry(), Stg.class);
            outputSignals = stg.getSignalReferences(Signal.Type.OUTPUT);
            internalSignals = stg.getSignalReferences(Signal.Type.INTERNAL);
        }
        result.addAll(getSyntacticSelfTriggeringSignals(stg, outputSignals));
        result.addAll(getSyntacticSelfTriggeringSignals(stg, internalSignals));
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
