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

package org.workcraft.plugins.sdfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.UniqueNameReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@DisplayName ("Static Data Flow Structure")
@VisualClass (org.workcraft.plugins.sdfs.VisualSDFS.class)
public class SDFS extends AbstractMathModel {

	public SDFS() {
		this(null, null);
	}

	public SDFS(Container root, References refs) {
		super(root, new UniqueNameReferenceManager(refs, new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof Logic)
					return "l";
				if (arg instanceof Register)
					return "r";
				if (arg instanceof Connection)
					return "con";
				return "node";
			}
		}));
	}

	public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
		MathConnection con = new MathConnection((MathNode)first, (MathNode)second);
		Hierarchy.getNearestContainer(first, second).add(con);
		return con;
	}

	@Override
	public Properties getProperties(Node node) {
		return Properties.Mix.from(new NamePropertyDescriptor(this, node));
	}

	public String getName(Node node) {
		return ((UniqueNameReferenceManager)getReferenceManager()).getName(node);
	}

	public void setName(Node node, String name) {
		((UniqueNameReferenceManager)getReferenceManager()).setName(node, name);
	}
}
