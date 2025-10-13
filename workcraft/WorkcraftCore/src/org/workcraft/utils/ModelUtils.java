package org.workcraft.utils;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathConnection;
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
import org.workcraft.types.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class ModelUtils {

    private static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

    @SuppressWarnings("unchecked")
    public static <N extends Node, C extends Connection> boolean isTransitive(Model<N, C> model, C connection) {
        N fromNode = (N) connection.getFirst();
        N toNode = (N) connection.getSecond();

        Queue<N> nextNodes = new ArrayDeque<>();
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
        Queue<N> nextNodes = new ArrayDeque<>(model.getPostset(fromNode));
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

    public static void setNameRenameClashes(VisualModel model, VisualComponent node, String name) {
        setNameRenameClashes(model.getMathModel(), node.getReferencedComponent(), name);
    }

    public static void setNameRenameClashes(MathModel model, MathNode node, String name) {
        if (node != null) {
            MathNode existingNode = model.getNodeByReference(name);
            if (existingNode != node) {
                setNameOrDerivedName(model, existingNode, name);
                model.setName(node, name);
            }
        }
    }

    public static void setNameOrDerivedName(VisualModel model, VisualComponent node, String name) {
        setNameOrDerivedName(model.getMathModel(), node.getReferencedComponent(), name);
    }

    public static void setNameOrDerivedName(MathModel model, MathNode node, String name) {
        if (node != null) {
            String derivedName = getNextAvailableDerivedNameOrNull(model, node, name);
            if (derivedName != null) {
                model.setName(node, derivedName);
            }
        }
    }

    public static String getNextAvailableDerivedNameOrNull(MathModel model, Node node, String name) {
        ReferenceManager referenceManager = model.getReferenceManager();
        if (referenceManager instanceof HierarchyReferenceManager hierarchyReferenceManager) {
            Node parent = node.getParent();
            NamespaceProvider provider = (parent instanceof NamespaceProvider) ? (NamespaceProvider) parent : null;
            NameManager nameManager = hierarchyReferenceManager.getNameManager(provider);
            return nameManager.getDerivedName(null, name);
        }
        return null;
    }

    public static String getConnectionDisplayName(MathModel model, MathConnection arc) {
        return ModelUtils.getConnectionDisplayName(model, arc.getFirst(), arc.getSecond());
    }

    public static String getConnectionDisplayName(MathModel model, MathNode srcNode, MathNode dstNode) {
        return getConnectionDisplayName(model.getNodeReference(srcNode), model.getNodeReference(dstNode));
    }

    public static String getConnectionDisplayName(Pair<String, String> nodeRefPair) {
        return getConnectionDisplayName(nodeRefPair.getFirst(), nodeRefPair.getSecond());
    }

    public static String getConnectionDisplayName(String srcNodeRef, String dstNodeRef) {
        return srcNodeRef + RIGHT_ARROW_SYMBOL + dstNodeRef;
    }

}
