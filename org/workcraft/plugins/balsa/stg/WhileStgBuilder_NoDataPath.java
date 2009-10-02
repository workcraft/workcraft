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
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveFullDataPullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class WhileStgBuilder_NoDataPath extends ComponentStgBuilder<While> {
	static interface WhileStgHandshakes
	{
		public ActiveFullDataPullStg guard();
		public PassiveSync activate();
		public ActiveSync activateOut();
	}

	class WhileStgHandshakesFromCollection implements WhileStgHandshakes
	{
		private final Map<String, StgInterface> map;

		public WhileStgHandshakesFromCollection(Map<String, StgInterface> map)
		{
			this.map = map;
		}

		public PassiveSync activate() {
			return (PassiveSync)map.get("activate");
		}

		public ActiveSync activateOut() {
			return (ActiveSync)map.get("activateOut");
		}

		public ActiveFullDataPullStg guard() {
			return (ActiveFullDataPullStg)map.get("guard");
		}
	}

	static class WhileInternalStgBuilder
	{
		public static void buildStg(While component, WhileStgHandshakes handshakes, StrictPetriBuilder builder)
		{
			StgPlace activated = builder.buildPlace(0);

			PassiveSync activate = handshakes.activate();
			ActiveSync activateOut = handshakes.activateOut();
			ActiveFullDataPullStg guard = handshakes.guard();

			// Call guard
			builder.connect(activate.go(), activated);
			builder.connect(activated, guard.go());

			// Activate and repeatedly call guard
			builder.connect(guard.result().get(1), activateOut.go());
			builder.connect(activateOut.done(), activated);

			// Return
			builder.connect(guard.result().get(0), activate.done());
		}
	}

	public void buildStg(While component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder)
	{
		WhileInternalStgBuilder.buildStg(component, new WhileStgHandshakesFromCollection(handshakes), builder);
	}
}
