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

import org.workcraft.plugins.balsa.components.Fetch;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class FetchStgBuilder extends
		ComponentStgBuilder<Fetch> {

	public FetchStgBuilder()
	{
	}

	public void buildStg(Fetch component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder) {
		// No data path handshakes needed - direct wires there

		PassiveSync activate = (PassiveSync)handshakes.get("activate");
		ActivePullStg in = (ActivePullStg)handshakes.get("inp");
		ActivePushStg out = (ActivePushStg)handshakes.get("out");

		//First, make sure input sets up the correct data
		builder.connect(activate.go(), in.go());
		//After it does, activate output
		builder.connect(in.done(), out.go());
		//After out acknowledges, RTZ input and acknowledge back
		builder.connect(out.dataRelease(), in.dataRelease());
		builder.connect(out.done(), activate.done());
	}
}
