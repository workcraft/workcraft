package org.workcraft.utils;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ModelUtils {

    public static <N extends Node, C extends Connection> boolean isTransitive(Model<N, C> model, C connection) {
        N fromNode = (N) connection.getFirst();
        N toNode = (N) connection.getSecond();

        Queue<N> nextNodes = new LinkedList<>();
        for (N succNode : model.getPostset(fromNode)) {
            if (succNode != toNode) {
                nextNodes.add(succNode);
            }
        }

        Set<N> visitedNodes = new HashSet<>();
        while (!nextNodes.isEmpty()) {
            N node = nextNodes.poll();
            if (node == toNode) {
                return true;
            }
            if (!visitedNodes.contains(node)) {
                visitedNodes.add(node);
                nextNodes.addAll(model.getPostset(node));
            }
        }
        return false;
    }

    public static <N extends Node, C extends Connection> boolean hasPath(Model<N, C> model, N fromNode, N toNode) {
        Queue<N> nextNodes = new LinkedList<>(model.getPostset(fromNode));
        Set<N> visitedNodes = new HashSet<>();
        while (!nextNodes.isEmpty()) {
            N node = nextNodes.poll();
            if (node == toNode) {
                return true;
            }
            if (!visitedNodes.contains(node)) {
                visitedNodes.add(node);
                nextNodes.addAll(model.getPostset(node));
            }
        }
        return false;
    }

    public static void refreshBoundingBox(VisualModel model) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        model.draw(g2d, Decorator.Empty.INSTANCE);
    }

    public static void refreshBoundingBox(VisualModel model, Drawable drawable) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        drawable.draw(new DrawRequest() {
            @Override
            public Decoration getDecoration() {
                return Decoration.Empty.INSTANCE;
            }
            @Override
            public Graphics2D getGraphics() {
                return image.createGraphics();
            }
            @Override
            public VisualModel getModel() {
                return model;
            }
        });
    }

    public static void renameNode(VisualModel model, VisualComponent node, String name) {
        renameNode(model.getMathModel(), node.getReferencedComponent(), name);
    }

    public static void renameNode(MathModel model, MathNode node, String name) {
        MathNode existingNode = model.getNodeByReference(name);
        if (existingNode != node) {
            if (existingNode != null) {
                ReferenceManager referenceManager = model.getReferenceManager();
                if (referenceManager instanceof HierarchyReferenceManager) {
                    Node parent = node.getParent();
                    NamespaceProvider provider = parent instanceof NamespaceProvider ? (NamespaceProvider) parent : null;
                    NameManager nameManager = ((HierarchyReferenceManager) referenceManager).getNameManager(provider);
                    String derivedName = nameManager.getDerivedName(null, name);
                    model.setName(existingNode, derivedName);
                }
            }
            model.setName(node, name);
        }
    }

}
