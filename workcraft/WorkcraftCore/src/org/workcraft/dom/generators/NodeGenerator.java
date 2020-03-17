package org.workcraft.dom.generators;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;

import javax.swing.*;
import java.awt.geom.Point2D;

public interface NodeGenerator {
    Icon getIcon();
    String getLabel();
    String getText();
    int getHotKeyCode();
    MathNode createMathNode() throws NodeCreationException;
    VisualNode createVisualNode(MathNode mathNode) throws NodeCreationException;
    VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException;
}
