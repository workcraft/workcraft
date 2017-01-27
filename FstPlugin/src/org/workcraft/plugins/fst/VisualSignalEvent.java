package org.workcraft.plugins.fst;

import java.awt.Color;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.SignalEvent.Direction;
import org.workcraft.plugins.shared.CommonSignalSettings;

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
            case INPUT:    return CommonSignalSettings.getInputColor();
            case OUTPUT:   return CommonSignalSettings.getOutputColor();
            case INTERNAL: return CommonSignalSettings.getInternalColor();
            default:       return CommonSignalSettings.getDummyColor();
            }
        }
        return Color.BLACK;
    }

    public SignalEvent getReferencedSignalEvent() {
        return (SignalEvent) getReferencedEvent();
    }

    private Signal getReferencedSignal() {
        return getReferencedSignalEvent().getSignal();
    }

    @Override
    public String getLabel(DrawRequest r) {
        String result = super.getLabel(r);
        if (getReferencedSignal().hasDirection()) {
            if (CommonSignalSettings.getShowToggle() || (getReferencedSignalEvent().getDirection() != Direction.TOGGLE)) {
                result += getReferencedSignalEvent().getDirection();
            }
        }
        return result;
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualSignalEvent) {
            VisualSignalEvent srcSignalEvent = (VisualSignalEvent) src;
            getReferencedSignalEvent().setDirection(srcSignalEvent.getReferencedSignalEvent().getDirection());
        }
    }

}
