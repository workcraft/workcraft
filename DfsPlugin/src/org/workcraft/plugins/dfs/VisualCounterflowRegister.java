package org.workcraft.plugins.dfs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.dfs.decorations.CounterflowRegisterDecoration;

@Hotkey(KeyEvent.VK_Q)
@DisplayName ("Counterflow register")
@SVGIcon("images/node-counterflow_register.svg")
public class VisualCounterflowRegister extends VisualAbstractRegister {

    public VisualCounterflowRegister(CounterflowRegister register) {
        super(register);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualCounterflowRegister, Boolean>(
                this, CounterflowRegister.PROPERTY_OR_MARKED, Boolean.class, true, true, true) {
            public void setter(VisualCounterflowRegister object, Boolean value) {
                object.getReferencedCounterflowRegister().setOrMarked(value);
            }
            public Boolean getter(VisualCounterflowRegister object) {
                return object.getReferencedCounterflowRegister().isOrMarked();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualCounterflowRegister, Boolean>(
                this, CounterflowRegister.PROPERTY_AND_MARKED, Boolean.class, true, true, true) {
            public void setter(VisualCounterflowRegister object, Boolean value) {
                object.getReferencedCounterflowRegister().setAndMarked(value);
            }
            public Boolean getter(VisualCounterflowRegister object) {
                return object.getReferencedCounterflowRegister().isAndMarked();
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
        double dx = size / 5;
        double v2 = w2 - dx;
        double dt = (size - strokeWidth) / 8;
        float strokeWidth1 = (float) strokeWidth;
        float strokeWidth2 = strokeWidth1 / 2;

        Shape shape = new Rectangle2D.Double(-w2, -h2, w, h);

        Path2D forwardShape = new Path2D.Double();
        forwardShape.moveTo(-v2, -h2);
        forwardShape.lineTo(-v2, 0);
        forwardShape.lineTo(v2, 0);
        forwardShape.lineTo(v2, -h2);

        Path2D backwardShape = new Path2D.Double();
        backwardShape.moveTo(v2, h2);
        backwardShape.lineTo(v2, 0);
        backwardShape.lineTo(-v2, 0);
        backwardShape.lineTo(-v2, h2);

        Shape andTokenShape = new Rectangle2D.Double(-dt, dt, 2 * dt, 2 * dt);
        Path2D orTokenShape = new Path2D.Double();
        orTokenShape.moveTo(0, -3 * dt);
        orTokenShape.lineTo(dt, -dt);
        orTokenShape.lineTo(-dt, -dt);
        orTokenShape.closePath();

        Color defaultColor = Coloriser.colorise(getForegroundColor(), d.getColorisation());
        Color tokenColor = getTokenColor();
        boolean forwardExcited = false;
        boolean backwardExcited = false;
        boolean orMarked = getReferencedCounterflowRegister().isOrMarked();
        boolean orMarkedExcited = false;
        boolean andMarked = getReferencedCounterflowRegister().isAndMarked();
        boolean andMarkedExcited = false;
        if (d instanceof CounterflowRegisterDecoration) {
            defaultColor = getForegroundColor();
            tokenColor = ((CounterflowRegisterDecoration) d).getTokenColor();
            forwardExcited = ((CounterflowRegisterDecoration) d).isForwardExcited();
            backwardExcited = ((CounterflowRegisterDecoration) d).isBackwardExcited();
            orMarked = ((CounterflowRegisterDecoration) d).isOrMarked();
            orMarkedExcited = ((CounterflowRegisterDecoration) d).isOrExcited();
            andMarked = ((CounterflowRegisterDecoration) d).isAndMarked();
            andMarkedExcited = ((CounterflowRegisterDecoration) d).isAndExcited();
        }

        g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);

        g.setStroke(new BasicStroke(strokeWidth2));
        g.setColor(defaultColor);
        if (!forwardExcited) {
            g.draw(forwardShape);
        }
        if (!backwardExcited) {
            g.draw(backwardShape);
        }
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        if (forwardExcited) {
            g.draw(forwardShape);
        }
        if (backwardExcited) {
            g.draw(backwardShape);
        }

        if (forwardExcited || backwardExcited || orMarkedExcited || andMarkedExcited) {
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.setStroke(new BasicStroke(strokeWidth1));
        g.draw(shape);

        if (orMarked) {
            g.setColor(tokenColor);
            g.fill(orTokenShape);
        }

        if (andMarked) {
            g.setColor(tokenColor);
            g.fill(andTokenShape);
        }

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    public CounterflowRegister getReferencedCounterflowRegister() {
        return (CounterflowRegister) getReferencedComponent();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualCounterflowRegister) {
            CounterflowRegister srcRegister = ((VisualCounterflowRegister) src).getReferencedCounterflowRegister();
            getReferencedCounterflowRegister().setOrMarked(srcRegister.isOrMarked());
            getReferencedCounterflowRegister().setAndMarked(srcRegister.isAndMarked());
        }
    }

}
