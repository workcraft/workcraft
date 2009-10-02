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

import org.workcraft.plugins.balsa.components.CallMux;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.SimpleHandshakeBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveFullDataPushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class CallMux_NoDataPath extends
		DataPathComponentStgBuilder<CallMux> {

	public CallMux_NoDataPath()
	{
	}

	public void buildStg(CallMux component, Map<String, StgInterface> handshakes, StgInterface dpHandshake, StrictPetriBuilder builder) {
		ActivePushStg out = (ActivePushStg)handshakes.get("out");
		ActiveFullDataPushStg sel = (ActiveFullDataPushStg)dpHandshake;


		/* TODO: move commented code to FullDataPush implementation
		  StgSignal selAc = builder.buildSignal(new SignalId(component, "selAq"), false);

		OutputPlace selRequested = builder.buildPlace();
		OutputPlace selRtz = builder.buildPlace();
		OutputPlace selAcknowledged = builder.buildPlace();
		OutputPlace selAckReset = builder.buildPlace(1);
		builder.connect(selAc.getPlus(), selAcknowledged);
		builder.connect(selAc.getMinus(), selAckReset);

		builder.connect(selRequested, selAc.getPlus());
		builder.connect(selRtz, selAc.getMinus()); */

		StgPlace releaseInput = builder.buildPlace(0);
		//OutputPlace releaseSel = builder.buildPlace();
		for(int i=0;i<component.getInputCount();i++)
		{
			PassivePushStg in = (PassivePushStg)handshakes.get("inp"+i);

			//StgSignal selRq = builder.buildSignal(new SignalId(component, "sel"+i+"Rq"), true);
			//OutputPlace selReady = builder.buildPlace(1);
			//OutputPlace selActive = builder.buildPlace();
			//builder.connect(selReady, selRq.getPlus());
			//builder.connect(selRq.getPlus(), selActive);
			//builder.connect(selActive, selRq.getMinus());
			//builder.connect(selRq.getMinus(), selReady);

			//builder.connect(selRq.getPlus(), selRequested);
			//builder.connect(selAcknowledged, selRq.getMinus());
			//builder.connect(selRq.getMinus(), selRtz);
			//builder.connect(selAckReset, selRq.getPlus());

			builder.connect(in.go(), sel.data().get(i));
			//builder.connect(releaseSel, selRq.getMinus());
			builder.connect(releaseInput, in.dataRelease());
		}

		builder.connect(sel.done(), out.go());

		builder.connect(out.done(), releaseInput);
		//builder.connect(out.done(), releaseSel);
	}

	@Override
	public void buildEnvironment(CallMux component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder)
	{
		StgPlace ready = builder.buildPlace(1);

		for(int i=0;i<component.getInputCount();i++)
		{
			ActivePushStg in = (ActivePushStg)handshakes.get("inp"+i);

			builder.connect(ready, (OutputEvent)in.go());
			builder.connect(in.done(), ready);
		}
	}

	@Override
	public Handshake getDataPathHandshake(CallMux component) {
		return SimpleHandshakeBuilder.getInstance().CreateActiveFullDataPush(component.getWidth()); // TODO: remove stupidity
	}
}
