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

package org.workcraft.dom.math;

import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Hierarchy;

public abstract class AbstractMathModel extends AbstractModel implements MathModel {

	public AbstractMathModel() {
		this (null, null);
	}

	public AbstractMathModel (Container root) {
		this (root, null);
	}

	public AbstractMathModel(Container root, ReferenceManager referenceManager) {
		super((root == null) ? new MathGroup() : root, referenceManager);

		new DefaultHangingConnectionRemover(this, "Math").attach(getRoot());
	}

	public Connection connect(Node first, Node second)
			throws InvalidConnectionException {

		validateConnection (first, second);

		Connection con =  new MathConnection((MathNode)first, (MathNode)second);

		Container group =
			Hierarchy.getNearestAncestor(
					Hierarchy.getCommonParent(first, second),
					Container.class);

		group.add(con);

		return con;
	}
}