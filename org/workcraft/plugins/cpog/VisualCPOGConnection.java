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

import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public class VisualCPOGConnection extends VisualConnection
{
	CPOGConnection mathConnection;

	public VisualCPOGConnection(CPOGConnection mathConnection)
	{
		super();
		this.mathConnection = mathConnection;
	}

	@Override
	protected void initialise()
	{
		super.initialise();
		addPropertyDeclaration(new PropertyDeclaration(this, "Condition", "getCondition", "setCondition", String.class));
	}

	public VisualCPOGConnection(CPOGConnection mathConnection, VisualVertex first, VisualVertex second)
	{
		super(mathConnection, first, second);
		this.mathConnection = mathConnection;
	}

	public String getCondition()
	{
		return mathConnection.getCondition().value;
	}

	public void setCondition(String condition)
	{
		mathConnection.setCondition(new BooleanFunction(condition));
	}
}
