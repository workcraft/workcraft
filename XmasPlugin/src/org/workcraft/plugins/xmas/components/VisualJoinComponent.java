package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;

import java.awt.*;
import java.awt.geom.Path2D;

@DisplayName("Join")
@SVGIcon("images/xmas-node-join.svg")
public class VisualJoinComponent extends VisualXmasComponent {

    public VisualJoinComponent(JoinComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.addInput(Positioning.TOP_LEFT);
            this.addInput(Positioning.BOTTOM_LEFT);
            this.addOutput(Positioning.RIGHT);
        }
    }

    public JoinComponent getReferencedJoinComponent() {
        return (JoinComponent) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.50 * SIZE, -0.50 * SIZE);
        shape.lineTo(-0.18 * SIZE, -0.50 * SIZE);

        shape.moveTo(-0.50 * SIZE, +0.50 * SIZE);
        shape.lineTo(-0.18 * SIZE, +0.50 * SIZE);

        shape.moveTo(-0.10 * SIZE, -0.60 * SIZE);
        shape.lineTo(-0.10 * SIZE, +0.60 * SIZE);

        shape.moveTo(+0.10 * SIZE, -0.60 * SIZE);
        shape.lineTo(+0.10 * SIZE, +0.60 * SIZE);

        shape.moveTo(+0.10 * SIZE, +0.00);
        shape.lineTo(+0.50 * SIZE, +0.00);

        // Arrows
        shape.moveTo(-0.25 * SIZE, -0.55 * SIZE);
        shape.lineTo(-0.15 * SIZE, -0.50 * SIZE);
        shape.lineTo(-0.25 * SIZE, -0.45 * SIZE);
        shape.closePath();

        shape.moveTo(-0.25 * SIZE, +0.55 * SIZE);
        shape.lineTo(-0.15 * SIZE, +0.50 * SIZE);
        shape.lineTo(-0.25 * SIZE, +0.45 * SIZE);
        shape.closePath();

        return shape;
    }

}
