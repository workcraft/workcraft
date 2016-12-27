package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonSignalSettings;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_D)
@DisplayName("Dummy Transition")
@SVGIcon("images/stg-node-dummy_transition.svg")
public class VisualDummyTransition extends VisualNamedTransition implements StateObserver {

    public VisualDummyTransition(DummyTransition dummyTransition) {
        super(dummyTransition);
    }

    @NoAutoSerialisation
    public DummyTransition getReferencedTransition() {
        return (DummyTransition) getReferencedComponent();
    }

    @Override
    public Color getColor() {
        return CommonSignalSettings.getDummyColor();
    }

}
