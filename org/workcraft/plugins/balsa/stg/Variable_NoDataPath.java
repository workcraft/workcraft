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

package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Variable;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.SimpleHandshakeBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class Variable_NoDataPath extends
		DataPathComponentStgBuilder<Variable> {

	public Variable_NoDataPath()
	{
	}

	public void buildStg(Variable component, Map<String, StgInterface> handshakes, StgInterface dpHandshake, StrictPetriBuilder builder) {
		// No action for reading needed, so we do nothing for read handshakes

		PassivePushStg write = (PassivePushStg)handshakes.get("write");
		ActiveSync latch = (ActiveSync)dpHandshake;

		//Need latch on writing
/*		StgSignal latchRq = builder.buildSignal(new SignalId(component, "latchRq"), true);
		StgSignal latchAc = builder.buildSignal(new SignalId(component, "latchAc"), false);
		OutputPlace latchReady = builder.buildPlace(1);
		builder.connect(latchReady, latchRq.getPlus());
		builder.connect(latchRq.getPlus(), latchAc.getPlus());
		builder.connect(latchAc.getPlus(), latchRq.getMinus());
		builder.connect(latchRq.getMinus(), latchAc.getMinus());
		builder.connect(latchAc.getMinus(), latchReady);*/

		builder.connect(write.go(), latch.go());
		builder.connect(latch.done(), write.dataRelease());
		builder.connect(latch.done(), write.done());
	}

	@Override
	public Handshake getDataPathHandshake(Variable component) {
		return SimpleHandshakeBuilder.getInstance().CreateActiveSync();
	}
}
