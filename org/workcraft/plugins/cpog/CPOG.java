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

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.stg.STGReferenceManager;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@DisplayName("Conditional Partial Order Graph")
@VisualClass("org.workcraft.plugins.cpog.VisualCPOG")
public class CPOG extends AbstractMathModel
{

	private STGReferenceManager referenceManager;

	public CPOG()
	{
		this(null, null);
	}

	public CPOG(Container root)
	{
		this(root, null);
	}

	public CPOG(Container root, References refs)
	{
		super(root, new STGReferenceManager(refs));
		referenceManager = (STGReferenceManager) getReferenceManager();
	}

	public String getName(Vertex vertex)
	{
		return referenceManager.getName(vertex);
	}

	public void setName(Vertex vertex, String name)
	{
		referenceManager.setName(vertex, name);
	}

	public Arc connect(Vertex first, Vertex second) throws InvalidConnectionException
	{
		Arc con = new Arc(first, second);
		getRoot().add(con);
		return con;
	}

	public DynamicVariableConnection connect(Vertex first, Variable second) throws InvalidConnectionException
	{
		DynamicVariableConnection con = new DynamicVariableConnection(first, second);
		getRoot().add(con);
		return con;
	}

	public Collection<Variable> getVariables() {
		return Hierarchy.getChildrenOfType(getRoot(), Variable.class);
	}
}