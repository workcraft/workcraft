package org.workcraft.plugins.xbm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.plugins.fsm.VisualState;

import java.awt.event.KeyEvent;

@Hotkey(KeyEvent.VK_T)
@DisplayName("State")
@SVGIcon("images/fsm-node-vertex.svg")
public class VisualXbmState extends VisualState {

    public VisualXbmState(XbmState state) {
        super(state);
    }

    @Override
    public XbmState getReferencedState() {
        return (XbmState) super.getReferencedState();
    }

    @Override
    public String getLabel() {
        return getReferencedState().getStateEncoding();
    }

    @Override
    public void setLabel(String value) {
    }
}
