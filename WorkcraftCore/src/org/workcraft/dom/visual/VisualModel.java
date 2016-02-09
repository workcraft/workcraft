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
import java.awt.geom.Point2D;
import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.observation.ObservableState;
import org.workcraft.plugins.layout.AbstractLayoutTool;

public interface VisualModel extends Model, ObservableState {
    void createDefaultFlatStructure() throws NodeCreationException;
    void draw(Graphics2D g, Decorator decorator);

    void setCurrentLevel(Container group);
    Container getCurrentLevel();
    MathModel getMathModel();

    String getNodeMathReference(Node node);
    String getMathName(Node node);
    void setMathName(Node node, String name);

    void validateConnection(Node first, Node second) throws InvalidConnectionException;
    VisualConnection connect(Node first, Node second) throws InvalidConnectionException;
    VisualConnection connect(Node first, Node second, MathConnection connection) throws InvalidConnectionException;

    void validateUndirectedConnection(Node first, Node second) throws InvalidConnectionException;
    VisualConnection connectUndirected(Node first, Node second) throws InvalidConnectionException;

    <T extends VisualComponent> T createVisualComponent(MathNode refNode, Container container, Class<T> type);
    <T extends VisualComponent> T getVisualComponent(MathNode refNode, Class<T> type);

    <T extends VisualReplica> T createVisualReplica(VisualComponent master, Container container, Class<T> type);

    void selectAll();
    void selectNone();
    void selectInverse();
    void select(Node node);
    void select(Collection<Node> nodes);
    void addToSelection(Node node);
    void addToSelection(Collection<Node> nodes);
    void removeFromSelection(Node node);
    void removeFromSelection(Collection<Node> nodes);
    void deleteSelection();

    Collection<Node> getSelection();

    boolean isGroupable(Node node);
    VisualGroup groupSelection();
    VisualPage groupPageSelection();
    void ungroupSelection();

    Collection<Node> boxHitTest(Point2D p1, Point2D p2);

    void setTemplateNode(VisualNode node);
    VisualNode getTemplateNode();
    AbstractLayoutTool getBestLayoutTool();

}
