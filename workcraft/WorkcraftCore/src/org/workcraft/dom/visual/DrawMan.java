package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;

final class DrawMan {
    private final Graphics2D graphics;
    private final Decorator decorator;
    private final VisualModel model;

    private DrawMan(VisualModel model, Graphics2D graphics, Decorator decorator) {
        this.model = model;
        this.graphics = graphics;
        this.decorator = decorator;
    }

    private void transformAndDraw(Decoration decoration, Movable node) {
        graphics.transform(node.getTransform());
        simpleDraw(decoration, node);
    }

    public static void draw(VisualModel model, Graphics2D graphics, Decorator decorator, Node node) {
        new DrawMan(model, graphics, decorator).draw(Decoration.Empty.INSTANCE, node);
    }

    private void draw(Decoration currentDecoration, Node node) {
        Decoration decoration = decorator.getDecoration(node);
        if (decoration == null) {
            decoration = currentDecoration;
        }
        if ((node instanceof Hidable) && ((Hidable) node).isHidden()) {
            return;
        }
        AffineTransform oldTransform = graphics.getTransform();
        if (node instanceof Movable) {
            transformAndDraw(decoration, (Movable) node);
        } else {
            simpleDraw(decoration, node);
        }
        graphics.setTransform(oldTransform);
    }

    private void simpleDraw(final Decoration decoration, Node node) {
        AffineTransform oldTransform = graphics.getTransform();
        if (node instanceof VisualConnection) {
            // Draw connection children first
            drawChildren(decoration, node);
            drawNode(decoration, node);
        } else {
            // Draw node children last
            drawNode(decoration, node);
            drawChildren(decoration, node);
        }
        graphics.setTransform(oldTransform);
    }

    private void drawNode(final Decoration decoration, Node node) {
        if (node instanceof Drawable) {
            Drawable drawableNode = (Drawable) node;
            drawableNode.draw(new DrawRequest() {
                @Override
                public Decoration getDecoration() {
                    return decoration;
                }
                @Override
                public Graphics2D getGraphics() {
                    return graphics;
                }
                @Override
                public VisualModel getModel() {
                    return model;
                }
            });
        }
    }

    private void drawChildren(final Decoration decoration, Node node) {
        // A collapsed node does not draw its contents, unless we are inside this node
        boolean isCollapsed = (node instanceof Collapsible) && ((Collapsible) node).getIsCollapsed();
        boolean isInsideCollapsed = isCollapsed && ((Collapsible) node).isCurrentLevelInside();
        if (isInsideCollapsed || !isCollapsed) {
            // Copy the collection of children nodes before drawing in order to avoid concurrent modification exception
            ArrayList<Node> children = new ArrayList<>(node.getChildren());
            // First draw nodes
            for (Node childNode : children) {
                if (!(childNode instanceof VisualConnection)) {
                    draw(decoration, childNode);
                }
            }
            // Then draw connections
            for (Node childNode : children) {
                if (childNode instanceof VisualConnection) {
                    draw(decoration, childNode);
                }
            }
        }
    }

}
