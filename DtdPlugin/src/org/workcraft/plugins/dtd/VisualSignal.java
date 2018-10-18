package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.plugins.shared.CommonSignalSettings;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Hierarchy;

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

    public static final String PROPERTY_COLOR = "Color";
    protected DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

    public VisualSignal(Signal signal) {
        super(signal);
        configureProperties();
    }

    private void configureProperties() {
        renamePropertyDeclarationByName(PROPERTY_FOREGROUND_COLOR, PROPERTY_COLOR);
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
        removePropertyDeclarationByName(PROPERTY_LABEL);
        removePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_LABEL_COLOR);

        addPropertyDeclaration(new PropertyDeclaration<VisualSignal, Signal.Type>(
                this, Signal.PROPERTY_TYPE, Signal.Type.class, true, true, true) {
            protected void setter(VisualSignal object, Signal.Type value) {
                object.setType(value);
            }
            protected Signal.Type getter(VisualSignal object) {
                return object.getType();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualSignal, Signal.State>(
                this, Signal.PROPERTY_INITIAL_STATE, Signal.State.class, true, true, true) {
            protected void setter(VisualSignal object, Signal.State value) {
                object.setInitialState(value);
            }
            protected Signal.State getter(VisualSignal object) {
                return object.getInitialState();
            }
        });
    }

    public Signal getReferencedSignal() {
        return (Signal) getReferencedComponent();
    }

    @NoAutoSerialisation
    public Signal.Type getType() {
        return getReferencedSignal().getType();
    }

    @NoAutoSerialisation
    public void setType(Signal.Type value) {
        getReferencedSignal().setType(value);
    }

    @NoAutoSerialisation
    public Signal.State getInitialState() {
        return getReferencedSignal().getInitialState();
    }

    @NoAutoSerialisation
    public void setInitialState(Signal.State value) {
        getReferencedSignal().setInitialState(value);
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
        case INPUT:    return CommonSignalSettings.getInputColor();
        case OUTPUT:   return CommonSignalSettings.getOutputColor();
        case INTERNAL: return CommonSignalSettings.getInternalColor();
        default:       return CommonSignalSettings.getDummyColor();
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
                if (event.hitTest(pointInLocalSpace)) {
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
