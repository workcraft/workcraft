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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
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
import org.workcraft.plugins.balsa.stgmodelstgbuilder.NameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.verification.PetriNetToolsSettings;
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
		export(((org.workcraft.plugins.balsa.BalsaCircuit)model).asNetlist(), out);
	}

	public void export(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> circuit, OutputStream out) throws IOException, ModelValidationException, SerialisationException
	{
		BalsaCircuit balsa = new BalsaCircuit(circuit);

		boolean useSimpleInternalHandshakes = false;

		NameProvider<Handshake> names = getNamesProvider(balsa);

		if(useSimpleInternalHandshakes)
		{
			STG stgf = buildStgFull(balsa, names);
			new DotGExporter().export(stgf, out);
		}
		else
		{
			ArrayList<File> tempFiles = new ArrayList<File>();
			for(BreezeComponent component : getComponentsToSave(balsa))
			{
				STG stg = buildStg(balsa, component, names);

				File tempFile = File.createTempFile("brz_", ".g");
				tempFiles.add(tempFile);

				Export.exportToFile(new DotGExporter(), stg, tempFile);
			}

			if(tempFiles.size() > 0)
			{
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
			else
			{
				new DotGExporter().export(new STG(), out);
			}
		}
	}

	private STG buildStgFull(BalsaCircuit balsa, NameProvider<Handshake> names) {
		STG stg = new STG();

		Iterable<? extends BreezeComponent> components = getComponentsToSave(balsa);

		Map<BreezeConnection, TwoWayStg> internalHandshakes = new HashMap<BreezeConnection, TwoWayStg>();

		for(BreezeComponent component : components)
		{
			Map<String, Handshake> fullHandshakes = new HashMap<String, Handshake>(component.getHandshakes());

			MainStgBuilder.addDataPathHandshakes(fullHandshakes, component.getUnderlyingComponent());

			StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, names);

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

	private BreezeConnection getInternalConnection(BalsaCircuit balsa, Iterable<? extends BreezeComponent> components, BreezeComponent component, Handshake handshake) {
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

	private boolean contains(Iterable<? extends BreezeComponent> components, BreezeComponent component)
	{
		for(BreezeComponent c : components)
			if(component == c)
				return true;
		return false;
	}

	protected Iterable<? extends BreezeComponent> getComponentsToSave(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> balsa) {
		return balsa.getBlocks();
	}

	private static void saveData(byte [] outputData, OutputStream out) throws IOException
	{
		out.write(outputData);
	}


	private STG buildStg(final BalsaCircuit circuit, final BreezeComponent breezeComponent, NameProvider<Handshake> names) {
		STG stg = new STG();

		Map<String, Handshake> fullHandshakes = new HashMap<String, Handshake>(breezeComponent.getHandshakes());

		MainStgBuilder.addDataPathHandshakes(fullHandshakes, breezeComponent.getUnderlyingComponent());

		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, names);

		Map<String, TwoSideStg> handshakeStgs = MainStgBuilder.buildHandshakes(fullHandshakes, protocol, stgBuilder);

		MainStgBuilder.buildStg(breezeComponent.getUnderlyingComponent(), handshakeStgs, stgBuilder);
		return stg;
	}

	class CountingNameProvider<T> implements NameProvider<T>
	{
		Map<T, String> names = new HashMap<T, String>();
		int nextId = 1;
		private final String prefix;

		public CountingNameProvider(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public String getName(T key) {
			String name = names.get(key);
			if(name == null)
			{
				name = "" + nextId++;
				names.put(key, name);
			}
			return prefix + name;
		}
	}

	private NameProvider<Handshake> getNamesProvider(final BalsaCircuit circuit)
	{
		NameProvider<BreezeComponent> componentNames = new CountingNameProvider<BreezeComponent>("c");

		final HashMap<Handshake, String> names = new HashMap<Handshake, String>();

		List<? extends HandshakeComponent> externalPorts = circuit.getPorts();

		for(HandshakeComponent hs : externalPorts)
			names.put(hs.getHandshake(), "port_" + hs.getHandshakeName());
		for(BreezeComponent comp : circuit.getBlocks())
			for(HandshakeComponent hs : comp.getPorts())
				names.put(hs.getHandshake(), componentNames.getName(comp) + "_" + hs.getHandshakeName());

		return new NameProvider<Handshake>()
		{
			public String getName(Handshake handshake)
			{
				String result = names.get(handshake);
				if(result == null)
					throw new NoSuchElementException("No name found for the given handshake");
				return result;
			}
		};
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