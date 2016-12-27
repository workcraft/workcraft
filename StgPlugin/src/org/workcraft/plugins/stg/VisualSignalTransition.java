package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonSignalSettings;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Signal Transition")
@SVGIcon("images/stg-node-signal_transition.svg")
public class VisualSignalTransition extends VisualNamedTransition implements StateObserver {

    public VisualSignalTransition(SignalTransition signalTransition) {
        super(signalTransition);
    }

    @Override
    public String getName() {
        String signalName = getReferencedTransition().getSignalName();
        if (signalName == null) {
            signalName = "";
        }
        final StringBuffer result = new StringBuffer(signalName);
        switch (getReferencedTransition().getDirection()) {
        case PLUS:
            result.append("+");
            break;
        case MINUS:
            result.append("-");
            break;
        case TOGGLE:
            if (CommonSignalSettings.getShowToggle()) {
                result.append("~");
            }
            break;
        }
        return result.toString();
    }

    @Override
    public Color getColor() {
        switch (getSignalType()) {
        case INPUT:    return CommonSignalSettings.getInputColor();
        case OUTPUT:   return CommonSignalSettings.getOutputColor();
        case INTERNAL: return CommonSignalSettings.getInternalColor();
        default:       return CommonSignalSettings.getDummyColor();
        }
    }

    @NoAutoSerialisation
    public SignalTransition getReferencedTransition() {
        return (SignalTransition) getReferencedComponent();
    }

    @NoAutoSerialisation
    public Type getSignalType() {
        return getReferencedTransition().getSignalType();
    }

    @NoAutoSerialisation
    public String getSignalName() {
        return getReferencedTransition().getSignalName();
    }

    @NoAutoSerialisation
    public Direction getDirection() {
        return getReferencedTransition().getDirection();
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
