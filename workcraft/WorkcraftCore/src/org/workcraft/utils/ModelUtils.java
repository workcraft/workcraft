package org.workcraft.utils;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
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

}
