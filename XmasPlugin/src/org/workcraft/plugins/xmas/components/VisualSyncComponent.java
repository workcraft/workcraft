package org.workcraft.plugins.xmas.components;

import java.awt.Shape;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;

@DisplayName("Sync")
@SVGIcon("images/xmas-node-sync.svg")
public class VisualSyncComponent extends VisualXmasComponent {

    public int xOffset = 0;

    public VisualSyncComponent(SyncComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.addInput("i", Positioning.LEFT);
            this.addOutput("o", Positioning.RIGHT);
        } else {
            int numInputs = ((XmasComponent) component).getInputs().size();
            int numOutputs = ((XmasComponent) component).getOutputs().size();
            if (numInputs > numOutputs) {
                xOffset = numInputs - 1;
            } else {
                xOffset = numOutputs - 1;
            }
        }
    }

    public SyncComponent getReferencedSyncComponent() {
        return (SyncComponent) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.5 * size, -0.4 * size);
        shape.lineTo(-0.5 * size, +0.4 * size + (xOffset * 0.5));
        shape.lineTo(+0.5 * size, +0.4 * size + (xOffset * 0.5));
        shape.lineTo(+0.5 * size, -0.4 * size);
        shape.closePath();

        return shape;
    }

}
