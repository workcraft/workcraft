package org.workcraft.plugins.wtg.observers;

import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.utils.DialogUtils;

import java.util.Collection;

public class GuardNameSupervisor extends StateSupervisor {

    private final Wtg wtg;

    public GuardNameSupervisor(Wtg wtg) {
        this.wtg = wtg;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            if ((sender instanceof Waveform) && pce.getPropertyName().equals(Waveform.PROPERTY_GUARD)) {
                Waveform waveform = (Waveform) sender;
                if (!guardedSignalExist(waveform.getGuard())) {
                    DialogUtils.showWarning("Guard set for an unknown signal.");
                }
            }
        }
    }

    private boolean guardedSignalExist(Guard guard) {
        Collection<String> signals = wtg.getSignalNames();
        for (String signal : guard.keySet()) {
            if (!signals.contains(signal)) {
                return false;
            }
        }
        return true;
    }
}
