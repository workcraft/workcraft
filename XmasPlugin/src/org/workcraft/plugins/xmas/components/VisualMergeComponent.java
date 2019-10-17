package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;

import java.awt.*;
import java.awt.geom.Path2D;

@DisplayName("Merge")
@SVGIcon("images/xmas-node-merge.svg")
public class VisualMergeComponent extends VisualXmasComponent {

    public VisualMergeComponent(MergeComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.addInput(Positioning.TOP_LEFT);
            this.addInput(Positioning.BOTTOM_LEFT);
            this.addOutput(Positioning.RIGHT);
        }
    }

    @Override
    public MergeComponent getReferencedComponent() {
        return (MergeComponent) super.getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.50 * SIZE, -0.50 * SIZE);
        shape.lineTo(-0.08, -0.50 * SIZE);

        shape.moveTo(-0.50 * SIZE, +0.50 * SIZE);
        shape.lineTo(-0.08, +0.50 * SIZE);

        shape.moveTo(+0.00, -0.60 * SIZE);
        shape.lineTo(+0.00, +0.60 * SIZE);

        shape.moveTo(+0.00, +0.00);
        shape.lineTo(+0.50 * SIZE, +0.00);

        // Arrows
        shape.moveTo(-0.15 * SIZE, -0.55 * SIZE);
        shape.lineTo(-0.05 * SIZE, -0.50 * SIZE);
        shape.lineTo(-0.15 * SIZE, -0.45 * SIZE);
        shape.closePath();

        shape.moveTo(-0.15 * SIZE, +0.55 * SIZE);
        shape.lineTo(-0.05 * SIZE, +0.50 * SIZE);
        shape.lineTo(-0.15 * SIZE, +0.45 * SIZE);
        shape.closePath();

        return shape;
    }

}
