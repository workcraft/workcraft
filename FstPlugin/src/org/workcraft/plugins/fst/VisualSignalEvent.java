package org.workcraft.plugins.fst;

import java.awt.Color;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.SignalEvent.Direction;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;

public class VisualSignalEvent extends VisualEvent {

    public VisualSignalEvent() {
        this(null, null, null);
    }

    public VisualSignalEvent(SignalEvent mathConnection) {
        this(mathConnection, null, null);
    }

    public VisualSignalEvent(SignalEvent mathConnection, VisualState first, VisualState second) {
        super(mathConnection, first, second);
        removePropertyDeclarationByName(PROPERTY_LABEL_COLOR);
    }

    @Override
    public Color getLabelColor() {
        Signal signal = getReferencedSignal();
        if (signal != null) {
            switch (signal.getType()) {
            case INPUT:    return SignalCommonSettings.getInputColor();
            case OUTPUT:   return SignalCommonSettings.getOutputColor();
            case INTERNAL: return SignalCommonSettings.getInternalColor();
            default:       return SignalCommonSettings.getDummyColor();
            }
        }
        return Color.BLACK;
    }

    @Override
    public SignalEvent getReferencedConnection() {
        return (SignalEvent) super.getReferencedConnection();
    }

    private Signal getReferencedSignal() {
        return getReferencedConnection().getSignal();
    }

    @Override
    public String getLabel(DrawRequest r) {
        String result = super.getLabel(r);
        if (getReferencedSignal().hasDirection()) {
            if (SignalCommonSettings.getShowToggle() || (getReferencedConnection().getDirection() != Direction.TOGGLE)) {
                result += getReferencedConnection().getDirection();
            }
        }
        return result;
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualSignalEvent) {
            VisualSignalEvent srcSignalEvent = (VisualSignalEvent) src;
            getReferencedConnection().setDirection(srcSignalEvent.getReferencedConnection().getDirection());
        }
    }

}
