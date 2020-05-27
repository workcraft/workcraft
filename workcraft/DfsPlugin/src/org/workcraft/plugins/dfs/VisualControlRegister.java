package org.workcraft.plugins.dfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.dfs.ControlRegister.SynchronisationType;
import org.workcraft.plugins.dfs.decorations.BinaryRegisterDecoration;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

@Hotkey(KeyEvent.VK_T)
@DisplayName ("Control register")
@SVGIcon("images/dfs-node-control_register.svg")
public class VisualControlRegister extends VisualBinaryRegister {

    public VisualControlRegister(ControlRegister register) {
        super(register);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Double.class, ControlRegister.PROPERTY_PROBABILITY,
                value -> getReferencedComponent().setProbability(value),
                () -> getReferencedComponent().getProbability())
                .setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(SynchronisationType.class, ControlRegister.PROPERTY_SYNCHRONISATION_TYPE,
                value -> getReferencedComponent().setSynchronisationType(value),
                () -> getReferencedComponent().getSynchronisationType())
                .setCombinable().setTemplatable());
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        double w = size - strokeWidth;
        double h = size - strokeWidth;
        double w2 = w / 2;
        double w4 = w / 4;
        double h2 = h / 2;
        double dx = size / 5;
        double dy = strokeWidth / 4;
        float strokeWidth1 = (float) strokeWidth;
        float strokeWidth2 = strokeWidth1 / 2;
        float strokeWidth4 = strokeWidth1 / 4;
        int kd = 6;
        double dd = (size - strokeWidth1 - strokeWidth1) / (4 * kd);
        double tr = (size - strokeWidth1 - strokeWidth1) / 6;

        Path2D shape = new Path2D.Double();
        shape.moveTo(-w2, 0);
        shape.lineTo(-w2 + dx - strokeWidth2, -h2);
        shape.lineTo(+w2 - dx + strokeWidth2, -h2);
        shape.lineTo(+w2, 0);
        shape.lineTo(+w2 - dx + strokeWidth2, +h2);
        shape.lineTo(-w2 + dx - strokeWidth2, +h2);
        shape.closePath();

        Path2D trueInnerShape = new Path2D.Double();
        trueInnerShape.moveTo(-w2 + dx, -dy);
        trueInnerShape.lineTo(-w2 + dx, -h2);
        trueInnerShape.lineTo(w2 - dx, -h2);
        trueInnerShape.lineTo(w2 - dx, -dy);

        Path2D falseInnerShape = new Path2D.Double();
        falseInnerShape.moveTo(w2 - dx, dy);
        falseInnerShape.lineTo(w2 - dx, h2);
        falseInnerShape.lineTo(-w2 + dx, h2);
        falseInnerShape.lineTo(-w2 + dx, dy);

        Path2D trueMarkerShape = new Path2D.Double();
        trueMarkerShape.moveTo(-dd, (-kd - 2) * dd);
        trueMarkerShape.lineTo(+dd, (-kd - 2) * dd);
        trueMarkerShape.moveTo(0, (-kd - 2) * dd);
        trueMarkerShape.lineTo(0, (-kd + 2) * dd);

        Path2D falseMarkerShape = new Path2D.Double();
        falseMarkerShape.moveTo(+dd, (+kd - 2) * dd);
        falseMarkerShape.lineTo(-dd, (+kd - 2) * dd);
        falseMarkerShape.lineTo(-dd, (+kd + 2) * dd);
        falseMarkerShape.moveTo(+dd, (+kd + 0) * dd);
        falseMarkerShape.lineTo(-dd, (+kd + 0) * dd);

        Shape trueTokenShape = new Ellipse2D.Double(-tr, -w4 - tr + strokeWidth4, 2 * tr, 2 * tr);
        Shape falseTokenShape = new Ellipse2D.Double(-tr, +w4 - tr - strokeWidth4, 2 * tr, 2 * tr);
        Shape separatorShape = new Line2D.Double(-w2 + dx, 0, w2 - dx, 0);

        Color defaultColor = ColorUtils.colorise(getForegroundColor(), d.getColorisation());
        Color tokenColor = ColorUtils.colorise(getTokenColor(), d.getColorisation());
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

        g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);

        g.setColor(ColorUtils.colorise(DfsSettings.getSynchronisationRegisterColor(), d.getBackground()));
        if (getReferencedComponent().getSynchronisationType() == SynchronisationType.AND) {
            g.fill(falseInnerShape);
        }
        if (getReferencedComponent().getSynchronisationType() == SynchronisationType.OR) {
            g.fill(trueInnerShape);
        }

        g.setColor(defaultColor);
        if (!trueExcited) {
            g.setStroke(new BasicStroke(strokeWidth2));
            g.draw(trueInnerShape);
            g.setStroke(new BasicStroke(strokeWidth4));
            g.draw(trueMarkerShape);
        }
        if (!falseExcited) {
            g.setStroke(new BasicStroke(strokeWidth2));
            g.draw(falseInnerShape);
            g.setStroke(new BasicStroke(strokeWidth4));
            g.draw(falseMarkerShape);
        }

        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        if (trueExcited) {
            g.setStroke(new BasicStroke(strokeWidth2));
            g.draw(trueInnerShape);
            g.setStroke(new BasicStroke(strokeWidth4));
            g.draw(trueMarkerShape);
        }
        if (falseExcited) {
            g.setStroke(new BasicStroke(strokeWidth2));
            g.draw(falseInnerShape);
            g.setStroke(new BasicStroke(strokeWidth4));
            g.draw(falseMarkerShape);
        }

        if (trueExcited || falseExcited) {
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.setStroke(new BasicStroke(strokeWidth2));
        g.draw(separatorShape);

        g.setStroke(new BasicStroke(strokeWidth1));
        g.draw(shape);

        g.setColor(tokenColor);
        g.setStroke(new BasicStroke(strokeWidth2));
        if (trueMarked) {
            g.draw(trueTokenShape);
        }
        if (falseMarked) {
            g.draw(falseTokenShape);
        }

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        double w2 = size / 2;
        double h2 = size / 2;
        double dx = size / 5 - strokeWidth / 2;

        Path2D shape = new Path2D.Double();
        shape.moveTo(-w2, 0);
        shape.lineTo(-w2 + dx, -h2);
        shape.lineTo(+w2 - dx, -h2);
        shape.lineTo(+w2, 0);
        shape.lineTo(+w2 - dx, +h2);
        shape.lineTo(-w2 + dx, +h2);
        shape.closePath();

        return shape.contains(pointInLocalSpace);
    }

    @Override
    public ControlRegister getReferencedComponent() {
        return (ControlRegister) super.getReferencedComponent();
    }

}
