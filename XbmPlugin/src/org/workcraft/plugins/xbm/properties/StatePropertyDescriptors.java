package org.workcraft.plugins.xbm.properties;

import org.workcraft.gui.properties.ActionListDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.xbm.SignalState;
import org.workcraft.plugins.xbm.XbmSignal;
import org.workcraft.plugins.xbm.XbmState;

import java.util.Map;

public class StatePropertyDescriptors {

    private static final char TOGGLE_SYMBOL = 0x21C5;
    private static final String TOGGLE_TEXT = Character.toString(TOGGLE_SYMBOL);
    private static final char HIGH_SYMBOL = 0x21C8;
    private static final String HIGH_TEXT = Character.toString(HIGH_SYMBOL);
    private static final char LOW_SYMBOL = 0x21CA;
    private static final String LOW_TEXT = Character.toString(LOW_SYMBOL);

    public static PropertyDescriptor burstProperty(XbmState state, String name, XbmSignal.Type type) {
        return new ActionListDeclaration(name)
                .addAction(TOGGLE_TEXT, () -> toggleSignalsByType(state, type))
                .addAction(HIGH_TEXT, () -> setSignalsToValueByType(state, type, SignalState.HIGH))
                .addAction(LOW_TEXT, () -> setSignalsToValueByType(state, type, SignalState.LOW));
    }

    private static void toggleSignalsByType(XbmState state, XbmSignal.Type type) {
        for (Map.Entry<XbmSignal, SignalState> entry: state.getEncoding().entrySet()) {
            if (entry.getKey().getType() == type) {
                state.addOrChangeSignalValue(entry.getKey(), entry.getValue().toggle());
            }
        }
    }

    private static void setSignalsToValueByType(XbmState state, XbmSignal.Type type, SignalState value) {
        for (Map.Entry<XbmSignal, SignalState> entry: state.getEncoding().entrySet()) {
            if ((entry.getKey().getType() == type) && (entry.getValue() != value)) {
                state.addOrChangeSignalValue(entry.getKey(), value);
            }
        }
    }

}