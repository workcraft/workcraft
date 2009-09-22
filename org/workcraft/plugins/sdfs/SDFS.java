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
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;

@DisplayName ("Static Data Flow Structure")
@VisualClass ("org.workcraft.plugins.sdfs.VisualSDFS")
public class SDFS extends AbstractMathModel {

	public SDFS() {
		super(null);

		new DefaultHangingConnectionRemover(this, "SDFS").attach(getRoot());
	}

	public void validate() throws ModelValidationException {
	}


	public void validateConnection(Connection connection)	throws InvalidConnectionException {
	}

	final public Register createRegister() {
		Register newRegister = new Register();
		add(newRegister);
		return newRegister;
	}

	final public Logic createLogic() {
		Logic newLogic = new Logic();
		add(newLogic);
		return newLogic;
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
		if (first == second)
			throw new InvalidConnectionException ("Self-loops are not allowed");
	}
}
