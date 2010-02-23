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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.SynchronousExternalProcess;
import org.workcraft.parsers.breeze.Netlist;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeConnection;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakeevents.TwoWayStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivenessSelector;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeProtocol;
import org.workcraft.plugins.balsa.handshakestgbuilder.InternalHandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.TwoSideStg;
import org.workcraft.plugins.balsa.stg.MainStgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.HandshakeNameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.layout.PetriNetToolsSettings;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.Export;

public abstract class BalsaToStgExporter {

	private final HandshakeProtocol protocol;
	private final String protocolName;

	public BalsaToStgExporter(HandshakeProtocol protocol, String protocolName)
	{
		this.protocol = protocol;
		this.protocolName = protocolName;
	}

	private static class BalsaCircuit extends CachedCircuit<HandshakeComponent, BreezeComponent, BreezeConnection>
	{

		public BalsaCircuit(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> c) {
			super(c);
		}
	}

	public void export(Model model, OutputStream out) throws IOException, ModelValidationException, SerialisationException
	{
		export(((org.workcraft.plugins.balsa.BalsaCircuit)model).asCircuit(), out);
	}

	public void export(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> circuit, OutputStream out) throws IOException, ModelValidationException, SerialisationException
	{
		BalsaCircuit balsa = new BalsaCircuit(circuit);

		boolean useSimpleInternalHandshakes = false;

		if(useSimpleInternalHandshakes)
		{
			STG stgf = buildStgFull(balsa);
			new DotGExporter().export(stgf, out);
		}
		else
		{
			ArrayList<File> tempFiles = new ArrayList<File>();
			for(BreezeComponent component : getComponentsToSave(balsa))
			{
				STG stg = buildStg(balsa, component);

				File tempFile = File.createTempFile("brz_", ".g");
				tempFiles.add(tempFile);

				DotGExporter exporter = new DotGExporter();

				Export.exportToFile(exporter, stg, tempFile);
			}

			String [] args = new String [tempFiles.size() + 3];
			args[0] = PetriNetToolsSettings.getPcompCommand();
			args[1] = "-d";
			args[2] = "-r";
			for(int i=0;i<tempFiles.size();i++)
				args[i+3] = tempFiles.get(i).getPath();

			SynchronousExternalProcess pcomp = new SynchronousExternalProcess(args, ".");

			pcomp.start(10000);

			byte [] outputData = pcomp.getOutputData();
			System.out.println("----- Pcomp errors: -----");
			System.out.print(new String(pcomp.getErrorData()));
			System.out.println("----- End of errors -----");

			if(pcomp.getReturnCode() != 0)
			{
				System.out.println("");
				System.out.println("----- Pcomp output: -----");
				System.out.print(new String(outputData));
				System.out.println("----- End of output -----");

				throw new RuntimeException("Pcomp failed! Return code: " + pcomp.getReturnCode());
			}

			saveData(outputData, out);

			for(File f : tempFiles)
				f.delete();
		}
	}

	private STG buildStgFull(BalsaCircuit balsa) {
		STG stg = new STG();

		Iterable<BreezeComponent> components = getComponentsToSave(balsa);

		Map<BreezeConnection, TwoWayStg> internalHandshakes = new HashMap<BreezeConnection, TwoWayStg>();

		for(BreezeComponent component : components)
		{
			Map<String, Handshake> fullHandshakes = new HashMap<String, Handshake>(component.getHandshakes());

			MainStgBuilder.addDataPathHandshakes(fullHandshakes, component.getUnderlyingComponent());

			HandshakeNameProvider nameProvider = getNamesProvider(balsa, component, fullHandshakes);

			StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, nameProvider);

			Map<String, Handshake> external = new HashMap<String, Handshake>();
			Map<String, TwoWayStg> internal = new HashMap<String, TwoWayStg>();

			for(String name : fullHandshakes.keySet())
			{
				BreezeConnection connection = getInternalConnection(balsa, components, component, fullHandshakes.get(name));
				if(connection == null)
					external.put(name, fullHandshakes.get(name));
				else
				{
					if(!internalHandshakes.containsKey(connection))
					{
						TwoWayStg internalStg = buildInternalStg(fullHandshakes.get(name), stgBuilder);
						internalHandshakes.put(connection, internalStg);
					}

					internal.put(name, internalHandshakes.get(connection));
				}
			}

			Map<String, TwoSideStg> handshakeStgs = MainStgBuilder.buildHandshakes(external, protocol, stgBuilder);

			for(String name : internal.keySet())
				handshakeStgs.put(name, ActivenessSelector.direct(internal.get(name), fullHandshakes.get(name).isActive()));

			MainStgBuilder.buildStg(component.getUnderlyingComponent(), handshakeStgs, stgBuilder);
		}

