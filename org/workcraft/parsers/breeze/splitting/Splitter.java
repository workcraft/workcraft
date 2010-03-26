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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.parsers.breeze.Netlist;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeConnection;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPull;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPush;
import org.workcraft.plugins.balsa.handshakebuilder.HandshakeVisitor;
import org.workcraft.plugins.balsa.handshakebuilder.PullHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.PushHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.Sync;

public class Splitter {
	public static SplitResult splitControlAndData(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> circuit)
	{
		return new Splitter().split(circuit);
	}

	private SplitResult split(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> circuit)
	{
		Map<HandshakeComponent, SplitPort> splitPorts = splitPorts(circuit);

		ArrayList<HandshakeComponent> controlPorts = new ArrayList<HandshakeComponent>();
		ArrayList<HandshakeComponent> dataPorts = new ArrayList<HandshakeComponent>();

		for(HandshakeComponent p : circuit.getPorts())
		{
			SplitPort split = splitPorts.get(p);
			controlPorts.add(split.controlPort);
			dataPorts.add(split.dataPort);
		}

		List<BreezeComponent> controlParts = new ArrayList<BreezeComponent>();
		List<BreezeComponent> dataParts = new ArrayList<BreezeComponent>();

		List<BreezeConnection> controlConns = new ArrayList<BreezeConnection>();
		List<BreezeConnection> dataConns = new ArrayList<BreezeConnection>();

		for(BreezeConnection c : circuit.getConnections())
		{
			SplitPort p1 = splitPorts.get(c.getFirst());
			SplitPort p2 = splitPorts.get(c.getSecond());

			BreezeConnection cc = tryConn(p1.controlPort, p2.controlPort);
			BreezeConnection cd = tryConn(p1.dataPort, p2.dataPort);

			if(cc != null)
				controlConns.add(cc);
			if(cd != null)
				controlConns.add(cd);
		}

		List<BreezeConnection> interconnects = new ArrayList<BreezeConnection>();

		for(BreezeComponent p : circuit.getBlocks())
		{
			PartSplitResult split = splitPart(p, splitPorts);
			controlParts.add(split.getControl());
			dataParts.add(split.getData());
			interconnects.addAll(split.getConnections());
		}

		return new SplitResult(getCircuit(controlPorts, controlParts, controlConns), getCircuit(dataPorts, dataParts, dataConns), interconnects);
	}

	private Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> getCircuit(
			final ArrayList<HandshakeComponent> ports,
			final List<BreezeComponent> parts, final List<BreezeConnection> conns) {
		return new Netlist<HandshakeComponent, BreezeComponent, BreezeConnection>() {
			@Override public Collection<? extends BreezeComponent> getBlocks() {
				return parts;
			}
			@Override public Collection<? extends BreezeConnection> getConnections() {
				return conns;
			}
			@Override public List<? extends HandshakeComponent> getPorts() {
				return ports;
			}
		};
	}

	private Map<HandshakeComponent, SplitPort> splitPorts(Netlist<HandshakeComponent,BreezeComponent,BreezeConnection> circuit) {
		Map<HandshakeComponent, SplitPort> result = new HashMap<HandshakeComponent, SplitPort>();
		for(BreezeComponent comp : circuit.getBlocks())
			for(HandshakeComponent port : comp.getHandshakeComponents().values())
				result.put(port, splitPort(port));
		for(HandshakeComponent port : circuit.getPorts())
			result.put(port, splitPort(port));
		return result;
	}

	private SplitPort splitPort(final HandshakeComponent port) {
		return port.getHandshake().accept(new HandshakeVisitor<SplitPort>()
				{
					@Override public SplitPort visit(Sync hs) {
						return new SplitPort(port, null);
					}

					@Override public SplitPort visit(FullDataPull hs) {
						return new SplitPort(port, null);
					}

					@Override
					public SplitPort visit(FullDataPush hs) {
						return new SplitPort(port, null);
					}

					@Override
					public SplitPort visit(PullHandshake hs) {
						return new SplitPort(port, port);
					}

					@Override
					public SplitPort visit(PushHandshake hs) {
						return new SplitPort(port, port);
					}
				}
		);
	}

	private BreezeConnection connect(HandshakeComponent p1, HandshakeComponent p2) {
		return new BreezeConnection(new MathConnection(p1, p2));
	}

	private BreezeConnection tryConn(HandshakeComponent p1, HandshakeComponent p2) {
		if(p1 == null || p2 == null)
			return null;
		return connect(p1, p2);
	}

	private PartSplitResult splitPart(BreezeComponent part, Map<HandshakeComponent, SplitPort> splitPorts) {
		List<HandshakeComponent> controlPorts = new ArrayList<HandshakeComponent>();
		List<HandshakeComponent> dataPorts = new ArrayList<HandshakeComponent>();

		boolean changed = false;
		for(HandshakeComponent p : part.getPorts())
		{
			SplitPort sp = splitPorts.get(p);

			if(sp.controlPort != p)
				changed = true;

			if(sp.controlPort != null)
				controlPorts.add(sp.controlPort);
			if(sp.dataPort != null)
				dataPorts.add(sp.controlPort);
		}

		BreezeComponent dataPart, controlPart;

		List<BreezeConnection> conns = new ArrayList<BreezeConnection>();

		if(!changed)
		{
			dataPart = null;
			controlPart = part;
		}
		else
		{
			HandshakeComponent active = createActiveSync();
			HandshakeComponent passive = createPassiveSync();
			dataPorts.add(passive);
			controlPorts.add(active);
			dataPart = createDataPart(part, dataPorts);
			controlPart = createControlPart(part, controlPorts);
		}

		return new PartSplitResult(dataPart, controlPart, conns);
	}
	private HandshakeComponent createPassiveSync() {
		return new HandshakeComponent(null, "");
	}

	private HandshakeComponent createActiveSync() {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private BreezeComponent createControlPart(BreezeComponent part, List<HandshakeComponent> controlPorts) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private BreezeComponent createDataPart(BreezeComponent part, List<HandshakeComponent> dataPorts) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}
}
