package org.workcraft.plugins.son;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;

public class VisualSuperGroup extends VisualGroup {

    private static final float strokeWidth = 0.03f;
    private static final float frameDepth = 0.5f;

    private GlyphVector glyphVector;
    private Rectangle2D contentsBB = null;
    private Rectangle2D labelBB = null;
    private String label = "";

    private static final Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(0.5f);

    public VisualSuperGroup() {
        addPropertyDeclaration(new PropertyDeclaration<VisualSuperGroup, String>(
                this, "Label", String.class, true, true) {
            @Override
            public void setter(VisualSuperGroup object, String value) {
                object.setLabel(value);
            }
            @Override
            public String getter(VisualSuperGroup object) {
                return object.getLabel();
            }
        });

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

        if (bb == null) bb = contentsBB;
        else {
            bb.setRect(bb.getMinX() - frameDepth, bb.getMinY() - frameDepth,
                    bb.getWidth() + 2.0 * frameDepth, bb.getHeight() + 2.0 * frameDepth);
        }

        if (bb == null) bb = new Rectangle2D.Double(0, 0, 1, 1);

        contentsBB = (Rectangle2D) bb.clone();

        return bb;
    }

    @Override
    public void draw(DrawRequest r) {

        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();

        Rectangle2D bb = getContentsBoundingBox();

        if (bb != null && getParent() != null) {
            g.setColor(Coloriser.colorise(Color.WHITE, colorisation));
            g.fill(bb);
            g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
            g.setStroke(new BasicStroke((float) (strokeWidth * 0.5)));
            g.draw(bb);

            // draw label

            glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), getLabel());

            labelBB = glyphVector.getVisualBounds();

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

    public Collection<Node> getVisualNodes() {
        Collection<Node> result = new HashSet<>();

        result.addAll(getVisualComponents());
        result.addAll(getVisualSONConnections());
        for (VisualONGroup vg : this.getVisualONGroups()) {
            result.addAll(vg.getVisualComment());
        }

        return result;
    }
}
