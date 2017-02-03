package org.workcraft.plugins.wtg;

import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.VisualPage;

@Hotkey(KeyEvent.VK_W)
@DisplayName("Waveform")
@SVGIcon("images/wtg-node-waveform.svg")
public class VisualWaveform extends VisualPage {

    public VisualWaveform(Waveform waveform) {
        super(waveform);
    }

}
