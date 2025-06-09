package org.workcraft.plugins.son;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class VisualSuperGroup extends VisualGroup {

    private static final float strokeWidth = 0.03f;
    private static final float frameDepth = 0.5f;

    private Rectangle2D contentsBB = null;
    private String label = "";

    private static final Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(0.5f);

    public VisualSuperGroup() {
        addPropertyDeclaration(new PropertyDeclaration<>(String.class, "Label",
                this::setLabel, this::getLabel).setCombinable().setTemplatable());

        removePropertyDeclarationByName(PROPERTY_IS_COLLAPSED);
    }

    private Rectangle2D getContentsBoundingBox() {
        Rectangle2D bb = null;
        for (VisualComponent v: Hierarchy.getChildrenOfType(this, VisualComponent.class)) {
            bb = BoundingBoxHelper.union(bb, v.getBoundingBox());
        }

        for (VisualONGroup v: Hierarchy.getChildrenOfType(this, VisualONGroup.class)) {
            bb = BoundingBoxHelper.union(bb, v.getBoundingBox());
        }

        for (VisualSONConnection v: Hierarchy.getChildrenOfType(this, VisualSONConnection.class)) {
            bb = BoundingBoxHelper.union(bb, v.getBoundingBox());
        }

        if (bb == null) {
            bb = contentsBB;
        } else {
            bb.setRect(bb.getMinX() - frameDepth, bb.getMinY() - frameDepth,
                    bb.getWidth() + 2.0 * frameDepth, bb.getHeight() + 2.0 * frameDepth);
        }

        if (bb == null) {
            bb = new Rectangle2D.Double(0, 0, 1, 1);
        }
        contentsBB = (Rectangle2D) bb.clone();
        return bb;
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Rectangle2D bb = getContentsBoundingBox();
        if (getParent() != null) {
            g.setColor(ColorUtils.colorise(Color.WHITE, colorisation));
            g.fill(bb);
            g.setColor(ColorUtils.colorise(Color.BLACK, colorisation));
            g.setStroke(new BasicStroke((float) (strokeWidth * 0.5)));
            g.draw(bb);

            // draw label
            GlyphVector glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), getLabel());
            Rectangle2D labelBB = glyphVector.getVisualBounds();
            labelBB = BoundingBoxHelper.expand(labelBB, 0.4, 0.2);
            Point2D labelPosition = new Point2D.Double(bb.getMaxX() - labelBB.getMaxX(), bb.getMinY() - labelBB.getMaxY());
            g.drawGlyphVector(glyphVector, (float) labelPosition.getX(), (float) labelPosition.getY());
        }
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D p) {
        return getContentsBoundingBox().contains(p);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    public Collection<VisualComponent> getVisualComponents() {
        return Hierarchy.getDescendantsOfType(this, VisualComponent.class);
    }

    public Collection<VisualSONConnection> getVisualSONConnections() {
        return Hierarchy.getDescendantsOfType(this, VisualSONConnection.class);
    }

    public Collection<VisualONGroup> getVisualONGroups() {
        return Hierarchy.getDescendantsOfType(this, VisualONGroup.class);
    }

}
