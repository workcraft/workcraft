package org.workcraft.plugins.stg;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.tools.CoreDecoration;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.Coloriser;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VisualNamedTransition extends VisualTransition implements StateObserver {


    public VisualNamedTransition(NamedTransition namedTransition) {
        super(namedTransition, false, false, false);
        namedTransition.addObserver(this);
    }

    public Point2D getRenderedTextOffset() {
        return new Point2D.Double(0.0, 0.0);
    }

    @Override
    public boolean getLabelVisibility() {
        return false;
    }

    @Override
    public Point2D getLabelOffset() {
        return new Point2D.Double(0.0, 0.0);
    }

    @Override
    public  Font getNameFont() {
        return LABEL_FONT.deriveFont((float) StgSettings.getTransitionFontSize());
    }

    @Override
    public Positioning getNamePositioning() {
        return Positioning.CENTER;
    }

    @Override
    public boolean getNameVisibility() {
        return true;
    }

    @Override
    public Point2D getNameOffset() {
        return new Point2D.Double(0.0, 0.0);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        if (d instanceof CoreDecoration) {
            Rectangle2D expandedShape = BoundingBoxHelper.expand(getBoundingBoxInLocalSpace(), 0.5, 0.5);
            Color[] palette = ((CoreDecoration) d).getColorisationPalette();
            if (palette == null) {
                Color color = d.getBackground();
                if (color != null) {
                    g.setColor(color);
                    g.fill(expandedShape);
                }
            } else {
                double x = expandedShape.getX();
                double y = expandedShape.getY();
                double w = expandedShape.getWidth() / palette.length;
                double h = expandedShape.getHeight();
                for (Color color: palette) {
                    g.setColor(color);
                    Rectangle2D shape = new Rectangle2D.Double(x, y, w, h);
                    g.fill(shape);
                    x += w;
                }
            }
        } else {
            Color background = d.getBackground();
            if (background != null) {
                g.setColor(Coloriser.colorise(EditorCommonSettings.getBackgroundColor(), background));
                Rectangle2D expandedShape = BoundingBoxHelper.expand(getBoundingBoxInLocalSpace(), 0.5, 0.5);
                g.fill(expandedShape);
            }
        }
        drawNameInLocalSpace(r);
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return BoundingBoxHelper.expand(getNameBoundingBox(), 0.2, 0.2);
    }

    @Override
    public NamedTransition getReferencedComponent() {
        return (NamedTransition) super.getReferencedComponent();
    }

    @NoAutoSerialisation
    public String getName() {
        return getReferencedComponent().getName();
    }

    @Override
    public void cacheNameRenderedText(DrawRequest r) {
        cacheNameRenderedText(getName(), getNameFont(), getNamePositioning(), getNameOffset());
    }

    @Override
    public void notify(StateEvent e) {
        cacheNameRenderedText(getName(), getNameFont(), getNamePositioning(), getNameOffset());
        // Updating the name rendered text changes bounding box of the transition, therefore transform notification should be sent.
        transformChanged();
    }

}
