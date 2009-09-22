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

package org.workcraft.testing.plugins.balsa;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.HandshakeNameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class StgModelStgBuilderTests {

	@Test
	public void Test1()
	{
		STG stg = new STG();
		StgBuilder builder =  new StgModelStgBuilder(stg, new HandshakeNameProvider(){
			@Override
			public String getName(Object handshake) {
				return handshake.toString();
			}
		});

		builder.buildSignal(new SignalId("hs1", "s1"), true);
		SignalTransition [] transitions = stg.getTransitions().toArray(new SignalTransition[0]);

		findSignal(transitions, "hs1_s1", Type.OUTPUT);
	}

	private void findSignal(SignalTransition[] transitions, String signalName, Type type) {

		boolean plusFound = false;
		boolean minusFound = false;

		for(SignalTransition t : transitions)
		{
			if(!t.getSignalName().equals(signalName))
				continue;
			if(!t.getSignalType().equals(type))
				continue;
			if(!t.getDirection().equals(Direction.MINUS))
				minusFound = true;
			if(!t.getDirection().equals(Direction.PLUS))
				plusFound = true;
		}
		Assert.assertTrue(plusFound && minusFound);
	}
}
