package org.workcraft.plugins.circuit;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VisualReplicaContact extends VisualReplica {

    @SuppressWarnings("unused") // Required for deserialisation and is called via reflection
    public VisualReplicaContact() {
        super();
        removePropertyDeclarationByName(PROPERTY_COLOR);
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
    }

    @SuppressWarnings("unused") // Required for deserialisation and is called via reflection
    public VisualReplicaContact(VisualContact contact) {
        super();
        setMaster(contact);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        cacheRenderedText(r);  // needed to better estimate the bounding box
        Color colorisation = d.getColorisation();
        g.setColor(ColorUtils.colorise(getForegroundColor(), colorisation));
        drawNameInLocalSpace(r);
    }

    @Override
    public boolean getNameVisibility() {
        return true;
    }

    @Override
    public Positioning getNamePositioning() {
        return Positioning.CENTER;
    }

    @Override
    public Point2D getNameOffset() {
        return new Point2D.Double(0.0, 0.0);
    }

    public Contact getReferencedContact() {
        if (getMaster() instanceof VisualContact) {
            VisualContact contact = (VisualContact) getMaster();
            return contact.getReferencedComponent();
        }
        return null;
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return BoundingBoxHelper.expand(getNameBoundingBox(), 0.2, 0.2);
    }

}
