package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.serialisation.NoAutoSerialisation;

import java.awt.*;
import java.awt.event.KeyEvent;

@Hotkey(KeyEvent.VK_D)
@DisplayName("Dummy Transition")
@SVGIcon("images/stg-node-dummy_transition.svg")
public class VisualDummyTransition extends VisualNamedTransition {

    public VisualDummyTransition(DummyTransition dummyTransition) {
        super(dummyTransition);
    }

    @NoAutoSerialisation
    @Override
    public DummyTransition getReferencedComponent() {
        return (DummyTransition) super.getReferencedComponent();
    }

    @Override
    public Color getNameColor() {
        return SignalCommonSettings.getDummyColor();
    }

}
