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
package org.workcraft.plugins.balsa.io;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.parsers.breeze.Block;
import org.workcraft.parsers.breeze.Netlist;
import org.workcraft.parsers.breeze.Connection;

public class CachedCircuit<Port, Comp extends Block<Port>, Conn extends Connection<Port>> implements Netlist<Port, Comp, Conn> {

	private final Netlist<Port, Comp, Conn> c;
	private final Map<Port, Conn> portConns = new HashMap<Port, Conn>();
	private final Map<Port, Port> portPort = new HashMap<Port, Port>();

	public CachedCircuit(Netlist<Port, Comp, Conn> c)
	{
		this.c = c;
		for(Conn conn : c.getConnections())
		{
			Port first = conn.getFirst();
			portConns.put(first, conn);
			Port second = conn.getSecond();
			portConns.put(second, conn);
			portPort.put(first, second);
			portPort.put(second, first);
		}
	}

	@Override
	public List<Comp> getBlocks() {
		return c.getBlocks();
	}

	@Override
	public List<Conn> getConnections() {
		return c.getConnections();
	}

	@Override
	public Collection<Port> getPorts() {
		return c.getPorts();
	}

	public Conn getConnection(Port port)
	{
		return portConns.get(port);
	}

	public Port getConnectedHandshake(Port hs) {
		return portPort.get(hs);
	}
}
