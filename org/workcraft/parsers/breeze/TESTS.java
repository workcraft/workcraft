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

package org.workcraft.parsers.breeze;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.Port;

import org.junit.Test;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.parsers.breeze.dom.BreezeDocument;
import org.workcraft.parsers.breeze.dom.BreezePart;
import org.workcraft.parsers.breeze.dom.ChannelDeclaration;
import org.workcraft.parsers.breeze.dom.PortDeclaration;
import org.workcraft.parsers.breeze.dom.PortVisitor;
import org.workcraft.parsers.breeze.dom.RawBreezePartReference;
import org.workcraft.parsers.breeze.expressions.Constant;
import org.workcraft.parsers.breeze.javacc.BreezeParser;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeConnection;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPull;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPush;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.HandshakeVisitor;
import org.workcraft.plugins.balsa.handshakebuilder.PullHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.PushHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.Sync;
import org.workcraft.plugins.balsa.io.BalsaToGatesExporter;
import org.workcraft.plugins.balsa.io.BalsaToStgExporter_FourPhase;
import org.workcraft.plugins.balsa.stg.implementations.StgBuilderSelector;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.Export;

//TODO!
@SuppressWarnings({"deprecation", "unused"})
public class TESTS {
	@Test
	public void parseAbs() throws org.workcraft.parsers.breeze.javacc.ParseException, IOException
	{
		BreezeLibrary lib = new BreezeLibrary();
		lib.registerPrimitives(new File("C:\\deleteMe\\"));
	}

	@Test
	public void viterbiToGates() throws Exception
	{
		File bzrFileName = new File("C:\\deleteMe\\viterbi\\BMU.breeze");
		File definitionsFolder = new File("C:\\deleteMe");

		BreezeLibrary lib = new BreezeLibrary();
		lib.registerPrimitives(definitionsFolder);

		registerParts(bzrFileName, lib);

		BalsaCircuit circuit = new BalsaCircuit();

		DefaultBreezeFactory factory = new DefaultBreezeFactory(circuit);

		lib.get("BMU").instantiate(lib, factory, EmptyValueList.instance());

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		Export.exportToFile(new BalsaToStgExporter_FourPhase(), circuit, "c:\\viterbi.g");

		new BalsaToGatesExporter().export(circuit, stream);
	}

	static abstract class ClassicHandshakeVisitor<T> implements HandshakeVisitor<T>
	{
		@Override public T visit(FullDataPull hs) {
			throw new org.workcraft.exceptions.NotSupportedException();
		}

		@Override public T visit(FullDataPush hs) {
			throw new org.workcraft.exceptions.NotSupportedException();
		}
	}

	private static final class HandshakeToPort extends ClassicHandshakeVisitor<PortDeclaration> {
		private final String portName;

		private HandshakeToPort(String portName) {
			this.portName = portName;
		}

		@Override public PortDeclaration visit(Sync hs) {
			return PortDeclaration.createSync(portName, hs.isActive());
		}

		@Override public PortDeclaration visit(PullHandshake hs) {
			return PortDeclaration.createData(portName, hs.isActive(), hs.isActive(), new Constant<Integer>(hs.getWidth()));
		}

		@Override public PortDeclaration visit(PushHandshake hs) {
			return PortDeclaration.createData(portName, hs.isActive(), !hs.isActive(), new Constant<Integer>(hs.getWidth()));
		}

		public static PortDeclaration convert(Handshake handshake, String portName) {
			return handshake.accept(new HandshakeToPort(portName));
		}
	}

	private static final class HandshakeToChannel extends ClassicHandshakeVisitor<ChannelDeclaration>
	{
		@Override public ChannelDeclaration visit(Sync hs) {
			return new ChannelDeclaration(ChannelType.SYNC, 0);
		}
		@Override public ChannelDeclaration visit(PullHandshake hs) {
			return new ChannelDeclaration(ChannelType.PULL, hs.getWidth());
		}
		@Override public ChannelDeclaration visit(PushHandshake hs) {
			return new ChannelDeclaration(ChannelType.PUSH, hs.getWidth());
		}
		public static HandshakeToChannel instance = new HandshakeToChannel();
		public static ChannelDeclaration convert(Handshake hs) {
			return hs.accept(instance);
		}
	}


	class VerilogComponent
	{
		public String verilogText;
		public String componentName;
	}

	@Test
	public void toVerilog()
	{
		BreezeLibrary lib;

		Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> circuit = null;//input

		SplitResult split = splitControlAndData(circuit);

		Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> control = split.getControl();
		Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> data = split.getData();

		VerilogComponent controller = synthesiseThroughStg(control);
		VerilogComponent dataPath = directMap(data);

		//VerilogComponent completeVerilog = createDataControlInterconnect(controller, dataPath, split);
		//File
	}

