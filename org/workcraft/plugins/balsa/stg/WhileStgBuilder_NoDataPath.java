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

import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;

public class WhileStgBuilder_NoDataPath extends ComponentStgBuilder<While> {
	static interface WhileStgHandshakes
	{
		public ActivePullStg getGuard();
		public PassiveSyncStg getActivate();
		public ActiveSyncStg getActivateOut();
	}

	class WhileStgHandshakesFromCollection implements WhileStgHandshakes
	{
		private final Map<String, StgHandshake> map;

		public WhileStgHandshakesFromCollection(Map<String, StgHandshake> map)
		{
			this.map = map;
		}

		public PassiveSyncStg getActivate() {
			return (PassiveSyncStg)map.get("activate");
		}

		public ActiveSyncStg getActivateOut() {
			return (ActiveSyncStg)map.get("activateOut");
		}

		public ActivePullStg getGuard() {
			return (ActivePullStg)map.get("guard");
		}
	}

	static class WhileInternalStgBuilder
	{
		public static void buildStg(While component, WhileStgHandshakes handshakes, StgBuilder builder)
		{
			StgPlace activated = builder.buildPlace();
			StgPlace dataReady = builder.buildPlace();

			PassiveSyncStg activate = handshakes.getActivate();
			ActiveSyncStg activateOut = handshakes.getActivateOut();
			ActivePullStg guard = handshakes.getGuard();

			StgPlace guardChangeAllowed = builder.buildPlace(1);

			StgSignal guardSignal = builder.buildSignal(new SignalId(component, "dp"), false);
			final StgPlace guardOne = builder.buildPlace();
			final StgPlace guardZero = builder.buildPlace(1);
			builder.addConnection(guardOne, guardSignal.getMinus());
			builder.addConnection(guardSignal.getMinus(), guardZero);
			builder.addConnection(guardZero, guardSignal.getPlus());
			builder.addConnection(guardSignal.getPlus(), guardOne);

			builder.addConnection(guard.getDataRelease(), guardChangeAllowed);
			//TODO: Move environment specification somewhere else
			builder.addConnection(guardChangeAllowed, (StgTransition)guard.getDataReady());
			builder.addReadArc(guardChangeAllowed, guardSignal.getMinus());
			builder.addReadArc(guardChangeAllowed, guardSignal.getPlus());

			// Call guard
			builder.addConnection(activate.getActivate(), activated);
			builder.addConnection(activated, guard.getActivate());
			builder.addConnection(guard.getDataReady(), dataReady);

			// Activate and repeatedly call guard
			builder.addConnection(dataReady, activateOut.getActivate());
			builder.addReadArc(guardOne, activateOut.getActivate());
			builder.addConnection(activateOut.getDeactivate(), activated);

			// Return
			builder.addConnection(dataReady, activate.getDeactivate());
			builder.addReadArc(guardZero, activate.getDeactivate());

			StgTransition dataRelease = guard.getDataRelease();

			StgPlace releaseAllowed = builder.buildPlace();
			builder.addConnection(activateOut.getActivate(), releaseAllowed);
			builder.addConnection(activate.getDeactivate(), releaseAllowed);
			builder.addConnection(releaseAllowed, dataRelease);
		}
	}

	public void buildStg(While component, Map<String, StgHandshake> handshakes, StgBuilder builder)
	{
		WhileInternalStgBuilder.buildStg(component, new WhileStgHandshakesFromCollection(handshakes), builder);
	}
}
