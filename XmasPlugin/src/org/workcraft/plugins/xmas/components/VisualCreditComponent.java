package org.workcraft.plugins.xmas.components;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

@DisplayName("Credit")
@SVGIcon("images/xmas-node-credit.svg")
public class VisualCreditComponent extends VisualXmasComponent {
    public Color color = new Color(0, 0, 0, 255);

    public VisualCreditComponent(CreditComponent component) {
        super(component);
        addPropertyDeclarations();
        if (component.getChildren().isEmpty()) {
            this.addInput("i", Positioning.LEFT);
            this.addOutput("o", Positioning.RIGHT);
        }
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualCreditComponent, Integer>(
                this, CreditComponent.PROPERTY_CAPACITY, Integer.class, true, true, true) {
            public void setter(VisualCreditComponent object, Integer value) {
                object.getReferencedCreditComponent().setCapacity(value);
            }
            public Integer getter(VisualCreditComponent object) {
                return object.getReferencedCreditComponent().getCapacity();
            }
        });
        addPropertyDeclaration(new PropertyDeclaration<VisualCreditComponent, Integer>(
                this, CreditComponent.PROPERTY_INIT, Integer.class, true, true, true) {
            public void setter(VisualCreditComponent object, Integer value) {
                object.getReferencedCreditComponent().setInit(value);
            }
            public Integer getter(VisualCreditComponent object) {
                return object.getReferencedCreditComponent().getInit();
            }
        });
    }

    public CreditComponent getReferencedCreditComponent() {
        return (CreditComponent) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.5 * size, -0.4 * size);
        shape.lineTo(-0.5 * size, +0.4 * size);
        shape.lineTo(+0.5 * size, +0.4 * size);
        shape.lineTo(+0.5 * size, -0.4 * size);
        shape.closePath();

        shape.moveTo(0.0, -0.4 * size);
        shape.lineTo(0.0, +0.4 * size);

        double tokenSize = size / 10.0;
        for (int i = 0; i < 4; i++) {
            shape.append(new Ellipse2D.Double(-0.2 * size - 0.5 * tokenSize, -0.5 * tokenSize, tokenSize, tokenSize), false);
            shape.append(new Ellipse2D.Double(+0.2 * size - 0.5 * tokenSize, -0.5 * tokenSize, tokenSize, tokenSize), false);
            tokenSize /= 3.0;
        }

        return shape;
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualCreditComponent) {
            CreditComponent srcComponent = ((VisualCreditComponent) src).getReferencedCreditComponent();
            getReferencedCreditComponent().setCapacity(srcComponent.getCapacity());
            getReferencedCreditComponent().setInit(srcComponent.getInit());
        }
    }

}
