package org.workcraft.dom.visual;

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.ContainerDecoration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class VisualGroup extends VisualTransformableNode implements Drawable, Collapsible, Container, ObservableHierarchy {
    public static final String PROPERTY_IS_COLLAPSED = "Is collapsed";

    protected double size = VisualCommonSettings.getNodeSize();
    protected static final double margin = 0.20;

    private boolean currentLevelInside = false;
    private boolean collapsed = false;
    private boolean excited = false;
    private final DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

    public VisualGroup() {
        super();
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, PROPERTY_IS_COLLAPSED,
                this::setIsCollapsed, this::getIsCollapsed).setCombinable().setTemplatable());
    }

    @Override
    public void setIsCurrentLevelInside(boolean value) {
        if (currentLevelInside != value) {
            sendNotification(new TransformChangingEvent(this));
            this.currentLevelInside = value;
            sendNotification(new TransformChangedEvent(this));
        }
    }

    @Override
    public boolean isCurrentLevelInside() {
        return currentLevelInside;
    }

    @Override
    public void setIsCollapsed(boolean value) {
        if (collapsed != value) {
            sendNotification(new TransformChangingEvent(this));
            collapsed = value;
            sendNotification(new TransformChangedEvent(this));
        }
    }

    @Override
    public boolean getIsCollapsed() {
        return collapsed && !excited;
    }

    @Override
    public void setIsExcited(boolean value) {
        if (excited != value) {
            sendNotification(new TransformChangingEvent(this));
            excited = value;
            sendNotification(new TransformChangedEvent(this));
        }
    }

    @Override
    public void draw(DrawRequest r) {
        Decoration d = r.getDecoration();
        if (d instanceof ContainerDecoration) {
            ContainerDecoration containerDecoration = (ContainerDecoration) d;
            boolean shouldBeExcited = containerDecoration.isContainerExcited();
            if (getIsCollapsed() == shouldBeExcited) {
                setIsExcited(shouldBeExcited);
            }
        }
        // This is to update the rendered text for names (and labels) of group children,
        // which is necessary to calculate the bounding box before children have been drawn
        for (VisualComponent component: Hierarchy.getChildrenOfType(this, VisualComponent.class)) {
            component.cacheRenderedText(r);
        }
        if (getParent() != null) {
            drawOutline(r);
            drawPivot(r);
        }
    }

    public void drawOutline(DrawRequest r) {
        Decoration d = r.getDecoration();
        Graphics2D g = r.getGraphics();
        Rectangle2D bb = getBoundingBoxInLocalSpace();
        if (bb != null) {
            g.setColor(ColorUtils.colorise(Color.GRAY, d.getColorisation()));
            float[] pattern = {0.2f, 0.2f};
            g.setStroke(new BasicStroke(0.05f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));
            g.draw(bb);
        }
    }

    public void drawPivot(DrawRequest r) {
        Decoration d = r.getDecoration();
        Graphics2D g = r.getGraphics();
        if (d.getColorisation() != null) {
            float s2 = (float) VisualCommonSettings.getPivotSize() / 2;
            Path2D p = new Path2D.Double();
            p.moveTo(-s2, 0);
            p.lineTo(s2, 0);
            p.moveTo(0, -s2);
            p.lineTo(0, s2);
            g.setStroke(new BasicStroke((float) VisualCommonSettings.getPivotWidth()));
            g.draw(p);
        }
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = null;
        if (!getIsCollapsed() || isCurrentLevelInside()) {
            Collection<Touchable> children = Hierarchy.getChildrenOfType(this, Touchable.class);
            bb = BoundingBoxHelper.mergeBoundingBoxes(children);
        }
        if (bb == null) {
            bb = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
        }
        return BoundingBoxHelper.expand(bb, margin, margin);
    }

    public Collection<VisualNode> unGroup() {
        Collection<VisualNode> nodesToReparent = NodeHelper.filterByType(groupImpl.getChildren(), VisualNode.class);
        Container newParent = Hierarchy.getNearestAncestor(getParent(), Container.class);
        groupImpl.reparent(nodesToReparent, newParent);
        double tx = localToParentTransform.getTranslateX();
        double ty = localToParentTransform.getTranslateY();
        VisualModelTransformer.translateNodes(nodesToReparent, -tx, -ty);
        return nodesToReparent;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        Rectangle2D bb = getBoundingBoxInLocalSpace();
        if ((bb != null) && (getParent() != null)) {
            return bb.contains(pointInLocalSpace);
        }
        return false;
    }

    @Override
    public void add(Node node) {
        groupImpl.add(node);
    }

    @Override
    public void addObserver(HierarchyObserver obs) {
        groupImpl.addObserver(obs);
    }

    @Override
    public Collection<Node> getChildren() {
        return groupImpl.getChildren();
    }

    @Override
    public Node getParent() {
        return groupImpl.getParent();
    }

    @Override
    public void remove(Node node) {
        groupImpl.remove(node);
    }

    public void removeWithoutNotify(Node node) {
        groupImpl.removeWithoutNotify(node);
    }

    @Override
    public void removeObserver(HierarchyObserver obs) {
        groupImpl.removeObserver(obs);
    }

    @Override
    public void removeAllObservers() {
        groupImpl.removeAllObservers();
    }

    @Override
    public void setParent(Node parent) {
        groupImpl.setParent(parent);
    }

    @Override
    public void add(Collection<? extends Node> nodes) {
        groupImpl.add(nodes);
    }

    @Override
    public void remove(Collection<? extends Node> nodes) {
        groupImpl.remove(nodes);
    }

    @Override
    public void reparent(Collection<? extends Node> nodes, Container newParent) {
        groupImpl.reparent(nodes, newParent);
    }

    @Override
    public void reparent(Collection<? extends Node> nodes) {
        groupImpl.reparent(nodes);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualGroup) {
            VisualGroup srcGroup = (VisualGroup) src;
            setIsCollapsed(srcGroup.getIsCollapsed());
        }
    }

}
