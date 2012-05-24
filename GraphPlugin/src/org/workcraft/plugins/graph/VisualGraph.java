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

package org.workcraft.plugins.graph;

import java.awt.geom.Point2D;

import org.workcraft.annotations.DefaultCreateButtons;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.Hierarchy;

@DefaultCreateButtons ( { Vertex.class } )
public class VisualGraph extends AbstractVisualModel {
	private Graph graph;

	public VisualGraph(Graph model) throws VisualModelInstantiationException {
		super(model);
		this.graph = model;
	}

	public VisualGraph(Graph model, VisualGroup root) {
		super(model, root);

		if (root == null) {
			try {
				createDefaultFlatStructure();
			} catch (NodeCreationException e) {
				throw new RuntimeException(e);
			}
		}
		this.graph = model;
	}


	@Override
	public void validateConnection(Node first, Node second) throws InvalidConnectionException {
		//if (first==second) {
//			throw new InvalidConnectionException ("Self loops are not allowed");
		//}
	}

	@Override
	public void connect(Node first, Node second) throws InvalidConnectionException {
		validateConnection(first, second);

		VisualComponent c1 = (VisualComponent) first;
		VisualComponent c2 = (VisualComponent) second;

		MathConnection con = (MathConnection) graph.connect(c1.getReferencedComponent(), c2.getReferencedComponent());
		VisualConnection ret = new VisualConnection(con, c1, c2);
		Hierarchy.getNearestContainer(c1, c2).add(ret);
		if (c1 == c2) {
			ret.setConnectionType(ConnectionType.BEZIER);
			Bezier b = (Bezier) ret.getGraphic();
			BezierControlPoint[] cp = b.getControlPoints();
			cp[0].setPosition(new Point2D.Double(c1.getX()-1.0, c1.getY()+2.0));
			cp[1].setPosition(new Point2D.Double(c1.getX()+1.0, c1.getY()+2.0));
		}
	}
}