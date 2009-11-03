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

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.SimpleHandshakeBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class BinaryFuncStgBuilder_Arithmetic extends
		DataPathComponentStgBuilder<BinaryFunc> {

	public BinaryFuncStgBuilder_Arithmetic()
	{
	}

	public void buildStg(BinaryFunc component, Map<String, StgInterface> handshakes, StgInterface dataPath, StrictPetriBuilder builder) {

		ActivePullStg inpA = (ActivePullStg)handshakes.get("inpA");
		ActivePullStg inpB = (ActivePullStg)handshakes.get("inpB");
		PassivePullStg out = (PassivePullStg)handshakes.get("out");
		ActiveSync dp = (ActiveSync)dataPath;

		// Read inputs
		builder.connect(out.go(), inpA.go());
		builder.connect(out.go(), inpB.go());

		// When inputs are read, start computing output
		builder.connect(inpA.done(), dp.go());
		builder.connect(inpB.done(), dp.go());

		// When output is computed, report to the output
		builder.connect(dp.done(), out.done());

		// When output releases data, release inputs
		builder.connect(out.dataRelease(), inpA.dataRelease());
		builder.connect(out.dataRelease(), inpB.dataRelease());
	}

	@Override
	public Handshake getDataPathHandshake(BinaryFunc component) {
		return SimpleHandshakeBuilder.getInstance().CreateActiveSync(); //TODO: Remove stupidity
	}
}
