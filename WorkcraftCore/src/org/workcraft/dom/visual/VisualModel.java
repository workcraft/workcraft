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
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.observation.ObservableState;

public interface VisualModel extends Model, ObservableState {
	public void draw (Graphics2D g, Decorator decorator);

	public Container getCurrentLevel();
	public MathModel getMathModel();

	public Collection<Node> getSelection();

	public void connect(Node first, Node second) throws InvalidConnectionException;
	public void validateConnection(Node first, Node second) throws InvalidConnectionException;

	public void selectAll();
	public void selectNone();
	public void selectInverse();
	public void select(Node node);
	public void select(Collection<Node> nodes);
	public void addToSelection (Collection<Node> nodes);
	public void addToSelection (Node node);
	public void removeFromSelection (Node node);
	public void removeFromSelection (Collection<Node> nodes);
	public void deleteSelection();
	public void groupSelection();
	public void ungroupSelection();

	public void setCurrentLevel (Container group);

	public Collection<Node> boxHitTest(Point2D p1, Point2D p2);
}