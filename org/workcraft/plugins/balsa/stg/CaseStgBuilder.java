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

import org.workcraft.plugins.balsa.components.Case;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveFullDataPullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class CaseStgBuilder extends DataPathComponentStgBuilder<Case> {

	@Override
	public void buildStg(Case component, Map<String, StgInterface> handshakes, StgInterface dpHandshake, StrictPetriBuilder builder) {

		PassivePushStg in = (PassivePushStg)handshakes.get("inp");
		ActiveFullDataPullStg dp = (ActiveFullDataPullStg)dpHandshake;

		//OutputPlace guardChangeAllowed = builder.buildPlace(1);

		//TODO: possibly move commented code to the FullDataPull implementation
	/*	StgSignal dataSignal = builder.buildSignal(new SignalId(component, "dp"), false);
		final OutputPlace dataOne = builder.buildPlace();
		final OutputPlace dataZero = builder.buildPlace(1);
		builder.connect(dataOne, dataSignal.getMinus());
		builder.connect(dataSignal.getMinus(), dataZero);
		builder.connect(dataZero, dataSignal.getPlus());
		builder.connect(dataSignal.getPlus(), dataOne);

		builder.connect(in.dataRelease(), guardChangeAllowed);
		//TODO: Externalise the enviromnent specification
		builder.connect(guardChangeAllowed, (OutputEvent)in.go());
		builder.addReadArc(guardChangeAllowed, dataSignal.getMinus());
		builder.addReadArc(guardChangeAllowed, dataSignal.getPlus());*/

		StgPlace done = builder.buildPlace(0);

		builder.connect(in.go(), dp.go());

		for(int i=0;i<component.getOutputCount();i++)
		{
			ActiveSync activateOut = (ActiveSync)handshakes.get("activateOut"+i);

			builder.connect(dp.result().get(i), activateOut.go());
			builder.connect(dp.result().get(i), in.dataRelease());
			builder.connect(activateOut.done(), done);
		}

		builder.connect(done, in.done());
	}

	@Override
	public Handshake getDataPathHandshake() {
		throw new RuntimeException("Not implemented!");// TODO Implement
	}
}
