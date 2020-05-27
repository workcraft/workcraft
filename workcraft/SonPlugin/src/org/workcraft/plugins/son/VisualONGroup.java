package org.workcraft.plugins.son;

import org.workcraft.dom.visual.*;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class VisualONGroup extends VisualPage {
    private static final float strokeWidth = 0.03f;

    private ONGroup mathGroup = null;

    public VisualONGroup(ONGroup mathGroup) {
        super(mathGroup);
        this.mathGroup = mathGroup;
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        removePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_IS_COLLAPSED);
    }

    @Override
    public Point2D getLabelOffset() {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        double xOffset = bb.getMaxX();
        double yOffset = bb.getMinY();
        Rectangle2D labelBB = getLabelBoundingBox();
        if (labelBB != null) {
            xOffset -= (labelBB.getWidth() - getExpansion().getX()) / 2;
        }
        return new Point2D.Double(xOffset, yOffset);
    }

    @Override
    public Rectangle2D getLabelBoundingBox() {
        return BoundingBoxHelper.expand(super.getLabelBoundingBox(), 0.4, 0.2);
    }

    @Override
    public void draw(DrawRequest r) {
        for (VisualComponent component: Hierarchy.getChildrenOfType(this, VisualComponent.class)) {
            component.cacheRenderedText(r);
        }
        cacheRenderedText(r);

        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Rectangle2D groupBB = getInternalBoundingBoxInLocalSpace();
        if ((groupBB != null) && (getParent() != null)) {
            // Draw label
            if (!getLabel().isEmpty() && getLabelVisibility()) {
                Rectangle2D labelBB = getLabelBoundingBox();
                if (labelBB != null) {
                    g.setColor(ColorUtils.colorise(Color.WHITE, colorisation));
                    g.fill(labelBB);
                    g.setStroke(new BasicStroke(strokeWidth - 0.005f, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND, 3.0f, new float[]{0.1f, 0.05f}, 0f));

                    g.setColor(ColorUtils.colorise(getLabelColor(), colorisation));
                    g.draw(labelBB);
                    drawLabelInLocalSpace(r);
                }
            }
            // Draw group
            g.setColor(ColorUtils.colorise(this.getForegroundColor(), colorisation));
            g.setStroke(new BasicStroke(strokeWidth));
            groupBB = BoundingBoxHelper.expand(groupBB, getExpansion().getX(), getExpansion().getY());
            g.draw(groupBB);
            drawNameInLocalSpace(r);
        }
    }

    @Override
    public void setLabel(String label) {
        getMathGroup().setLabel(label);
        super.setLabel(label);
    }

    @Override
    public String getLabel() {
        return getMathGroup().getLabel();
    }

    @Override
    public void setForegroundColor(Color color) {
        this.getMathGroup().setForegroundColor(color);
    }

    @Override
    public Color getForegroundColor() {
        return this.getMathGroup().getForegroundColor();
    }

    public ONGroup getMathGroup() {
        return mathGroup;
    }

    public void setMathGroup(ONGroup mathGroup) {
        this.mathGroup = mathGroup;
    }

    public Collection<VisualCondition> getVisualConditions() {
        return Hierarchy.getDescendantsOfType(this, VisualCondition.class);
    }

    public Collection<VisualEvent> getVisualEvents() {
        return Hierarchy.getDescendantsOfType(this, VisualEvent.class);
    }

    public Collection<VisualSONConnection> getVisualSONConnections() {
        return Hierarchy.getDescendantsOfType(this, VisualSONConnection.class);
    }

    public Collection<VisualPage> getVisualPages() {
        return Hierarchy.getDescendantsOfType(this, VisualPage.class);
    }

    public Collection<VisualBlock> getVisualBlocks() {
        return Hierarchy.getDescendantsOfType(this, VisualBlock.class);
    }

    public Collection<VisualComment> getVisualComment() {
        return Hierarchy.getDescendantsOfType(this, VisualComment.class);
    }

    public Collection<VisualComponent> getVisualComponents() {
        return Hierarchy.getDescendantsOfType(this, VisualComponent.class);
    }

}
