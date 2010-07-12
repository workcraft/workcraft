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
import java.util.List;
import java.util.Map;

import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;

public class StgHandshakeInterpreter {
	private final Map<String, StgInterface> h;

	public StgHandshakeInterpreter(Map<String, StgInterface> handshakes)
	{
		this.h = handshakes;
	}

	public <T extends StgInterface> List<T> array(
			String arrayName, int portCount, Class<T> handshakeType) {
		return array(h, arrayName, portCount, handshakeType);
	}


	public static <T extends StgInterface> List<T> array(
			Map<String, StgInterface> handshakes,
			String arrayName, int portCount, Class<T> handshakeType) {
		List<T> result = new ArrayList<T>(portCount);

		for(int i=0;i<portCount;i++)
		{
			result.add(get(handshakes, arrayName+i, handshakeType));
		}

		return result;
	}

	public <T extends StgInterface> T get(String portName, Class<T> type)
	{
		return get(h, portName, type);
	}

	public static <T extends StgInterface> T get(Map<String, StgInterface> handshakes, String portName, Class<T> type)
	{
		StgInterface result = handshakes.get(portName);
		if(result == null)
			throw new RuntimeException(String.format("Port '%s' not found", portName));
		if(!type.isInstance(result))
			throw new RuntimeException(String.format("Port '%s' is of incorrect type: %s instead of %s", portName, result.getClass().getSimpleName(), type.getSimpleName()));
		return type.cast(result);
	}
}
