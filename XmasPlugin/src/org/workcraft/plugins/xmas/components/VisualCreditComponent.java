package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.properties.PropertyDeclaration;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

@DisplayName("Credit")
@SVGIcon("images/xmas-node-credit.svg")
public class VisualCreditComponent extends VisualXmasComponent {

    public VisualCreditComponent(CreditComponent component) {
        super(component);
        addPropertyDeclarations();
        if (component.getChildren().isEmpty()) {
            this.addInput(Positioning.LEFT);
            this.addOutput(Positioning.RIGHT);
        }
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Integer.class,
                CreditComponent.PROPERTY_CAPACITY,
                (value) -> getReferencedComponent().setCapacity(value),
                () -> getReferencedComponent().getCapacity())
                .setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Integer.class,
                CreditComponent.PROPERTY_INIT,
                (value) -> getReferencedComponent().setInit(value),
                () -> getReferencedComponent().getInit())
                .setCombinable().setTemplatable());
    }

    @Override
    public CreditComponent getReferencedComponent() {
        return (CreditComponent) super.getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.5 * SIZE, -0.4 * SIZE);
        shape.lineTo(-0.5 * SIZE, +0.4 * SIZE);
        shape.lineTo(+0.5 * SIZE, +0.4 * SIZE);
        shape.lineTo(+0.5 * SIZE, -0.4 * SIZE);
        shape.closePath();

        shape.moveTo(0.0, -0.4 * SIZE);
        shape.lineTo(0.0, +0.4 * SIZE);

        double tokenSize = SIZE / 10.0;
        for (int i = 0; i < 4; i++) {
            shape.append(new Ellipse2D.Double(-0.2 * SIZE - 0.5 * tokenSize, -0.5 * tokenSize, tokenSize, tokenSize), false);
            shape.append(new Ellipse2D.Double(+0.2 * SIZE - 0.5 * tokenSize, -0.5 * tokenSize, tokenSize, tokenSize), false);
            tokenSize /= 3.0;
        }

        return shape;
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualCreditComponent) {
            CreditComponent srcComponent = ((VisualCreditComponent) src).getReferencedComponent();
            getReferencedComponent().setCapacity(srcComponent.getCapacity());
            getReferencedComponent().setInit(srcComponent.getInit());
        }
    }

}
