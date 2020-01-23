package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;

import java.awt.*;
import java.awt.geom.Path2D;

@DisplayName("Sync")
@SVGIcon("images/xmas-node-sync.svg")
public class VisualSyncComponent extends VisualXmasComponent {

    private int xOffset = 0;

    public VisualSyncComponent(SyncComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.createInput(Positioning.LEFT);
            this.createOutput(Positioning.RIGHT);
        } else {
            int numInputs = component.getInputs().size();
            int numOutputs = component.getOutputs().size();
            if (numInputs > numOutputs) {
                xOffset = numInputs - 1;
            } else {
                xOffset = numOutputs - 1;
            }
        }
    }

    @Override
    public SyncComponent getReferencedComponent() {
        return (SyncComponent) super.getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();
        shape.moveTo(-0.5 * SIZE, -0.4 * SIZE);
        shape.lineTo(-0.5 * SIZE, +0.4 * SIZE + (xOffset * 0.5));
        shape.lineTo(+0.5 * SIZE, +0.4 * SIZE + (xOffset * 0.5));
        shape.lineTo(+0.5 * SIZE, -0.4 * SIZE);
        shape.closePath();
        return shape;
    }

}
