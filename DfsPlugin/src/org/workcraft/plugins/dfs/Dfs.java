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

package org.workcraft.plugins.dfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@DisplayName ("Dataflow Structure")
@VisualClass (org.workcraft.plugins.dfs.VisualDfs.class)
public class Dfs extends AbstractMathModel {

	public Dfs() {
		this(new MathGroup(), null);
	}

	public Dfs(Container root, References refs) {

		super(root, new HierarchicalUniqueNameReferenceManager(refs, new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if ((arg instanceof Logic) || (arg instanceof CounterflowLogic))
					return "l";
				if ((arg instanceof Register) || (arg instanceof CounterflowRegister))
					return "r";
				if ((arg instanceof ControlRegister))
					return "c";
				if ((arg instanceof PushRegister) || (arg instanceof PopRegister))
					return "p";
				if (arg instanceof Connection)
					return "con";
				return "node";
			}
		}));
	}

	public MathConnection connect(Node first, Node second) {
		MathConnection con = new MathConnection((MathNode)first, (MathNode)second);
		Hierarchy.getNearestContainer(first, second).add(con);
		return con;
	}

	public ControlConnection controlConnect(Node first, Node second) {
		ControlConnection con = new ControlConnection((MathNode)first, (MathNode)second);
		Hierarchy.getNearestContainer(first, second).add(con);
		return con;
	}

	@Override
	public Properties getProperties(Node node) {
		if (node != null) {
			return Properties.Mix.from(new NamePropertyDescriptor(this, node));
		}
		return null;
	}

}
