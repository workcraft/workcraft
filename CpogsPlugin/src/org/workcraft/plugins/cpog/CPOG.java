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

package org.workcraft.plugins.cpog;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.UniqueNameReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class CPOG extends AbstractMathModel {

	public CPOG() {
		this(new MathGroup(), null);
	}

	public CPOG(Container root, References refs) {
		super(root, new HierarchicalUniqueNameReferenceManager((NamespaceProvider) root, refs, new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof Vertex)
					return "v";
				if (arg instanceof Variable)
					return "var";
				if ((arg instanceof RhoClause))
					return "rho";
				if (arg instanceof Connection)
					return "con";
				return "node";
			}
		}));
		// update all vertex conditions when a variable is removed
		new HierarchySupervisor() {
			@Override
			public void handleEvent(HierarchyEvent e) {
				if (e instanceof NodesDeletingEvent) {
					for (Node node: e.getAffectedNodes()) {
						if (node instanceof Variable) {
							final Variable var = (Variable)node;
							for (Vertex v: new ArrayList<Vertex>(getVertices())) {
					    		v.setCondition(BooleanReplacer.replace(v.getCondition(), var, Zero.instance()));
							}
						}
					}
				}
			}
		}.attach(getRoot());

	}

	public Arc connect(Vertex first, Vertex second) {
		Arc con = new Arc(first, second);
		getRoot().add(con);
		return con;
	}

	public DynamicVariableConnection connect(Vertex first, Variable second) throws InvalidConnectionException {
		DynamicVariableConnection con = new DynamicVariableConnection(first, second);
		getRoot().add(con);
		return con;
	}

	public Collection<Variable> getVariables() {
		return Hierarchy.getChildrenOfType(getRoot(), Variable.class);
	}

	public Collection<Vertex> getVertices() {
		return Hierarchy.getChildrenOfType(getRoot(), Vertex.class);
	}

}
