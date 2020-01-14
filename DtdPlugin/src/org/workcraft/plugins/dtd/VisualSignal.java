package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.Coloriser;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;

@Hotkey(KeyEvent.VK_X)
@DisplayName("Signal")
@SVGIcon("images/dtd-node-signal.svg")
public class VisualSignal extends VisualComponent implements Container, CustomTouchable, ObservableHierarchy {

    protected DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

    public VisualSignal(Signal signal) {
        super(signal);
        configureProperties();
    }

    private void configureProperties() {
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
        removePropertyDeclarationByName(PROPERTY_LABEL);
        removePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_LABEL_COLOR);

        addPropertyDeclaration(new PropertyDeclaration<>(Signal.Type.class, Signal.PROPERTY_TYPE,
                this::setType, this::getType).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Signal.State.class, Signal.PROPERTY_INITIAL_STATE,
                this::setInitialState, this::getInitialState).setCombinable().setTemplatable());
    }

    @Override
    public Signal getReferencedComponent() {
        return (Signal) super.getReferencedComponent();
    }

    @Override
    public boolean checkForegroundColor(Color value) {
        if (!super.checkForegroundColor(value)) {
            return false;
        }

        VisualEntryEvent entry = getVisualSignalEntry();
        if ((entry != null) && !entry.checkForegroundColor(value)) {
            return false;
        }

        VisualExitEvent exit = getVisualSignalExit();
        if ((exit != null) && !exit.checkForegroundColor(value)) {
            return false;
        }

        for (VisualTransitionEvent transition : getVisualTransitions()) {
            if ((transition != null) && !transition.checkForegroundColor(value)) {
                return false;
            }
        }
        return true;
    }

    @NoAutoSerialisation
    public Signal.Type getType() {
        return getReferencedComponent().getType();
    }

    @NoAutoSerialisation
    public void setType(Signal.Type value) {
        getReferencedComponent().setType(value);
    }

    @NoAutoSerialisation
    public Signal.State getInitialState() {
        return getReferencedComponent().getInitialState();
    }

    @NoAutoSerialisation
    public void setInitialState(Signal.State value) {
        getReferencedComponent().setInitialState(value);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        drawNameInLocalSpace(r);
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        double size = VisualCommonSettings.getNodeSize();
        return new Rectangle2D.Double(-0.25 * size, -0.25 * size, 0.5 * size, 0.5 * size);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        Collection<Touchable> touchableChildren = Hierarchy.getChildrenOfType(this, Touchable.class);
        Rectangle2D childrenBB = BoundingBoxHelper.mergeBoundingBoxes(touchableChildren);
        return BoundingBoxHelper.union(bb, childrenBB);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
    }

    @Override
    public Positioning getNamePositioning() {
        return Positioning.LEFT;
    }

    @Override
    public Positioning getLabelPositioning() {
        return Positioning.CENTER;
    }

    @Override
    public Alignment getLabelAlignment() {
        return Alignment.CENTER;
    }

    @Override
    public boolean getLabelVisibility() {
        return true;
    }

    @Override
    public boolean getNameVisibility() {
        return true;
    }

    @Override
    public Color getNameColor() {
        switch (getType()) {
        case INPUT:    return SignalCommonSettings.getInputColor();
        case OUTPUT:   return SignalCommonSettings.getOutputColor();
        case INTERNAL: return SignalCommonSettings.getInternalColor();
        default:       return SignalCommonSettings.getDummyColor();
        }
    }

    @Override
    public void add(Node node) {
        groupImpl.add(node);
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
    public void setParent(Node parent) {
        groupImpl.setParent(parent);
    }

    @Override
    public void remove(Node node) {
        groupImpl.remove(node);
    }

    @Override
    public void add(Collection<? extends Node> nodes) {
        groupImpl.add(nodes);
    }

    @Override
    public void remove(Collection<? extends Node> nodes) {
        for (Node n : nodes) {
            remove(n);
        }
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
    public Node hitCustom(Point2D point) {
        Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
        for (Node node : getChildren()) {
            if (node instanceof VisualEvent) {
                VisualEvent event = (VisualEvent) node;
                double threshold = Math.min(0.1, event.getShape().getBounds2D().getWidth());
                if (event.hitTest(pointInLocalSpace)
                        || event.hitTest(new Point2D.Double(pointInLocalSpace.getX() - threshold, pointInLocalSpace.getY()))
                        || event.hitTest(new Point2D.Double(pointInLocalSpace.getX() + threshold, pointInLocalSpace.getY()))) {
                    return event;
                }
            }
        }
        return hitTest(point) ? this : null;
    }

    @Override
    public void addObserver(HierarchyObserver obs) {
        groupImpl.addObserver(obs);
    }

    @Override
    public void removeObserver(HierarchyObserver obs) {
        groupImpl.removeObserver(obs);
    }

    @Override
    public void removeAllObservers() {
        groupImpl.removeAllObservers();
    }

    public Collection<VisualTransitionEvent> getVisualTransitions() {
        HashSet<VisualTransitionEvent> result = new HashSet<>();
        for (Node node: getChildren()) {
            if (node instanceof VisualTransitionEvent) {
                result.add((VisualTransitionEvent) node);
            }
        }
        return result;
    }

    public VisualEntryEvent getVisualSignalEntry() {
        for (Node node: getChildren()) {
            if (node instanceof VisualEntryEvent) {
                return (VisualEntryEvent) node;
            }
        }
        return null;
    }

    public VisualExitEvent getVisualSignalExit() {
        for (Node node: getChildren()) {
            if (node instanceof VisualExitEvent) {
                return (VisualExitEvent) node;
            }
        }
        return null;
    }

}
