package org.workcraft.plugins.dfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.dfs.decorations.CounterflowLogicDecoration;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

@Hotkey(KeyEvent.VK_K)
@DisplayName ("Counterflow logic")
@SVGIcon("images/dfs-node-counterflow_logic.svg")
public class VisualCounterflowLogic extends VisualDelayComponent {

    public VisualCounterflowLogic(CounterflowLogic logic) {
        super(logic);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class,
                CounterflowLogic.PROPERTY_FORWARD_COMPUTED,
                value -> getReferencedComponent().setForwardComputed(value),
                () -> getReferencedComponent().isForwardComputed())
                .setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class,
                CounterflowLogic.PROPERTY_BACKWARD_COMPUTED,
                value -> getReferencedComponent().setBackwardComputed(value),
                () -> getReferencedComponent().isBackwardComputed())
                .setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class,
                CounterflowLogic.PROPERTY_FORWARD_EARLY_EVALUATION,
                value -> getReferencedComponent().setForwardEarlyEvaluation(value),
                () -> getReferencedComponent().isForwardEarlyEvaluation())
                .setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class,
                CounterflowLogic.PROPERTY_BACKWARD_EARLY_EVALUATION,
                value -> getReferencedComponent().setBackwardEarlyEvaluation(value),
                () -> getReferencedComponent().isBackwardEarlyEvaluation())
                .setCombinable().setTemplatable());
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
        float strokeWidth2 = strokeWidth1 / 2;
        float strokeWidth4 = strokeWidth1 / 4;
        int kd = 6;
        double dd = (size - strokeWidth1 - strokeWidth1) / (4 * kd);

        Path2D forwardShape = new Path2D.Double();
        forwardShape.moveTo(-w2, -strokeWidth4);
        forwardShape.lineTo(-w2, -h2);
        forwardShape.lineTo(w2, -h2);
        forwardShape.lineTo(w2, -strokeWidth4);

        Path2D forwardEarlyShape = new Path2D.Double();
        forwardEarlyShape.moveTo(-2 * dd + dd, (-kd - 2) * dd);
        forwardEarlyShape.lineTo(-2 * dd - dd, (-kd - 2) * dd);
        forwardEarlyShape.lineTo(-2 * dd - dd, (-kd + 2) * dd);
        forwardEarlyShape.lineTo(-2 * dd + dd, (-kd + 2) * dd);
        forwardEarlyShape.moveTo(-2 * dd + dd, (-kd + 0) * dd);
        forwardEarlyShape.lineTo(-2 * dd - dd, (-kd + 0) * dd);
        forwardEarlyShape.moveTo(+2 * dd + dd, (-kd - 2) * dd);
        forwardEarlyShape.lineTo(+2 * dd - dd, (-kd - 2) * dd);
        forwardEarlyShape.lineTo(+2 * dd - dd, (-kd + 2) * dd);
        forwardEarlyShape.lineTo(+2 * dd + dd, (-kd + 2) * dd);
        forwardEarlyShape.moveTo(+2 * dd + dd, (-kd + 0) * dd);
        forwardEarlyShape.lineTo(+2 * dd - dd, (-kd + 0) * dd);

        Path2D backwardShape = new Path2D.Double();
        backwardShape.moveTo(w2, strokeWidth4);
        backwardShape.lineTo(w2, h2);
        backwardShape.lineTo(-w2, h2);
        backwardShape.lineTo(-w2, strokeWidth4);

        Path2D backwardEarlyShape = new Path2D.Double();
        backwardEarlyShape.moveTo(-2 * dd + dd, (+kd - 2) * dd);
        backwardEarlyShape.lineTo(-2 * dd - dd, (+kd - 2) * dd);
        backwardEarlyShape.lineTo(-2 * dd - dd, (+kd + 2) * dd);
        backwardEarlyShape.lineTo(-2 * dd + dd, (+kd + 2) * dd);
        backwardEarlyShape.moveTo(-2 * dd + dd, (+kd + 0) * dd);
        backwardEarlyShape.lineTo(-2 * dd - dd, (+kd + 0) * dd);
        backwardEarlyShape.moveTo(+2 * dd + dd, (+kd - 2) * dd);
        backwardEarlyShape.lineTo(+2 * dd - dd, (+kd - 2) * dd);
        backwardEarlyShape.lineTo(+2 * dd - dd, (+kd + 2) * dd);
        backwardEarlyShape.lineTo(+2 * dd + dd, (+kd + 2) * dd);
        backwardEarlyShape.moveTo(+2 * dd + dd, (+kd + 0) * dd);
        backwardEarlyShape.lineTo(+2 * dd - dd, (+kd + 0) * dd);

        Shape separatorShape = new Line2D.Double(-w2, 0, w2, 0);

        Color defaultColor = ColorUtils.colorise(getForegroundColor(), d.getColorisation());
        boolean forwardComputed = getReferencedComponent().isForwardComputed();
        boolean forwardComputedExcited = false;
        boolean backwardComputed = getReferencedComponent().isBackwardComputed();
        boolean backwardComputedExcited = false;
        if (d instanceof CounterflowLogicDecoration) {
            defaultColor = getForegroundColor();
            forwardComputed = ((CounterflowLogicDecoration) d).isForwardComputed();
            forwardComputedExcited = ((CounterflowLogicDecoration) d).isForwardComputedExcited();
            backwardComputed = ((CounterflowLogicDecoration) d).isBackwardComputed();
            backwardComputedExcited = ((CounterflowLogicDecoration) d).isBackwardComputedExcited();
        }

        if (forwardComputed) {
            g.setColor(ColorUtils.colorise(DfsSettings.getComputedLogicColor(), d.getBackground()));
        } else {
            g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        }
        g.fill(forwardShape);

        if (backwardComputed) {
            g.setColor(ColorUtils.colorise(DfsSettings.getComputedLogicColor(), d.getBackground()));
        } else {
            g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        }
        g.fill(backwardShape);

        g.setColor(defaultColor);
        if (!forwardComputedExcited) {
            g.setStroke(new BasicStroke(strokeWidth1));
            g.draw(forwardShape);
            if (getReferencedComponent().isForwardEarlyEvaluation()) {
                g.setStroke(new BasicStroke(strokeWidth4));
                g.draw(forwardEarlyShape);
            }
        }
        if (!backwardComputedExcited) {
            g.setStroke(new BasicStroke(strokeWidth1));
            g.draw(backwardShape);
            if (getReferencedComponent().isBackwardEarlyEvaluation()) {
                g.setStroke(new BasicStroke(strokeWidth4));
                g.draw(backwardEarlyShape);
            }
        }

        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        if (forwardComputedExcited) {
            g.setStroke(new BasicStroke(strokeWidth1));
            g.draw(forwardShape);
            if (getReferencedComponent().isForwardEarlyEvaluation()) {
                g.setStroke(new BasicStroke(strokeWidth4));
                g.draw(forwardEarlyShape);
            }
        }
        if (backwardComputedExcited) {
            g.setStroke(new BasicStroke(strokeWidth1));
            g.draw(backwardShape);
            if (getReferencedComponent().isBackwardEarlyEvaluation()) {
                g.setStroke(new BasicStroke(strokeWidth4));
                g.draw(backwardEarlyShape);
            }
        }

        if (forwardComputedExcited || backwardComputedExcited) {
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.setStroke(new BasicStroke(strokeWidth2));
        g.draw(separatorShape);

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public CounterflowLogic getReferencedComponent() {
        return (CounterflowLogic) super.getReferencedComponent();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualCounterflowLogic) {
            CounterflowLogic srcLogic = ((VisualCounterflowLogic) src).getReferencedComponent();
            getReferencedComponent().setForwardComputed(srcLogic.isForwardComputed());
            getReferencedComponent().setBackwardComputed(srcLogic.isBackwardComputed());
            getReferencedComponent().setForwardEarlyEvaluation(srcLogic.isForwardEarlyEvaluation());
            getReferencedComponent().setBackwardEarlyEvaluation(srcLogic.isBackwardEarlyEvaluation());
        }
    }

}
