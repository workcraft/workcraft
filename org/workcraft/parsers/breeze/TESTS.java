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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.workcraft.parsers.breeze.dom.BreezeDocument;
import org.workcraft.parsers.breeze.dom.BreezePart;
import org.workcraft.parsers.breeze.dom.ChannelDeclaration;
import org.workcraft.parsers.breeze.dom.PortDeclaration;
import org.workcraft.parsers.breeze.dom.RawBreezePartReference;
import org.workcraft.parsers.breeze.expressions.Constant;
import org.workcraft.parsers.breeze.splitting.SplitResult;
import org.workcraft.parsers.breeze.splitting.Splitter;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeConnection;
import org.workcraft.plugins.balsa.HandshakeComponent;
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
import org.workcraft.plugins.balsa.io.BreezeImporter;
import org.workcraft.util.Export;

//TODO!
//@SuppressWarnings({"deprecation", "unused"})
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
	public void toVerilog() throws Exception
	{
		BreezeLibrary lib;

		File inFile = new File("C:\\balsa_Testing\\viterbi\\test.breeze");
		final BalsaCircuit balsa = new BreezeImporter("C:\\balsa_Testing\\balsa").importFrom(new FileInputStream(inFile));
		Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> circuit = balsa.asNetlist();

		SplitResult split = Splitter.splitControlAndData(circuit);

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
		OutputStream breezeFile;
		try {
			breezeFile = new FileOutputStream("c:\\data.breeze");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}// new ByteArrayOutputStream();
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
