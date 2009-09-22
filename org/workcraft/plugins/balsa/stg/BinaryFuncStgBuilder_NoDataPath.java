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
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

public class BinaryFuncStgBuilder_NoDataPath extends
		ComponentStgBuilder<BinaryFunc> {

	public BinaryFuncStgBuilder_NoDataPath()
	{
	}

	public void buildStg(BinaryFunc component, Map<String, StgHandshake> handshakes, StgBuilder builder) {
		ActivePullStg inpA = (ActivePullStg)handshakes.get("inpA");
		ActivePullStg inpB = (ActivePullStg)handshakes.get("inpB");
		PassivePullStg out = (PassivePullStg)handshakes.get("out");

		//Data path handshaking
		StgSignal dpReq = builder.buildSignal(new SignalId(component, "dpReq"), true);
		StgSignal dpAck = builder.buildSignal(new SignalId(component, "dpAck"), false);
		builder.addConnection(dpReq.getPlus(), dpAck.getPlus());
		builder.addConnection(dpAck.getPlus(), dpReq.getMinus());
		builder.addConnection(dpReq.getMinus(), dpAck.getMinus());
		StgPlace dpStart = builder.buildPlace(1);
		builder.addConnection(dpAck.getMinus(), dpStart);
		builder.addConnection(dpStart, dpReq.getPlus());

		// Read inputs
		builder.addConnection(out.getActivate(), inpA.getActivate());
		builder.addConnection(out.getActivate(), inpB.getActivate());

		// When inputs are read, start computing output
		builder.addConnection(inpA.getDataReady(), dpReq.getPlus());
		builder.addConnection(inpB.getDataReady(), dpReq.getPlus());

		// When output is computed, report to the output
		builder.addConnection(dpAck.getPlus(), out.getDataReady());
		builder.addConnection(dpAck.getPlus(), out.getDataReady());

		// When output releases data, release inputs
		builder.addConnection(out.getDataRelease(), inpA.getDataRelease());
		builder.addConnection(out.getDataRelease(), inpB.getDataRelease());
	}
}
