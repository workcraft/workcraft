package org.workcraft.plugins.policy;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

@Hotkey(KeyEvent.VK_T)
@DisplayName ("Transition")
@SVGIcon("images/policy-node-transition.svg")
public class VisualBundledTransition extends VisualTransition {

    public VisualBundledTransition(BundledTransition transition) {
        super(transition);
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
    }

    public BundledTransition getReferencedTransition() {
        return (BundledTransition) getReferencedComponent();
    }

    @Override
    public Color getFillColor() {
        return CommonVisualSettings.getFillColor();
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        VisualPolicyNet model = (VisualPolicyNet) r.getModel();

        double size = CommonVisualSettings.getNodeSize();
        double strokeWidth = CommonVisualSettings.getStrokeWidth();
        double w = size - strokeWidth;
        double h = size - strokeWidth;
        double w2 = w / 2;
        double h2 = h / 2;
        Shape shape = new Rectangle2D.Double(-w2, -h2, w, h);

        Collection<VisualBundle> bundles = model.getBundlesOfTransition(this);
        if (bundles.size() > 0) {
            h = (h - strokeWidth) / bundles.size();
            h2 = h / 2;
        }

        if (bundles.isEmpty()) {
            g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
            g.fill(shape);
        } else {
            double y = -size / 2 + strokeWidth + h2;
            for (VisualBundle b: bundles) {
                Shape bundleShape = new Rectangle2D.Double(-w2, y - h2, w, h);
                g.setColor(Coloriser.colorise(b.getColor(), d.getBackground()));
                g.fill(bundleShape);
                y += h;
            }
        }
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        g.setStroke(new BasicStroke((float) strokeWidth));
        g.draw(shape);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

}
