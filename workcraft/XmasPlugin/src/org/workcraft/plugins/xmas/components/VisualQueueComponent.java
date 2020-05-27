package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

@DisplayName("Queue")
@Hotkey(KeyEvent.VK_Q)
@SVGIcon("images/xmas-node-queue.svg")
public class VisualQueueComponent extends VisualXmasComponent {

    public static final double SLOT_WIDTH = 0.35 * SIZE;
    public static final double SLOT_HEIGHT = 1.0 * SIZE;
    public static final double HEAD_SIZE = 0.15 * SIZE;
    public static final double TAIL_SIZE = 0.15 * SIZE;
    private static final double CONTACT_LENGTH = 0.5 * SIZE - SLOT_WIDTH;

    public VisualQueueComponent(QueueComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.createInput(Positioning.LEFT);
            this.createOutput(Positioning.RIGHT);
        }
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Integer.class,
                QueueComponent.PROPERTY_CAPACITY,
                value -> {
                    if (value < 1) {
                        throw new ArgumentException("Negative or zero capacity is not allowed.");
                    }
                    getReferencedComponent().setCapacity(value);
                    AffineTransform unrotateTransform = new AffineTransform();
                    unrotateTransform.quadrantRotate(-getOrientation().getQuadrant());
                    AffineTransform rotateTransform = new AffineTransform();
                    rotateTransform.quadrantRotate(getOrientation().getQuadrant());
                    for (VisualXmasContact contact: getContacts()) {
                        TransformHelper.applyTransform(contact, unrotateTransform);
                        if (contact.isInput()) {
                            setContactPosition(contact, Positioning.LEFT);
                        } else {
                            setContactPosition(contact, Positioning.RIGHT);
                        }
                        TransformHelper.applyTransform(contact, rotateTransform);
                    }
                },
                () -> getReferencedComponent().getCapacity())
                .setCombinable().setTemplatable());

    }

    @Override
    public void setContactPosition(VisualXmasContact vc, Positioning positioning) {
        double factor2 = (double) getReferencedComponent().getCapacity() / 2.0;
        double offset = factor2 * (SIZE / 2 - CONTACT_LENGTH) + CONTACT_LENGTH;
        double x = positioning.xSign * offset;
        double y = positioning.ySign * offset;
        vc.setPosition(new Point2D.Double(x, y));
    }

    @Override
    public QueueComponent getReferencedComponent() {
        return (QueueComponent) super.getReferencedComponent();
    }

    private boolean isInitialised() {
        return getReferencedComponent() != null;
    }

    private double getSlotOffset(int i) {
        int capacity = getReferencedComponent().getCapacity();
        return SLOT_WIDTH * (i - 0.5 * (capacity - 1));
    }

    public Shape getSlotShape(int index) {
        Path2D shape = new Path2D.Double();
        if (isInitialised()) {
            double w2 = 0.5 * SLOT_WIDTH;
            double h2 = 0.5 * SLOT_HEIGHT;
            double slotOffset = getSlotOffset(index);
            shape.moveTo(slotOffset - w2, -h2);
            shape.lineTo(slotOffset - w2, +h2);
            shape.lineTo(slotOffset + w2, +h2);
            shape.lineTo(slotOffset + w2, -h2);
            shape.closePath();
        }
        return shape;
    }

    public Shape getTokenShape(int index) {
        Path2D shape = new Path2D.Double();
        if (isInitialised()) {
            double slotOffset = getSlotOffset(index);
            shape.append(new Ellipse2D.Double(slotOffset - 0.5 * TOKEN_SIZE, -0.5 * TOKEN_SIZE, TOKEN_SIZE, TOKEN_SIZE), false);
        }
        return shape;
    }

    public Shape getHeadShape(int index) {
        Path2D shape = new Path2D.Double();
        if (isInitialised()) {
            double slotOffset = getSlotOffset(index);
            double headOffset = -0.5 * SLOT_HEIGHT;
            shape.moveTo(slotOffset - 0.7 * HEAD_SIZE, headOffset);
            shape.lineTo(slotOffset + 0.00, headOffset + HEAD_SIZE);
            shape.lineTo(slotOffset + 0.7 * HEAD_SIZE, headOffset);
            shape.closePath();
        }
        return shape;
    }

    public Shape getTailShape(int index) {
        Path2D shape = new Path2D.Double();
        if (isInitialised()) {
            double slotOffset = getSlotOffset(index);
            double tailOffset = 0.5 * SLOT_HEIGHT;
            shape.moveTo(slotOffset - 0.7 * TAIL_SIZE, tailOffset);
            shape.lineTo(slotOffset + 0.00, tailOffset - TAIL_SIZE);
            shape.lineTo(slotOffset + 0.7 * TAIL_SIZE, tailOffset);
            shape.closePath();
        }
        return shape;
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();
        QueueComponent ref = getReferencedComponent();
        if (ref != null) {
            int capacity = ref.getCapacity();
            double contactOffset = 0.5 * capacity * SLOT_WIDTH;

            shape.moveTo(+contactOffset, 0.0);
            shape.lineTo(+contactOffset + CONTACT_LENGTH, 0.0);

            shape.moveTo(-contactOffset, 0.0);
            shape.lineTo(-contactOffset - CONTACT_LENGTH, 0.0);

            for (int i = 0; i < capacity; i++) {
                shape.append(getSlotShape(i), false);
            }
        }
        return shape;
    }

    @Override
    public void draw(org.workcraft.dom.visual.DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        if (d instanceof QueueDecoration) {
            int capacity = getReferencedComponent().getCapacity();
            // Quiescent elements
            g.setColor(getForegroundColor());
            for (int i = 0; i < capacity; i++) {
                SlotState slot = ((QueueDecoration) d).getSlotState(i);
                Shape slotShape = transformShape(getSlotShape(i));
                g.draw(slotShape);
                if (!slot.isMemExcited && slot.isFull) {
                    Shape tokenShape = transformShape(getTokenShape(i));
                    g.draw(tokenShape);
                    g.fill(tokenShape);
                }
                if (!slot.isHeadExcited && slot.isHead) {
                    Shape headShape = transformShape(getHeadShape(i));
                    g.draw(headShape);
                    g.fill(headShape);
                }
                if (!slot.isTailExcited && slot.isTail) {
                    Shape tailShape = transformShape(getTailShape(i));
                    g.draw(tailShape);
                    g.fill(tailShape);
                }
            }
            // Excited elements
            g.setColor(ColorUtils.colorise(getForegroundColor(), SimulationDecorationSettings.getExcitedComponentColor()));
            for (int i = 0; i < capacity; i++) {
                SlotState slot = ((QueueDecoration) d).getSlotState(i);
                if (slot.isMemExcited) {
                    Shape tokenShape = transformShape(getTokenShape(i));
                    g.draw(tokenShape);
                    if (slot.isFull) {
                        g.fill(tokenShape);
                    }
                }
                if (slot.isHeadExcited) {
                    Shape headShape = transformShape(getHeadShape(i));
                    g.draw(headShape);
                    if (slot.isHead) {
                        g.fill(headShape);
                    }
                }
                if (slot.isTailExcited) {
                    Shape tailShape = transformShape(getTailShape(i));
                    g.draw(tailShape);
                    if (slot.isTail) {
                        g.fill(tailShape);
                    }
                }
            }
        } else {
            super.draw(r);
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualQueueComponent) {
            QueueComponent srcComponent = ((VisualQueueComponent) src).getReferencedComponent();
            getReferencedComponent().setCapacity(srcComponent.getCapacity());
            getReferencedComponent().setInit(srcComponent.getInit());
        }
    }

}
