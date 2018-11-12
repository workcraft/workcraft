package org.workcraft.plugins.xmas.components;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class VisualXmasContact extends VisualComponent implements StateObserver {

    private static final double SIZE = 0.3 * CommonVisualSettings.getNodeSize();

    public VisualXmasContact(XmasContact contact) {
        super(contact);
        contact.addObserver(this);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualXmasContact, IOType>(
                this, XmasContact.PROPERTY_IO_TYPE, IOType.class, false, false) {
            @Override
            public void setter(VisualXmasContact object, IOType value) {
                object.getReferencedContact().setIOType(value);
            }
            @Override
            public IOType getter(VisualXmasContact object) {
                return object.getReferencedContact().getIOType();
            }
            @Override
            public boolean isEditable() {
                return false;
            }
        });
    }

    public XmasContact getReferencedContact() {
        return (XmasContact) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        double pos = -0.5 * SIZE;
        if ((getReferencedContact() == null) || (getReferencedContact().getIOType() == IOType.INPUT)) {
            return new Ellipse2D.Double(pos, pos, SIZE, SIZE);
        }
        return new Rectangle2D.Double(pos, pos, SIZE, SIZE);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        boolean inSimulationMode = (d.getColorisation() != null) || (d.getBackground() != null);
        if (inSimulationMode || XmasSettings.getShowContacts()) {
            Shape shape = getShape();
            g.setStroke(new BasicStroke((float) XmasSettings.getWireWidth()));

            Color fillColor = d.getBackground();
            if (fillColor == null) {
                fillColor = getFillColor();
            }
            g.setColor(fillColor);
            g.fill(shape);

            Color colorisation = d.getColorisation();
            g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
            g.draw(shape);
        }
    }

    public boolean isInput() {
        return getReferencedContact().isInput();
    }

    public boolean isOutput() {
        return getReferencedContact().isOutput();
    }

    @Override
    public void notify(StateEvent e) {
    }

}
