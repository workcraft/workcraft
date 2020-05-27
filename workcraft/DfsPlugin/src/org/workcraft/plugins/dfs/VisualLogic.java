package org.workcraft.plugins.dfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.dfs.decorations.LogicDecoration;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

@Hotkey(KeyEvent.VK_L)
@DisplayName ("Logic")
@SVGIcon("images/dfs-node-logic.svg")
public class VisualLogic extends VisualDelayComponent {

    public VisualLogic(Logic logic) {
        super(logic);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, Logic.PROPERTY_COMPUTED,
                value -> getReferencedComponent().setComputed(value),
                () -> getReferencedComponent().isComputed())
                .setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, Logic.PROPERTY_EARLY_EVALUATION,
                value -> getReferencedComponent().setEarlyEvaluation(value),
                () -> getReferencedComponent().isEarlyEvaluation())
                .setCombinable().setTemplatable());
    }

    @Override
    public Logic getReferencedComponent() {
        return (Logic) super.getReferencedComponent();
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        double w = size - strokeWidth;
        double h = size - strokeWidth;
        double w2 = w / 2;
        double h2 = h / 2;
        float strokeWidth1 = (float) strokeWidth;
        float strokeWidth4 = strokeWidth1 / 4;
        int kd = 6;
        double dd = (size - strokeWidth1 - strokeWidth1) / (4 * kd);

        Shape shape = new Rectangle2D.Double(-w2, -h2, w, h);
        Path2D eeShape = new Path2D.Double();
        eeShape.moveTo(-2 * dd + dd, -2 * dd);
        eeShape.lineTo(-2 * dd - dd, -2 * dd);
        eeShape.lineTo(-2 * dd - dd, +2 * dd);
        eeShape.lineTo(-2 * dd + dd, +2 * dd);
        eeShape.moveTo(-2 * dd + dd, 0);
        eeShape.lineTo(-2 * dd - dd, 0);
        eeShape.moveTo(+2 * dd + dd, -2 * dd);
        eeShape.lineTo(+2 * dd - dd, -2 * dd);
        eeShape.lineTo(+2 * dd - dd, +2 * dd);
        eeShape.lineTo(+2 * dd + dd, +2 * dd);
        eeShape.moveTo(+2 * dd + dd, 0);
        eeShape.lineTo(+2 * dd - dd, 0);

        boolean computed = getReferencedComponent().isComputed();
        if (d instanceof LogicDecoration) {
            computed = ((LogicDecoration) d).isComputed();
        }
        if (computed) {
            g.setColor(ColorUtils.colorise(DfsSettings.getComputedLogicColor(), d.getBackground()));
        } else {
            g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        }
        g.fill(shape);
        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        if (getReferencedComponent().isEarlyEvaluation()) {
            g.setStroke(new BasicStroke(strokeWidth4));
            g.draw(eeShape);
        }
        g.setStroke(new BasicStroke(strokeWidth1));
        g.draw(shape);

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualLogic) {
            Logic srcLogic = ((VisualLogic) src).getReferencedComponent();
            getReferencedComponent().setEarlyEvaluation(srcLogic.isEarlyEvaluation());
            getReferencedComponent().setComputed(srcLogic.isComputed());
        }
    }

}
