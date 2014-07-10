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

package org.workcraft.plugins.circuit;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;


@DisplayName("FunctionContact")
@VisualClass(org.workcraft.plugins.circuit.VisualFunctionContact.class)

public class FunctionContact extends Contact {
	private BooleanFormula setFunction = null;
	private BooleanFormula resetFunction = null;

	public FunctionContact(IOType ioType) {
		super(ioType);
	}

	public FunctionContact() {
		super();
	}

	public BooleanFormula getSetFunction() {
		return setFunction;
	}

	public void setSetFunction(BooleanFormula setFunction) {
		this.setFunction = setFunction;
		sendNotification(new PropertyChangedEvent(this, "setFunction"));
	}

	public BooleanFormula getResetFunction() {
		return resetFunction;
	}

	public void setResetFunction(BooleanFormula resetFunction) {
		this.resetFunction = resetFunction;
		sendNotification(new PropertyChangedEvent(this, "resetFunction"));
	}

}
