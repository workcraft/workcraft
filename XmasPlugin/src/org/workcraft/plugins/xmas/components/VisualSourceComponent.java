package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.SourceComponent.Mode;
import org.workcraft.plugins.xmas.components.SourceComponent.Type;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

@DisplayName("Source")
@Hotkey(KeyEvent.VK_I)
@SVGIcon("images/xmas-node-source.svg")
public class VisualSourceComponent extends VisualXmasComponent {

    private VisualXmasContact oContact = null;

    public VisualSourceComponent(SourceComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            oContact = addOutput(Positioning.CENTER);
        }
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualSourceComponent, Type>(
                this, SourceComponent.PROPERTY_TYPE, Type.class, true, true) {
            @Override
            public void setter(VisualSourceComponent object, Type value) {
                object.getReferencedSourceComponent().setType(value);
            }
            @Override
            public Type getter(VisualSourceComponent object) {
                return object.getReferencedSourceComponent().getType();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualSourceComponent, Mode>(
                this, SourceComponent.PROPERTY_MODE, Mode.class, true, true) {
            @Override
            public void setter(VisualSourceComponent object, Mode value) {
                object.getReferencedSourceComponent().setMode(value);
            }
            @Override
            public Mode getter(VisualSourceComponent object) {
                return object.getReferencedSourceComponent().getMode();
            }
        });
    }

    public SourceComponent getReferencedSourceComponent() {
        return (SourceComponent) getReferencedComponent();
    }

    public VisualXmasContact getOContact() {
        return oContact;
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(0.00, 0.00);
        shape.lineTo(0.00, -0.60 * SIZE);

        shape.moveTo(-0.40 * SIZE, -0.60 * SIZE);
        shape.lineTo(+0.40 * SIZE, -0.60 * SIZE);

        return shape;
    }

    public Shape getTokenShape() {
        return new Ellipse2D.Double(-1.8 * TOKEN_SIZE, -2.5 * TOKEN_SIZE, TOKEN_SIZE, TOKEN_SIZE);
    }

    @Override
    public void draw(DrawRequest r) {
        super.draw(r);
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        if (d instanceof StateDecoration) {
            if (((StateDecoration) d).getState()) {
                g.setStroke(new BasicStroke((float) XmasSettings.getBorderWidth()));
                g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
                Shape shape = transformShape(getTokenShape());
                g.fill(shape);
                g.draw(shape);
            }
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualSourceComponent) {
            SourceComponent srcComponent = ((VisualSourceComponent) src).getReferencedSourceComponent();
            getReferencedSourceComponent().setType(srcComponent.getType());
            getReferencedSourceComponent().setMode(srcComponent.getMode());
        }
    }

}
