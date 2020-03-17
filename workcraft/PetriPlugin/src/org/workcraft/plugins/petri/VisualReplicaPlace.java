package org.workcraft.plugins.petri;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;

@DisplayName("Proxy place")
@SVGIcon("images/petri-node-proxy.svg")
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
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
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

    public Place getReferencedPlace() {
        if (getMaster() instanceof VisualPlace) {
            VisualPlace visualPlace = (VisualPlace) getMaster();
            return visualPlace.getReferencedComponent();
        }
        return null;
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        Rectangle2D nameBoundingBox = getNameBoundingBox();
        if (nameBoundingBox == null) {
            nameBoundingBox = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
        }
        return BoundingBoxHelper.expand(nameBoundingBox, 0.2, 0.2);
    }

}
