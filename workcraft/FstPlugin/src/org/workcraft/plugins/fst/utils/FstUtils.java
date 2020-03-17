package org.workcraft.plugins.fst.utils;

import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.fst.Signal;

import java.awt.*;

public class FstUtils {

    public static Color getTypeColor(Signal.Type type) {
        if (type != null) {
            switch (type) {
            case INPUT:    return SignalCommonSettings.getInputColor();
            case OUTPUT:   return SignalCommonSettings.getOutputColor();
            case INTERNAL: return SignalCommonSettings.getInternalColor();
            default:       return SignalCommonSettings.getDummyColor();
            }
        }
        return Color.BLACK;
    }

}
