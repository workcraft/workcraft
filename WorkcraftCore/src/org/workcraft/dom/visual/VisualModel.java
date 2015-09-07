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
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.observation.ObservableState;
import org.workcraft.plugins.layout.AbstractLayoutTool;

public interface VisualModel extends Model, ObservableState {
	public void draw(Graphics2D g, Decorator decorator);

	public void setCurrentLevel(Container group);
	public Container getCurrentLevel();
	public MathModel getMathModel();

	public String getNodeMathReference(Node node);
	public String getMathName(Node node);
	public void setMathName(Node node, String name);

	public void validateConnection(Node first, Node second) throws InvalidConnectionException;
	public VisualConnection connect(Node first, Node second) throws InvalidConnectionException;
	public VisualConnection connect(Node first, Node second, MathConnection connection) throws InvalidConnectionException;

	public void validateUndirectedConnection(Node first, Node second) throws InvalidConnectionException;
	public VisualConnection connectUndirected(Node first, Node second) throws InvalidConnectionException;

	public <T extends VisualComponent> T createVisualComponent(MathNode refNode, Container container, Class<T> type);
	public <T extends VisualComponent> T getVisualComponent(MathNode refNode, Class<T> type);

	public void selectAll();
	public void selectNone();
	public void selectInverse();
	public void select(Node node);
	public void select(Collection<Node> nodes);
	public void addToSelection(Node node);
	public void addToSelection(Collection<Node> nodes);
	public void removeFromSelection(Node node);
	public void removeFromSelection(Collection<Node> nodes);
	public void deleteSelection();

	public Collection<Node> getSelection();

	public boolean isGroupable(Node node);
	public VisualGroup groupSelection();
	public VisualPage groupPageSelection();
	public void ungroupSelection();

	public Collection<Node> boxHitTest(Point2D p1, Point2D p2);

	public void setTemplateNode(VisualNode node);
	public VisualNode getTemplateNode();
	public AbstractLayoutTool getBestLayoutTool();


}
