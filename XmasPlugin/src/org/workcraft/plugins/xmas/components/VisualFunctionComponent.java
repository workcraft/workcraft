package org.workcraft.plugins.xmas.components;

import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.xmas.components.FunctionComponent.Type;

@DisplayName("Function")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/xmas-node-function.svg")
public class VisualFunctionComponent extends VisualXmasComponent {

    public VisualFunctionComponent(FunctionComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.addInput("", Positioning.LEFT);
            this.addOutput("", Positioning.RIGHT);
        }
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualFunctionComponent, Type>(
                this, FunctionComponent.PROPERTY_TYPE, Type.class, true, true, true) {
            protected void setter(VisualFunctionComponent object, Type value) {
                object.getReferencedFunctionComponent().setType(value);
            }
            protected Type getter(VisualFunctionComponent object) {
                return object.getReferencedFunctionComponent().getType();
            }
        });
    }

    public FunctionComponent getReferencedFunctionComponent() {
        return (FunctionComponent) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.5 * size, 0.0);
        shape.lineTo(+0.5 * size, 0.0);

        shape.moveTo(-0.2 * size, +0.2 * size);
        shape.lineTo(+0.2 * size, -0.2 * size);

        return shape;
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualFunctionComponent) {
            FunctionComponent srcComponent = ((VisualFunctionComponent) src).getReferencedFunctionComponent();
            getReferencedFunctionComponent().setType(srcComponent.getType());
        }
    }

}
