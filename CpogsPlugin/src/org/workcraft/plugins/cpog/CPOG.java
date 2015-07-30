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

import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

public class CPOG extends AbstractMathModel {

	public CPOG() {
		this(null, null);
	}

	public CPOG(Container root, References refs) {
		super(root, new HierarchicalUniqueNameReferenceManager(refs) {
			@Override
			public String getPrefix(Node node) {
				if (node instanceof Vertex) return "v";
				if (node instanceof Variable) return "var";
				if (node instanceof RhoClause) return "rho";
				return super.getPrefix(node);
			}
		});

		new ConditionConsistencySupervisor(this).attach(getRoot());
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

	@Override
	public ModelProperties getProperties(Node node) {
		ModelProperties properties = super.getProperties(node);
		if (node != null)  {
			properties.removeByName("Name");
		}
		return properties;
	}

}
