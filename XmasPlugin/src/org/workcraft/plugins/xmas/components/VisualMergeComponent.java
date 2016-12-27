package org.workcraft.plugins.xmas.components;

import java.awt.Shape;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;

@DisplayName("Merge")
@SVGIcon("images/xmas-node-merge.svg")
public class VisualMergeComponent extends VisualXmasComponent {

    public VisualMergeComponent(MergeComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.addInput("a", Positioning.TOP_LEFT);
            this.addInput("b", Positioning.BOTTOM_LEFT);
            this.addOutput("o", Positioning.RIGHT);
        }
    }

    public MergeComponent getReferencedMergeComponent() {
        return (MergeComponent) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.50 * size, -0.50 * size);
        shape.lineTo(-0.08, -0.50 * size);

        shape.moveTo(-0.50 * size, +0.50 * size);
        shape.lineTo(-0.08, +0.50 * size);

        shape.moveTo(+0.00, -0.60 * size);
        shape.lineTo(+0.00, +0.60 * size);

        shape.moveTo(+0.00, +0.00);
        shape.lineTo(+0.50 * size, +0.00);

        // Arrows
        shape.moveTo(-0.15 * size, -0.55 * size);
        shape.lineTo(-0.05 * size, -0.50 * size);
        shape.lineTo(-0.15 * size, -0.45 * size);
        shape.closePath();

        shape.moveTo(-0.15 * size, +0.55 * size);
        shape.lineTo(-0.05 * size, +0.50 * size);
        shape.lineTo(-0.15 * size, +0.45 * size);
        shape.closePath();

        return shape;
    }

}
