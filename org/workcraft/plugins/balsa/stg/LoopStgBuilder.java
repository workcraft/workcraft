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

import org.workcraft.plugins.balsa.components.Loop;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;

public class LoopStgBuilder extends ComponentStgBuilder<org.workcraft.plugins.balsa.components.Loop> {

	@Override
	public void buildStg(Loop component, Map<String, StgHandshake> handshakes, StgBuilder builder) {
		PassiveSyncStg activate = (PassiveSyncStg)handshakes.get("activate");
		ActiveSyncStg activateOut = (ActiveSyncStg)handshakes.get("activateOut");

		StgPlace activated = builder.buildPlace();

		StgPlace never = builder.buildPlace();

		builder.addConnection(activate.getActivate(), activated);
		builder.addConnection(activated, activateOut.getActivate());
		builder.addConnection(activateOut.getDeactivate(), activated);

		builder.addConnection(never, activate.getDeactivate());
	}

}
