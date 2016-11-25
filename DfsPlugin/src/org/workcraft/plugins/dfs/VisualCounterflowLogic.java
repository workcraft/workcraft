package org.workcraft.plugins.dfs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.dfs.decorations.CounterflowLogicDecoration;

@Hotkey(KeyEvent.VK_K)
@DisplayName ("Counterflow logic")
@SVGIcon("images/dfs-node-counterflow_logic.svg")
public class VisualCounterflowLogic extends VisualDelayComponent {

    public VisualCounterflowLogic(CounterflowLogic logic) {
        super(logic);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualCounterflowLogic, Boolean>(
                this, CounterflowLogic.PROPERTY_FORWARD_COMPUTED, Boolean.class, true, true, true) {
            public void setter(VisualCounterflowLogic object, Boolean value) {
                object.getReferencedCounterflowLogic().setForwardComputed(value);
            }
            public Boolean getter(VisualCounterflowLogic object) {
                return object.getReferencedCounterflowLogic().isForwardComputed();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualCounterflowLogic, Boolean>(
                this, CounterflowLogic.PROPERTY_BACKWARD_COMPUTED, Boolean.class, true, true, true) {
            public void setter(VisualCounterflowLogic object, Boolean value) {
                object.getReferencedCounterflowLogic().setBackwardComputed(value);
            }
            public Boolean getter(VisualCounterflowLogic object) {
                return object.getReferencedCounterflowLogic().isBackwardComputed();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualCounterflowLogic, Boolean>(
                this, CounterflowLogic.PROPERTY_FORWARD_EARLY_EVALUATION, Boolean.class, true, true, true) {
            public void setter(VisualCounterflowLogic object, Boolean value) {
                object.getReferencedCounterflowLogic().setForwardEarlyEvaluation(value);
            }
            public Boolean getter(VisualCounterflowLogic object) {
                return object.getReferencedCounterflowLogic().isForwardEarlyEvaluation();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualCounterflowLogic, Boolean>(
                this, CounterflowLogic.PROPERTY_BACKWARD_EARLY_EVALUATION, Boolean.class, true, true, true) {
            public void setter(VisualCounterflowLogic object, Boolean value) {
                object.getReferencedCounterflowLogic().setBackwardEarlyEvaluation(value);
            }
            public Boolean getter(VisualCounterflowLogic object) {
                return object.getReferencedCounterflowLogic().isBackwardEarlyEvaluation();
            }
        });
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

        Color defaultColor = Coloriser.colorise(getForegroundColor(), d.getColorisation());
        boolean forwardComputed = getReferencedCounterflowLogic().isForwardComputed();
        boolean forwardComputedExcited = false;
        boolean backwardComputed = getReferencedCounterflowLogic().isBackwardComputed();
        boolean backwardComputedExcited = false;
        if (d instanceof CounterflowLogicDecoration) {
            defaultColor = getForegroundColor();
            forwardComputed = ((CounterflowLogicDecoration) d).isForwardComputed();
            forwardComputedExcited = ((CounterflowLogicDecoration) d).isForwardComputedExcited();
            backwardComputed = ((CounterflowLogicDecoration) d).isBackwardComputed();
            backwardComputedExcited = ((CounterflowLogicDecoration) d).isBackwardComputedExcited();
        }

        if (forwardComputed) {
            g.setColor(Coloriser.colorise(DfsSettings.getComputedLogicColor(), d.getBackground()));
        } else {
            g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        }
        g.fill(forwardShape);

        if (backwardComputed) {
            g.setColor(Coloriser.colorise(DfsSettings.getComputedLogicColor(), d.getBackground()));
        } else {
            g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        }
        g.fill(backwardShape);

        g.setColor(defaultColor);
        if (!forwardComputedExcited) {
            g.setStroke(new BasicStroke(strokeWidth1));
            g.draw(forwardShape);
            if (getReferencedCounterflowLogic().isForwardEarlyEvaluation()) {
                g.setStroke(new BasicStroke(strokeWidth4));
                g.draw(forwardEarlyShape);
            }
        }
        if (!backwardComputedExcited) {
            g.setStroke(new BasicStroke(strokeWidth1));
            g.draw(backwardShape);
            if (getReferencedCounterflowLogic().isBackwardEarlyEvaluation()) {
                g.setStroke(new BasicStroke(strokeWidth4));
                g.draw(backwardEarlyShape);
            }
        }

        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        if (forwardComputedExcited) {
            g.setStroke(new BasicStroke(strokeWidth1));
            g.draw(forwardShape);
            if (getReferencedCounterflowLogic().isForwardEarlyEvaluation()) {
                g.setStroke(new BasicStroke(strokeWidth4));
                g.draw(forwardEarlyShape);
            }
        }
        if (backwardComputedExcited) {
            g.setStroke(new BasicStroke(strokeWidth1));
            g.draw(backwardShape);
            if (getReferencedCounterflowLogic().isBackwardEarlyEvaluation()) {
                g.setStroke(new BasicStroke(strokeWidth4));
                g.draw(backwardEarlyShape);
            }
        }

        if (forwardComputedExcited || backwardComputedExcited) {
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.setStroke(new BasicStroke(strokeWidth2));
        g.draw(separatorShape);

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    public CounterflowLogic getReferencedCounterflowLogic() {
        return (CounterflowLogic) getReferencedComponent();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualCounterflowLogic) {
            CounterflowLogic srcLogic = ((VisualCounterflowLogic) src).getReferencedCounterflowLogic();
            getReferencedCounterflowLogic().setForwardComputed(srcLogic.isForwardComputed());
            getReferencedCounterflowLogic().setBackwardComputed(srcLogic.isBackwardComputed());
            getReferencedCounterflowLogic().setForwardEarlyEvaluation(srcLogic.isForwardEarlyEvaluation());
            getReferencedCounterflowLogic().setBackwardEarlyEvaluation(srcLogic.isBackwardEarlyEvaluation());
        }
    }

}
