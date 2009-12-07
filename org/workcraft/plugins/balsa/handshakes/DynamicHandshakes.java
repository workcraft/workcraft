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

package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.parsers.breeze.ParameterScope;
import org.workcraft.parsers.breeze.PortDeclaration;
import org.workcraft.parsers.breeze.PortType;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class DynamicHandshakes extends HandshakeMaker<DynamicComponent> {

	@Override
	protected void fillHandshakes(DynamicComponent component,
			Map<String, Handshake> handshakes) {
		for(PortDeclaration port : component.declaration().ports())
		{
			addPort(port, component.parameters(), handshakes);
		}
	}

	private void addPort(PortDeclaration port, ParameterScope parameters, Map<String, Handshake> handshakes) {
		if(port.isArrayed)
		{
			for(int i=0;i<port.count.evaluate(parameters);i++)
				addChannel(port.name+i, i, port, parameters, handshakes);
		}
		else
			addChannel(port.name, 0, port, parameters, handshakes);
	}

	private void addChannel(String name, int i, PortDeclaration port,
			ParameterScope parameters, Map<String, Handshake> handshakes) {
		Handshake hs;

		if(port.type == PortType.SYNC)
		{
			if(port.isActive)
				hs = builder.CreateActiveSync();
			else
				hs = builder.CreatePassiveSync();
		}
		else
		{
			int width = port.width.evaluate(parameters)[i];
			if(port.isActive)
				if(port.isInput)
					hs = builder.CreateActiveFullDataPull(width);
				else
					hs = builder.CreateActiveFullDataPush(width);
			else
				if(port.isInput)
					hs = builder.CreatePassiveFullDataPush(width);
				else
					hs = builder.CreatePassiveFullDataPull(width);
		}

		handshakes.put(name, hs);
	}
}
