package org.workcraft.plugins.dfs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.dfs.decorations.BinaryRegisterDecoration;

@Hotkey(KeyEvent.VK_U)
@DisplayName ("Push register")
@SVGIcon("images/dfs-node-push_register.svg")
public class VisualPushRegister extends VisualBinaryRegister {

    public VisualPushRegister(PushRegister register) {
        super(register);
    }

    @Override
    public PushRegister getReferencedComponent() {
        return (PushRegister) super.getReferencedComponent();
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        double w = size - strokeWidth;
        double h = size - strokeWidth;
        double w2 = w / 2;
        double h2 = h / 2;
        double dx = size / 5;
        double dy = strokeWidth / 2;
        double dt = (size - strokeWidth) / 8;
        float strokeWidth1 = (float) strokeWidth;
        float strokeWidth2 = strokeWidth1 / 2;
        float eps = 0.001f;

        Shape shape = new Rectangle2D.Double(-w2, -h2, w, h);

        Path2D trueInnerShape = new Path2D.Double();
        trueInnerShape.moveTo(-w2 + dx, +h2 - dy);
        trueInnerShape.lineTo(-w2 + dx, -h2 + dy);
        trueInnerShape.moveTo(+w2 - dx, +h2 - dy);
        trueInnerShape.lineTo(+w2 - dx, -h2 + dy);

        Path2D falseInnerShape = new Path2D.Double();
        falseInnerShape.moveTo(+w2 - dx, +h2  - 2 * dt - eps);
        falseInnerShape.lineTo(+w2 - dx, +h2 - 2 * dt);
        falseInnerShape.lineTo(0, +h2 - dy);
        falseInnerShape.lineTo(-w2 + dx, +h2 - 2 * dt);
        falseInnerShape.lineTo(-w2 + dx, +h2 - 2 * dt - eps);

        Shape tokenShape = new Ellipse2D.Double(-dt, -dt, 2 * dt, 2 * dt);

        Color defaultColor = Coloriser.colorise(getForegroundColor(), d.getColorisation());
        Color tokenColor = Coloriser.colorise(getTokenColor(), d.getColorisation());
        boolean trueMarked = getReferencedComponent().isTrueMarked();
        boolean trueExcited = false;
        boolean falseMarked = getReferencedComponent().isFalseMarked();
        boolean falseExcited = false;
        if (d instanceof BinaryRegisterDecoration) {
            defaultColor = getForegroundColor();
            tokenColor = ((BinaryRegisterDecoration) d).getTokenColor();
            trueMarked = ((BinaryRegisterDecoration) d).isTrueMarked();
            trueExcited = ((BinaryRegisterDecoration) d).isTrueExcited();
            falseMarked = ((BinaryRegisterDecoration) d).isFalseMarked();
            falseExcited = ((BinaryRegisterDecoration) d).isFalseExcited();
        }

        g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);

        g.setStroke(new BasicStroke(strokeWidth2));
        if (falseExcited) {
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.draw(falseInnerShape);
        if (trueExcited) {
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.draw(trueInnerShape);

        if (trueExcited || falseExcited) {
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.setStroke(new BasicStroke(strokeWidth1));
        g.draw(shape);

        g.setColor(tokenColor);
        g.setStroke(new BasicStroke(strokeWidth2));
        if (trueMarked) {
            g.fill(tokenShape);
        }
        if (falseMarked) {
            g.draw(tokenShape);
        }

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

}
