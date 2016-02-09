/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;


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

    public void draw(Decoration currentDecoration, Node node) {
        Decoration decoration = decorator.getDecoration(node);
        if (decoration == null) {
            decoration = currentDecoration;
        }
        if ((node instanceof Hidable) && ((Hidable)node).isHidden()) {
            return;
        }
        AffineTransform oldTransform = graphics.getTransform();
        if (node instanceof Movable) {
            transformAndDraw(decoration, (Movable)node);
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
            Drawable drawableNode = (Drawable)node;
            drawableNode.draw(new DrawRequest(){
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
        boolean isCollapsed = (node instanceof Collapsible) && ((Collapsible)node).getIsCollapsed();
        boolean isInsideCollapsed = isCollapsed && ((Collapsible)node).isCurrentLevelInside();
        if (isInsideCollapsed || !isCollapsed) {
            // First draw nodes
            for (Node childNode : node.getChildren()) {
                if ( !(childNode instanceof VisualConnection) ) {
                    draw(decoration, childNode);
                }
            }
            // Then draw connections
            for (Node childNode : node.getChildren()) {
                if (childNode instanceof VisualConnection) {
                    draw(decoration, childNode);
                }
            }
        }
    }

}
