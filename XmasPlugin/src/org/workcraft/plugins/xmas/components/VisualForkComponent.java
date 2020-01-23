package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;

import java.awt.*;
import java.awt.geom.Path2D;

@DisplayName("Fork")
@SVGIcon("images/xmas-node-fork.svg")
public class VisualForkComponent extends VisualXmasComponent {

    public VisualForkComponent(ForkComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.createInput(Positioning.LEFT);
            this.createOutput(Positioning.TOP_RIGHT);
            this.createOutput(Positioning.BOTTOM_RIGHT);
        }
    }

    @Override
    public ForkComponent getReferencedComponent() {
        return (ForkComponent) super.getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.50 * SIZE, -0.00);
        shape.lineTo(-0.18 * SIZE, -0.00);

        shape.moveTo(-0.10 * SIZE, -0.60 * SIZE);
        shape.lineTo(-0.10 * SIZE, +0.60 * SIZE);

        shape.moveTo(+0.10 * SIZE, -0.60 * SIZE);
        shape.lineTo(+0.10 * SIZE, +0.60 * SIZE);

        shape.moveTo(+0.10 * SIZE, -0.50 * SIZE);
        shape.lineTo(+0.50 * SIZE, -0.50 * SIZE);

        shape.moveTo(+0.10 * SIZE, +0.50 * SIZE);
        shape.lineTo(+0.50 * SIZE, +0.50 * SIZE);

        // Arrows
        shape.moveTo(-0.25 * SIZE, -0.05 * SIZE);
        shape.lineTo(-0.15 * SIZE, +0.00);
        shape.lineTo(-0.25 * SIZE, +0.05 * SIZE);
        shape.closePath();

        return shape;
    }

}
