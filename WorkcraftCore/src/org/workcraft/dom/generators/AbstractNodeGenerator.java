package org.workcraft.dom.generators;

import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.exceptions.NodeCreationException;

import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public abstract class AbstractNodeGenerator implements NodeGenerator {

    @Override
    public VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException {
        Container visualContainer = model.getCurrentLevel();
        Container mathContainer = model.getMathModel().getRoot();

        Container visualNamespace = visualContainer;

        while (visualNamespace instanceof VisualGroup) {
            visualNamespace = (Container) visualNamespace.getParent();
        }

        //TODO: this will brake at some point
        if (visualNamespace != null) {
            mathContainer = (Container) ((VisualComponent) visualNamespace).getReferencedComponent();
        }

        MathNode mn = createMathNode();
        mathContainer.add(mn);

        VisualNode vc = createVisualNode(mn);
        visualContainer.add(vc);

        if (vc instanceof Movable) {
            AffineTransform transform = TransformHelper.getTransform(model.getRoot(), vc);
            Point2D transformed = new Point2D.Double();
            transform.transform(where, transformed);
            MovableHelper.translate((Movable) vc, transformed.getX(), transformed.getY());
        }
        return vc;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getText() {
        return "Click to create a " + getLabel();
    }

    @Override
    public int getHotKeyCode() {
        return -1; // undefined hotkey
    }

}
