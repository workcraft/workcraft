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
import org.workcraft.parsers.breeze.dom.ArrayedDataPortDeclaration;
import org.workcraft.parsers.breeze.dom.ArrayedSyncPortDeclaration;
import org.workcraft.parsers.breeze.dom.FullDataPortDeclaration;
import org.workcraft.parsers.breeze.dom.DataPortDeclaration;
import org.workcraft.parsers.breeze.dom.PortDeclaration;
import org.workcraft.parsers.breeze.dom.PortVisitor;
import org.workcraft.parsers.breeze.dom.SyncPortDeclaration;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class DynamicHandshakes extends HandshakeMaker<DynamicComponent> {

	@Override
	protected void fillHandshakes(DynamicComponent component,
			Map<String, Handshake> handshakes) {
		for(PortDeclaration port : component.declaration().getPorts())
		{
			addPort(port, component.parameters(), handshakes);
		}
	}

	private void addPort(PortDeclaration port, final ParameterScope parameters, final Map<String, Handshake> handshakes)
	{
		port.accept(new PortVisitor<Object>()
		{
			private Handshake newData(boolean isActive, boolean isInput, int width) {
				if(isActive)
					if(isInput)
						return builder.CreateActivePull(width);
					else
						return builder.CreateActivePush(width);
				else
					if(isInput)
						return builder.CreatePassivePush(width);
					else
						return builder.CreatePassivePull(width);
			}

			private Handshake newSync(PortDeclaration port) {
				Handshake hs;
				if(port.isActive())
					hs = builder.CreateActiveSync();
				else
					hs = builder.CreatePassiveSync();
				return hs;
			}

			private Handshake fullData(boolean isActive, boolean isInput, Integer width) {
				if(isActive)
					if(isInput)
						return builder.CreateActiveFullDataPull(width);
					else
						return builder.CreateActiveFullDataPush(width);
				else
					if(isInput)
						return builder.CreatePassiveFullDataPush(width);
					else
						return builder.CreatePassiveFullDataPull(width);
			}

			@Override public Object visit(SyncPortDeclaration port) {
				handshakes.put(port.getName(), newSync(port));
				return null;
			}

			@Override public Object visit(DataPortDeclaration port) {
				handshakes.put(port.getName(), newData(port.isActive(), port.isInput(), port.getWidth().evaluate(parameters)));
				return null;
			}

			@Override public Object visit(ArrayedDataPortDeclaration port) {
				for(int i=0;i<port.getCount().evaluate(parameters);i++)
					handshakes.put(port.getName() + i, newData(port.isActive(), port.isInput(), port.getWidth().evaluate(parameters)[i]));
				return null;
			}

			@Override public Object visit(ArrayedSyncPortDeclaration port) {
				for(int i=0;i<port.getCount().evaluate(parameters);i++)
					handshakes.put(port.getName() + i, newSync(port));
				return null;
			}

			@Override
			public Object visit(FullDataPortDeclaration port) {
				handshakes.put(port.getName(), fullData(port.isActive(), port.isInput(), port.getValueCount().evaluate(parameters)));
				return null;
			}

		}
		);
	}
}
