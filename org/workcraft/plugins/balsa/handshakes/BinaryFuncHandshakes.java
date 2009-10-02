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

package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.BinaryOperator;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class BinaryFuncHandshakes extends HandshakeMaker<BinaryFunc> {

	@Override
	protected void fillHandshakes(BinaryFunc component, Map<String, Handshake> handshakes) {
		boolean comparison = isComparison(component.getOp());
		//boolean bool = isBoolean(component.getOp());

		handshakes.put("inpA", builder.CreateActivePull(component.getInputAWidth()));
		handshakes.put("inpB", builder.CreateActivePull(component.getInputAWidth()));
		if(comparison)
			handshakes.put("out", builder.CreatePassiveFullDataPull(component.getOutputWidth()));
		else
			handshakes.put("out", builder.CreatePassivePull(component.getOutputWidth()));
	}

	private static boolean isComparison(BinaryOperator op) {
		return
		op == BinaryOperator.EQUALS ||
		op == BinaryOperator.NOT_EQUALS ||
		op == BinaryOperator.GREATER_OR_EQUALS ||
		op == BinaryOperator.GREATER_THAN ||
		op == BinaryOperator.LESS_OR_EQUALS ||
		op == BinaryOperator.LESS_THAN;
	}
/*
	private static boolean isBoolean(BinaryOperator op) {
		return
		op == BinaryOperator.AND ||
		op == BinaryOperator.OR ||
		op == BinaryOperator.GREATER_THAN ||
		op == BinaryOperator.LESS_OR_EQUALS ||
		op == BinaryOperator.LESS_THAN;
	}
*/
}
