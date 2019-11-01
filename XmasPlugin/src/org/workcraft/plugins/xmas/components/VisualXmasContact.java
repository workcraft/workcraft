package org.workcraft.plugins.xmas.components;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;
import org.workcraft.utils.Coloriser;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class VisualXmasContact extends VisualComponent implements StateObserver {

    private static final double SIZE = 0.3 * VisualCommonSettings.getNodeSize();

    public VisualXmasContact(XmasContact contact) {
        super(contact);
        contact.addObserver(this);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(IOType.class, XmasContact.PROPERTY_IO_TYPE,
                value -> getReferencedComponent().setIOType(value),
                () -> getReferencedComponent().getIOType())
                .setReadonly());
    }

    @Override
    public XmasContact getReferencedComponent() {
        return (XmasContact) super.getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        double pos = -0.5 * SIZE;
        if ((getReferencedComponent() == null) || (getReferencedComponent().getIOType() == IOType.INPUT)) {
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
        return getReferencedComponent().isInput();
    }

    public boolean isOutput() {
        return getReferencedComponent().isOutput();
    }

    @Override
    public void notify(StateEvent e) {
    }

}
