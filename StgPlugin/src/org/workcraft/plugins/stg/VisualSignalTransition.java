package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.serialisation.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Signal Transition")
@SVGIcon("images/stg-node-signal_transition.svg")
public class VisualSignalTransition extends VisualNamedTransition implements StateObserver {

    public VisualSignalTransition(SignalTransition signalTransition) {
        super(signalTransition);
    }

    @Override
    public String getName() {
        String signalName = getReferencedComponent().getSignalName();
        if (signalName == null) {
            signalName = "";
        }
        final StringBuffer result = new StringBuffer(signalName);
        switch (getReferencedComponent().getDirection()) {
        case PLUS:
            result.append("+");
            break;
        case MINUS:
            result.append("-");
            break;
        case TOGGLE:
            if (SignalCommonSettings.getShowToggle()) {
                result.append("~");
            }
            break;
        }
        return result.toString();
    }

    @Override
    public Color getNameColor() {
        switch (getSignalType()) {
        case INPUT:    return SignalCommonSettings.getInputColor();
        case OUTPUT:   return SignalCommonSettings.getOutputColor();
        case INTERNAL: return SignalCommonSettings.getInternalColor();
        default:       return SignalCommonSettings.getDummyColor();
        }
    }

    @NoAutoSerialisation
    @Override
    public SignalTransition getReferencedComponent() {
        return (SignalTransition) super.getReferencedComponent();
    }

    @NoAutoSerialisation
    public Signal.Type getSignalType() {
        return getReferencedComponent().getSignalType();
    }

    @NoAutoSerialisation
    public String getSignalName() {
        return getReferencedComponent().getSignalName();
    }

    @NoAutoSerialisation
    public SignalTransition.Direction getDirection() {
        return getReferencedComponent().getDirection();
    }

// FIXME: type, direction and name of the signal cannot be copied as it breaks template functionality
//    @Override
//    public void copyStyle(Stylable src) {
//        super.copyStyle(src);
//        if (src instanceof VisualSignalTransition) {
//            VisualSignalTransition srcSignalTransition = (VisualSignalTransition) src;
//            setType(srcSignalTransition.getType());
//            setDirection(srcSignalTransition.getDirection());
//        }
//    }

}
