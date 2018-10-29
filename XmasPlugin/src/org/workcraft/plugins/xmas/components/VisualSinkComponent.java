package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.plugins.xmas.XmasSettings;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

@DisplayName("Sink")
@Hotkey(KeyEvent.VK_O)
@SVGIcon("images/xmas-node-sink.svg")
public class VisualSinkComponent extends VisualXmasComponent {

    public VisualSinkComponent(SinkComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.addInput(Positioning.CENTER);
        }
    }

    public SinkComponent getReferencedSinkComponent() {
        return (SinkComponent) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(0.00, +0.00);
        shape.lineTo(0.00, +0.40 * SIZE);

        shape.moveTo(-0.35 * SIZE, 0.40 * SIZE);
        shape.lineTo(+0.35 * SIZE, 0.40 * SIZE);

        shape.moveTo(-0.20 * SIZE, +0.55 * SIZE);
        shape.lineTo(+0.20 * SIZE, +0.55 * SIZE);

        shape.moveTo(-0.05 * SIZE, +0.70 * SIZE);
        shape.lineTo(+0.05 * SIZE, +0.70 * SIZE);

        return shape;
    }

    public Shape getTokenShape() {
        return new Ellipse2D.Double(+1.4 * TOKEN_SIZE, +0.6 * TOKEN_SIZE, TOKEN_SIZE, TOKEN_SIZE);
    }

    @Override
    public void draw(DrawRequest r) {
        super.draw(r);
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        if (d instanceof StateDecoration) {
            if (((StateDecoration) d).getState()) {
                g.setStroke(new BasicStroke((float) XmasSettings.getBorderWidth()));
                g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
                Shape shape = transformShape(getTokenShape());
                g.draw(shape);
            }
        }
    }

}