	BreezeDocument toBreeze(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> circuit)
	{
		List<PortDeclaration> ports = new ArrayList<PortDeclaration>();
		List<ChannelDeclaration> channels = new ArrayList<ChannelDeclaration>();
		List<RawBreezePartReference> parts = new ArrayList<RawBreezePartReference>();
		int index = 0;
		for(HandshakeComponent c : circuit.getPorts())
		{
			String portName = "port"+index++;
			ports.add(HandshakeToPort.convert(c.getHandshake(), portName));
			channels.add(HandshakeToChannel.convert(c.getHandshake()));
		}
		for(BreezeConnection c : circuit.getConnections())
			channels.add(HandshakeToChannel.convert(c.getFirst().getHandshake()));



		for(BreezeComponent c : circuit.getBlocks())
		{
			DynamicComponent dc = (DynamicComponent)c.getUnderlyingComponent();
			String name = dc.declaration().getName();
			List<String> parameters = new ArrayList<String>();
			List<List<Integer>> connections = new ArrayList<List<Integer>>();

			for(ParameterDeclaration param : dc.declaration().parameters)
				parameters.add(paramToString(dc.parameters().get(param.getName())));

			new RawBreezePartReference(name, parameters, connections);
		}

		BreezePart part = new BreezePart("dataPath", ports, channels, parts);
		return new BreezeDocument(Arrays.asList(new BreezePart[]{part }));
	}

	private String paramToString(Object object) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private VerilogComponent directMap(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> data)
	{
		ByteArrayOutputStream breezeFile = new ByteArrayOutputStream();
		new Writer(new PrintStream(breezeFile)).print(toBreeze(data));
		//TODO
		return null;
	}

	private VerilogComponent synthesiseThroughStg(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> control)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new BalsaToStgExporter_FourPhase().export(control, out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.out.println(out.toString());
		return null;
	}

	public class PartSplitResult
	{

		public PartSplitResult(
				BreezeComponent data,
				BreezeComponent control,
				List<BreezeConnection> connections) {
			super();
			this.data = data;
			this.control = control;
			this.connections = connections;
		}
		private final BreezeComponent data;
		private final BreezeComponent control;
		private final List<BreezeConnection> connections;
		public BreezeComponent getControl() {
			return control;
		}
		public BreezeComponent getData() {
			return data;
		}
		public Collection<? extends BreezeConnection> getConnections() {
			return connections;
		}
	}


	public class SplitResult implements Netlist<HandshakeComponent, Block<HandshakeComponent>, BreezeConnection>
	{
		private final Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> data;
		private final Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> control;
		private final List<BreezeConnection> connections;

		public SplitResult(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> data, Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> control, List<BreezeConnection> connections)
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

	class SplitPort
	{
		public SplitPort(HandshakeComponent controlPort, HandshakeComponent dataPort)
		{
			this.controlPort = controlPort;
			this.dataPort = dataPort;
		}
		public final HandshakeComponent controlPort;
		public final HandshakeComponent dataPort;
	}

	private SplitResult splitControlAndData(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> circuit)
	{
		Map<HandshakeComponent, SplitPort> splitPorts = splitPorts(circuit.getPorts());

		ArrayList<HandshakeComponent> controlPorts = new ArrayList<HandshakeComponent>();
		ArrayList<HandshakeComponent> dataPorts = new ArrayList<HandshakeComponent>();

		for(HandshakeComponent p : splitPorts.keySet())
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
			controlParts.add(split.getData());
			interconnects.addAll(split.getConnections());
		}

		return new SplitResult(getCircuit(controlPorts, controlParts, controlConns), getCircuit(dataPorts, dataParts, dataConns), interconnects);
	}

	private Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> getCircuit(
			ArrayList<HandshakeComponent> dataPorts,
			List<BreezeComponent> dataParts, List<BreezeConnection> dataConns) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private Map<HandshakeComponent, SplitPort> splitPorts(
			Collection<HandshakeComponent> ports) {
		throw new org.workcraft.exceptions.NotImplementedException();
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
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private HandshakeComponent createActiveSync() {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private BreezeComponent createControlPart(BreezeComponent part,
			List<HandshakeComponent> controlPorts) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private BreezeComponent createDataPart(BreezeComponent part,
			List<HandshakeComponent> dataPorts) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private HandshakeComponent getDataPortDataPart(
			HandshakeComponent p) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private HandshakeComponent getDataPortControlPart(
			HandshakeComponent p) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private boolean isPureControlPort(HandshakeComponent p) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private BreezeConnection connect(HandshakeComponent p1, HandshakeComponent p2) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	private BreezeConnection tryConn(HandshakeComponent p1, HandshakeComponent p2) {
		if(p1 == null || p2 == null)
			return null;
		return connect(p1, p2);
	}

	private Map<HandshakeComponent, SplitPort> splitPorts(List<HandshakeComponent> ports)
	{
		Map<HandshakeComponent, SplitPort> result = new HashMap<HandshakeComponent, SplitPort>();

		for(HandshakeComponent p : ports)
		{
			HandshakeComponent control;
			HandshakeComponent data;
			if (isPureControlPort(p))
			{
				control = p;
				data = null;
			}
			else
			{
				control = getDataPortControlPart(p);
				data = getDataPortDataPart(p);
			}

			result.put(p, new SplitPort(control, data));
		}

		return result;
	}

	private void mapSimple(BreezeComponent component, BalsaCircuit preSplit, Map<HandshakeComponent, HandshakeComponent> portMapping) {
		BreezeComponent result = preSplit.addNew(component.getUnderlyingComponent());
		for(String portName : component.getHandshakes().keySet())
			portMapping.put(component.getHandshakeComponentByName(portName), result.getHandshakeComponentByName(portName));
	}

	private void registerParts(File file, BreezeLibrary lib) throws Exception {
		InputStream is = new FileInputStream(file);
		try
		{
			lib.registerParts(is);
		}
		finally
		{
			is.close();
		}
	}
}
