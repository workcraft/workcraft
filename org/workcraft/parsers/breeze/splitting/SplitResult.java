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
package org.workcraft.parsers.breeze.splitting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.workcraft.parsers.breeze.Block;
import org.workcraft.parsers.breeze.Netlist;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeConnection;
import org.workcraft.plugins.balsa.HandshakeComponent;

public class SplitResult implements Netlist<HandshakeComponent, Block<HandshakeComponent>, BreezeConnection>
{
	private final Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> data;
	private final Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> control;
	private final List<BreezeConnection> connections;

	public SplitResult(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> control, Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> data, List<BreezeConnection> connections)
	{
		this.data = data;
		this.control = control;
		this.connections = connections;
	}

	public Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> getData()
	{
		return data;
	}

	public Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> getControl()
	{
		return control;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Block<HandshakeComponent>> getBlocks() {
		return Arrays.asList((Block<HandshakeComponent>[])new Block[]{data, control});
	}

	@Override
	public List<BreezeConnection> getConnections() {
		return connections;
	}

	@Override
	public List<HandshakeComponent> getPorts() {
		ArrayList<HandshakeComponent> result = new ArrayList<HandshakeComponent>();
		result.addAll(data.getPorts());
		result.addAll(control.getPorts());
		for(BreezeConnection c : connections)
		{
			result.remove(c.getFirst());
			result.remove(c.getSecond());
		}
		return result;
	}
}

