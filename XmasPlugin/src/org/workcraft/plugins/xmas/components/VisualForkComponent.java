package org.workcraft.plugins.xmas.components;

import java.awt.Shape;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;

@DisplayName("Fork")
@SVGIcon("images/xmas-node-fork.svg")
public class VisualForkComponent extends VisualXmasComponent {

    public VisualForkComponent(ForkComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.addInput("i", Positioning.LEFT);
            this.addOutput("a", Positioning.TOP_RIGHT);
            this.addOutput("b", Positioning.BOTTOM_RIGHT);
        }
    }

    public ForkComponent getReferencedForkComponent() {
        return (ForkComponent) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.50 * size, -0.00);
        shape.lineTo(-0.18 * size, -0.00);

        shape.moveTo(-0.10 * size, -0.60 * size);
        shape.lineTo(-0.10 * size, +0.60 * size);

        shape.moveTo(+0.10 * size, -0.60 * size);
        shape.lineTo(+0.10 * size, +0.60 * size);

        shape.moveTo(+0.10 * size, -0.50 * size);
        shape.lineTo(+0.50 * size, -0.50 * size);

        shape.moveTo(+0.10 * size, +0.50 * size);
        shape.lineTo(+0.50 * size, +0.50 * size);

        // Arrows
        shape.moveTo(-0.25 * size, -0.05 * size);
        shape.lineTo(-0.15 * size, +0.00);
        shape.lineTo(-0.25 * size, +0.05 * size);
        shape.closePath();

        return shape;
    }

}
