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

import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class ConcurStgBuilder extends ComponentStgBuilder<Concur> {

	@Override
	public void buildStg(Concur component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder) {
		PassiveSync activate = (PassiveSync)handshakes.get("activate");

		for(int i=0;i<component.getOutputCount();i++)
		{
			ActiveSync out = (ActiveSync)handshakes.get("activateOut"+i);
			builder.connect(activate.go(), out.go());
			builder.connect(out.done(), activate.done());
		}
	}

}
