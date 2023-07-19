package org.workcraft.plugins.fst;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.SignalEvent.Direction;

import java.awt.*;

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
        Signal signal = getReferencedConnection().getSymbol();
        return signal != null ? signal.getType().getColor() : super.getLabelColor();
    }

    @Override
    public SignalEvent getReferencedConnection() {
        return (SignalEvent) super.getReferencedConnection();
    }

    @Override
    public String getLabel(DrawRequest r) {
        String result = super.getLabel(r);
        Signal signal = getReferencedConnection().getSymbol();
        if ((signal != null) && signal.hasDirection()) {
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