		return stg;
	}

	private BreezeConnection getInternalConnection(BalsaCircuit balsa, Iterable<BreezeComponent> components, BreezeComponent component, Handshake handshake) {
		HandshakeComponent hs = component.getHandshakeComponents().get(handshake);
		if(hs==null)
			return null;
		BreezeConnection connection = balsa.getConnection(hs);
		if(connection == null)
			return null;

		if(!contains(components, balsa.getConnectedHandshake(hs).getOwner()))
			return null;
		return connection;
	}

	private TwoWayStg buildInternalStg(Handshake handshake, StgBuilder stg) {
		return handshake.accept(new InternalHandshakeStgBuilder(stg));
	}

	private boolean contains(Iterable<BreezeComponent> components, BreezeComponent component)
	{
		for(BreezeComponent c : components)
			if(component == c)
				return true;
		return false;
	}

	protected Iterable<BreezeComponent> getComponentsToSave(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> balsa) {
		return balsa.getBlocks();
	}

	private static void saveData(byte [] outputData, OutputStream out) throws IOException
	{
		out.write(outputData);
	}


	private STG buildStg(final BalsaCircuit circuit, final BreezeComponent breezeComponent) {
		STG stg = new STG();

		Map<String, Handshake> fullHandshakes = new HashMap<String, Handshake>(breezeComponent.getHandshakes());

		MainStgBuilder.addDataPathHandshakes(fullHandshakes, breezeComponent.getUnderlyingComponent());

		HandshakeNameProvider nameProvider = getNamesProvider(circuit, breezeComponent, fullHandshakes);

		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, nameProvider);

		Map<String, TwoSideStg> handshakeStgs = MainStgBuilder.buildHandshakes(fullHandshakes, protocol, stgBuilder);

		MainStgBuilder.buildStg(breezeComponent.getUnderlyingComponent(), handshakeStgs, stgBuilder);
		return stg;
	}

	class CountingNameProvider implements HandshakeNameProvider
	{
		Map<Object, String> names = new HashMap<Object, String>();
		int nextId = 1;
		private final String prefix;

		public CountingNameProvider(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public String getName(Object key) {
			String name = names.get(key);
			if(name == null)
			{
				name = "" + nextId++;
				names.put(key, name);
			}
			return prefix + name;
		}
	}

	private HandshakeNameProvider getNamesProvider(final BalsaCircuit circuit, final BreezeComponent breezeComponent, final Map<String, Handshake> handshakes) {
		final HashMap<Object, String> names;

		names = new HashMap<Object, String>();
		final CountingNameProvider cnp1 = new CountingNameProvider("c");
		final CountingNameProvider cnp2 = new CountingNameProvider("w");

		for(Entry<String, Handshake> entry : handshakes.entrySet())
			names.put(entry.getValue(), cnp1.getName(breezeComponent) + "_" + entry.getKey());

		for(Entry<Handshake, HandshakeComponent> entry : breezeComponent.getHandshakeComponents().entrySet())
		{
			BreezeConnection connection = circuit.getConnection(entry.getValue());
			if(connection != null)
				names.put(entry.getKey(), cnp2.getName(connection));
		}
		names.put(breezeComponent.getUnderlyingComponent(), cnp1.getName(breezeComponent));

		HandshakeNameProvider nameProvider = new HandshakeNameProvider()
		{
			public String getName(Object handshake) {
				String result = names.get(handshake);
				if(result == null)
					throw new IndexOutOfBoundsException("No name found for the given handshake");
				return result;
			}
		};
		return nameProvider;
	}

	public String getDescription() {
		return "STG using "+protocolName+" protocol (.g)";
	}

	public String getExtenstion() {
		return ".g";
	}

	public int getCompatibility(Model model) {
		if (model instanceof BalsaCircuit)
			return Exporter.BEST_COMPATIBILITY;
		else
			return Exporter.NOT_COMPATIBLE;
	}
}