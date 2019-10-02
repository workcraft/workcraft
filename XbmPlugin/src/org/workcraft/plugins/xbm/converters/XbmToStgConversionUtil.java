package org.workcraft.plugins.xbm.converters;

import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.xbm.Burst;
import org.workcraft.plugins.xbm.XbmSignal;

public class XbmToStgConversionUtil {
    public static Signal.Type getReferredType(XbmSignal.Type burstType) {
        switch (burstType) {
        case INPUT:
            return Signal.Type.INPUT;
        case OUTPUT:
            return Signal.Type.OUTPUT;
        case CONDITIONAL:
            return Signal.Type.INTERNAL;
        default:
            return null;
        }
    }

    public static SignalTransition.Direction getReferredDirection(Burst.Direction burstDirection) {
        switch (burstDirection) {
        case PLUS:
            return SignalTransition.Direction.PLUS;
        case MINUS:
            return SignalTransition.Direction.MINUS;
        default:
            return SignalTransition.Direction.TOGGLE; //TODO Add conversion for XBM's stable and unstable signals
        }
    }
}
