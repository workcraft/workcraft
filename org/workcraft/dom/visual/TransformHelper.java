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

import java.awt.geom.AffineTransform;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.NotAnAncestorException;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

public class TransformHelper {

	public static void applyTransform(Node node, AffineTransform transform) {
		if(node instanceof Movable)
			((Movable) node).applyTransform(transform);
		else
			applyTransformToChildNodes(node, transform);
	}

	public static void applyTransformToChildNodes(Node node, AffineTransform transform) {
		for(Node n: node.getChildren())
			applyTransform(n, transform);
	}

	public static AffineTransform getTransformToAncestor(Node node, Node ancestor) {
		AffineTransform t = new AffineTransform();

		while (ancestor != node) {
			Node next = node.getParent();
			if (next == null)
				throw new NotAnAncestorException();
			if(next instanceof Movable)
				t.preConcatenate(((Movable)next).getTransform());
			node = next;
		}

		return t;
	}

	public static AffineTransform getTransform(Node node1, Node node2) {
		Node parent = Hierarchy.getCommonParent(node1, node2);
		AffineTransform node1ToParent = getTransformToAncestor(node1, parent);
		AffineTransform node2ToParent = getTransformToAncestor(node2, parent);
		AffineTransform parentToNode2 = Geometry.optimisticInverse(node2ToParent);

		parentToNode2.concatenate(node1ToParent);
		return parentToNode2;
	}

	public static Touchable transform(Touchable touchable, AffineTransform transform)
	{
		return new TouchableTransformer(touchable, transform);
	}
}
