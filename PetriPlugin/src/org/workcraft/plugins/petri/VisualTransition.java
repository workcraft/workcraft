package org.workcraft.plugins.petri;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.shared.ColorGenerator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

@Hotkey(KeyEvent.VK_T)
@DisplayName ("Transition")
@SVGIcon("images/petri-node-transition.svg")
public class VisualTransition extends VisualComponent {
    private ColorGenerator tokenColorGenerator = null;

    public VisualTransition(Transition transition) {
        this(transition, true, true, true);
    }

    public VisualTransition(Transition transition, boolean hasColorProperties, boolean hasLabelProperties, boolean hasNameProperties) {
        super(transition, hasColorProperties, hasLabelProperties, hasNameProperties);
    }

    public Transition getReferencedTransition() {
        return (Transition) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        double size = CommonVisualSettings.getNodeSize() - CommonVisualSettings.getStrokeWidth();
        double pos = -0.5 * size;
        return new Rectangle2D.Double(pos, pos, size, size);
    }

    public ColorGenerator getTokenColorGenerator() {
        return this.tokenColorGenerator;
    }

    public void setTokenColorGenerator(ColorGenerator value) {
        this.tokenColorGenerator = value;
    }

}
