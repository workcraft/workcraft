package org.workcraft.plugins.dfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.dfs.decorations.RegisterDecoration;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

@Hotkey(KeyEvent.VK_R)
@DisplayName ("Register")
@SVGIcon("images/dfs-node-register.svg")
public class VisualRegister extends VisualAbstractRegister {

    public VisualRegister(Register register) {
        super(register);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, Register.PROPERTY_MARKED,
                value -> getReferencedComponent().setMarked(value),
                () -> getReferencedComponent().isMarked())
                .setCombinable().setTemplatable());
    }

    @Override
    public Register getReferencedComponent() {
        return (Register) super.getReferencedComponent();
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
        double dy = strokeWidth / 4;
        double dt = (size - strokeWidth) / 8;
        float strokeWidth1 = (float) strokeWidth;
        float strokeWidth2 = strokeWidth1 / 2;

        Shape shape = new Rectangle2D.Double(-w2, -h2, w, h);
        Shape innerShape = new Rectangle2D.Double(-w2 + dx, -h2 + dy, w - dx - dx, h - dy - dy);
        Shape tokenShape = new Ellipse2D.Double(-dt, -dt, 2 * dt, 2 * dt);

        Color defaultColor = ColorUtils.colorise(getForegroundColor(), d.getColorisation());
        Color tokenColor = ColorUtils.colorise(getTokenColor(), d.getColorisation());
        boolean marked = getReferencedComponent().isMarked();
        boolean excited = false;
        if (d instanceof RegisterDecoration) {
            defaultColor = getForegroundColor();
            tokenColor = ((RegisterDecoration) d).getTokenColor();
            marked = ((RegisterDecoration) d).isMarked();
            excited = ((RegisterDecoration) d).isExcited();
        }

        g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        if (excited) {
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.setStroke(new BasicStroke(strokeWidth2));
        g.draw(innerShape);
        g.setStroke(new BasicStroke(strokeWidth1));
        g.draw(shape);
        if (marked) {
            g.setColor(tokenColor);
            g.fill(tokenShape);
        }

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualRegister) {
            Register srcRegister = ((VisualRegister) src).getReferencedComponent();
            getReferencedComponent().setMarked(srcRegister.isMarked());
        }
    }

}
