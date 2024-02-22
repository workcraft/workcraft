package org.workcraft.plugins.petri;

import org.workcraft.dom.visual.*;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VisualReplicaPlace extends VisualReplica {

    public VisualReplicaPlace() {
        super();
        removePropertyDeclarationByName(PROPERTY_COLOR);
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
    }

    public VisualReplicaPlace(VisualComponent master) {
        super();
        setMaster(master);
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

    public Place getReferencedPlace() {
        if (getMaster() instanceof VisualPlace) {
            VisualPlace visualPlace = (VisualPlace) getMaster();
            return visualPlace.getReferencedComponent();
        }
        return null;
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return BoundingBoxHelper.expand(getNameBoundingBox(), 0.2, 0.2);
    }

}
