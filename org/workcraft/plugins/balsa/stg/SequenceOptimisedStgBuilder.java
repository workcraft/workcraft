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

import org.workcraft.plugins.balsa.components.SequenceOptimised;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

public class SequenceOptimisedStgBuilder extends
		ComponentStgBuilder<SequenceOptimised> {

	@Override
	public void buildStg(SequenceOptimised component,
			Map<String, StgHandshake> handshakes, StgBuilder builder) {

		PassiveSyncStg activate = (PassiveSyncStg)handshakes.get("activate");

		TransitionOutput lastHandshake = activate.getActivate();

		for(int i=0;i<component.getOutputCount();i++)
		{
			ActiveSyncStg out = (ActiveSyncStg)handshakes.get("activateOut"+i);
			builder.addConnection(lastHandshake, out.getActivate());
			lastHandshake = out.getDeactivate();
		}

		builder.addConnection(lastHandshake, activate.getDeactivate());
	}

}
