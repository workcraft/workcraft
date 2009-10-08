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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.workcraft.plugins.balsa.components.SequenceOptimised;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class SequenceOptimisedStgBuilder extends
		ComponentStgBuilder<SequenceOptimised> {
	ProcessOperations o;
	@Override
	public void buildStg(SequenceOptimised component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder) {
		o = new ProcessOperations(builder);
		PassiveSync activate = (PassiveSync)handshakes.get("activate");
		Collection<ActiveSync> arr = getHandshakeArray(handshakes, "activateOut", component.getOutputCount(), ActiveSync.class);
		o.enclosure(activate, o.sequence(arr));
	}
	private static <T extends StgInterface> Collection<T> getHandshakeArray(Map<String, StgInterface> handshakes,
			String arrayName, int arraySize, Class<T> handshakeType) {
		Collection<T> result = new ArrayList<T>(arraySize);

		for(int i=0;i<arraySize;i++)
			result.add(handshakeType.cast(handshakes.get("activateOut"+i)));

		return result;
	}

}
