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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.SynchronousExternalProcess;
import org.workcraft.parsers.breeze.Netlist;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeConnection;
import org.workcraft.plugins.balsa.BreezeHandshake;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakeevents.TwoWayStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivenessSelector;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeProtocol;
import org.workcraft.plugins.balsa.handshakestgbuilder.InternalHandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.TwoSideStg;
import org.workcraft.plugins.balsa.stg.MainStgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.NameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.shared.PcompUtilitySettings;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.Export;

public abstract class BalsaToStgExporter implements Exporter {

	public static class CompositionSettings
	{
		public CompositionSettings(boolean eventBasedInternal, boolean improvedPcomp)
		{
			this.eventBasedInternal = eventBasedInternal;
			this.improvedPcomp = improvedPcomp;
		}
		public boolean eventBasedInternal;
		public boolean improvedPcomp;
	}

	private final HandshakeProtocol protocol;
	private final String protocolName;

	public BalsaToStgExporter(HandshakeProtocol protocol, String protocolName)
	{
		this.protocol = protocol;
		this.protocolName = protocolName;
	}

	private static class BalsaCircuit extends CachedCircuit<BreezeHandshake, BreezeComponent, BreezeConnection>
	{
		public BalsaCircuit(Netlist<BreezeHandshake, BreezeComponent, BreezeConnection> c) {
			super(c);
		}
	}

	public void export(Model model, OutputStream out) throws IOException, ModelValidationException, SerialisationException
	{
		export(((org.workcraft.plugins.balsa.BalsaCircuit)model).asNetlist(), out);
	}

	public STGModel getSTG(org.workcraft.plugins.balsa.BalsaCircuit circuit) throws IOException, ModelValidationException, SerialisationException
	{
		return getSTG(circuit.asNetlist());
	}

	public void export(Model model, CompositionSettings settings, OutputStream out) throws IOException, ModelValidationException, SerialisationException
	{
		export(((org.workcraft.plugins.balsa.BalsaCircuit)model).asNetlist(), settings, out);
	}

	public void export(Netlist<BreezeHandshake, BreezeComponent, BreezeConnection> circuit, OutputStream out) throws IOException, ModelValidationException, SerialisationException
	{
		export(circuit, askForSettings(), out);
	}

	public STGModel getSTG(Netlist<BreezeHandshake, BreezeComponent, BreezeConnection> circuit) throws IOException, ModelValidationException, SerialisationException
	{
		return getSTG(circuit, askForSettings());
	}

	private CompositionSettings askForSettings() {
		String opt1 = "Improved Pcomp";
		String opt2 = "Standard Pcomp";
		String opt3 = "Event-based composition";
		Object result = JOptionPane.showOptionDialog(null, "Select the type of STG composition", "STG composition type", 0, JOptionPane.QUESTION_MESSAGE, null, new Object[]{opt1, opt2, opt3}, opt1);

		boolean improved = (result != opt2);
		boolean event = (result == opt3);
		final CompositionSettings settings = new CompositionSettings(event, improved);
		return settings;
	}

	public void export(Netlist<BreezeHandshake, BreezeComponent, BreezeConnection> circuit, CompositionSettings settings, OutputStream out) throws IOException, ModelValidationException, SerialisationException
	{
		new DotGExporter().export(getSTG(circuit, settings), out);
	}

	public STGModel getSTG(Netlist<BreezeHandshake, BreezeComponent, BreezeConnection> circuit, CompositionSettings settings) throws IOException, ModelValidationException, SerialisationException
	{
		BalsaCircuit balsa = new BalsaCircuit(circuit);

		boolean useSimpleInternalHandshakes = false;

		NameProvider<Handshake> names = getNamesProvider(balsa);

		if(useSimpleInternalHandshakes)
		{
			STG stgf = buildStgFull(balsa, names);
			return stgf;
		}
		else
		{
			int cc=1;
			ArrayList<File> tempFiles = new ArrayList<File>();
			for(BreezeComponent component : getComponentsToSave(balsa))
			{
				STG stg = buildStg(balsa, component, names);

				File tempFile = new File(String.format("/tmp/%d.g",cc++)); //File.createTempFile("brz_", ".g");
				tempFiles.add(tempFile);

				Export.exportToFile(new DotGExporter(), stg, tempFile);
			}

			if(tempFiles.size() > 0)
			{
				List<String>  args = new ArrayList<String> ();
				args.add(PcompUtilitySettings.getPcompCommand());
				args.add("-d");
				args.add("-r");
				if(settings.improvedPcomp) args.add("-p");
				for(int i=0;i<tempFiles.size();i++)
					args.add(tempFiles.get(i).getPath());

				try
				{
					SynchronousExternalProcess pcomp = new SynchronousExternalProcess(args.toArray(new String[0]), ".");

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

					try {
						return new DotGImporter().importSTG(new ByteArrayInputStream(outputData));
					} catch (DeserialisationException e) {
						throw new RuntimeException(e);
					}
				}
				finally
				{
					for(File f : tempFiles)
						f.delete();
				}
			}
			else
				return new STG();
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
		BreezeHandshake hs = component.getHandshakeComponents().get(handshake);
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

	protected Iterable<? extends BreezeComponent> getComponentsToSave(Netlist<BreezeHandshake, BreezeComponent, BreezeConnection> balsa) {
		return balsa.getBlocks();
	}

	public static boolean injectiveLabelling = false;

	private STG buildStg(final BalsaCircuit circuit, final BreezeComponent breezeComponent, NameProvider<Handshake> names) {
		STG stg = new STG();

		Map<String, Handshake> fullHandshakes = new HashMap<String, Handshake>(breezeComponent.getHandshakes());

		MainStgBuilder.addDataPathHandshakes(fullHandshakes, breezeComponent.getUnderlyingComponent());

		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, names);



		DynamicComponent component = breezeComponent.getUnderlyingComponent();

		if(!injectiveLabelling && component.declaration().getName().equals("Call"))
		{
			Handshake inp0 = fullHandshakes.get("inp0");
			Handshake inp1 = fullHandshakes.get("inp1");
			Handshake out = fullHandshakes.get("out");
			StgPlace place = stgBuilder.buildPlace(1);
			StgSignal r1 = stgBuilder.buildSignal(new SignalId(inp0, "rq"), false);
			StgSignal r2 = stgBuilder.buildSignal(new SignalId(inp1, "rq"), false);
			StgSignal a1 = stgBuilder.buildSignal(new SignalId(inp0, "ac"), true);
			StgSignal a2 = stgBuilder.buildSignal(new SignalId(inp1, "ac"), true);
			StgSignal r_1 = stgBuilder.buildSignal(new SignalId(out, "rq"), true);
			StgSignal a_1 = stgBuilder.buildSignal(new SignalId(out, "ac"), false);
			StgSignal r_2 = stgBuilder.buildSignal(new SignalId(out, "rq"), true);
			StgSignal a_2 = stgBuilder.buildSignal(new SignalId(out, "ac"), false);

			stgBuilder.connect(place, r1.getPlus());
			stgBuilder.connect(r1.getPlus(), r_1.getPlus());
			stgBuilder.connect(r_1.getPlus(), a_1.getPlus());
			stgBuilder.connect(a_1.getPlus(), a1.getPlus());
			stgBuilder.connect(a1.getPlus(), r1.getMinus());
			stgBuilder.connect(r1.getMinus(), r_1.getMinus());
			stgBuilder.connect(r_1.getMinus(), a_1.getMinus());
			stgBuilder.connect(a_1.getMinus(), a1.getMinus());
			stgBuilder.connect(a1.getMinus(), place);

			stgBuilder.connect(place, r2.getPlus());
			stgBuilder.connect(r2.getPlus(), r_2.getPlus());
			stgBuilder.connect(r_2.getPlus(), a_2.getPlus());
			stgBuilder.connect(a_2.getPlus(), a2.getPlus());
			stgBuilder.connect(a2.getPlus(), r2.getMinus());
			stgBuilder.connect(r2.getMinus(), r_2.getMinus());
			stgBuilder.connect(r_2.getMinus(), a_2.getMinus());
			stgBuilder.connect(a_2.getMinus(), a2.getMinus());
			stgBuilder.connect(a2.getMinus(), place);
		}
		else
		{
			Map<String, TwoSideStg> handshakeStgs = MainStgBuilder.buildHandshakes(fullHandshakes, protocol, stgBuilder);
			MainStgBuilder.buildStg(component, handshakeStgs, stgBuilder);
		}
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

		List<? extends BreezeHandshake> externalPorts = circuit.getPorts();

		for(BreezeHandshake hs : externalPorts)
			names.put(hs.getHandshake(), "port_" + hs.getHandshakeName());
		for(BreezeComponent comp : circuit.getBlocks())
			for(BreezeHandshake hs : comp.getPorts())
				names.put(hs.getHandshake(), componentNames.getName(comp) + "_" + hs.getHandshakeName());

		for(BreezeConnection con : circuit.getConnections())
		{
			Handshake first = con.getFirst().getHandshake();
			Handshake second = con.getSecond().getHandshake();
			String fullName = names.get(first) + "__" + names.get(second);
			names.put(first, fullName);
			names.put(second, fullName);
		}

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
		if (model instanceof org.workcraft.plugins.balsa.BalsaCircuit)
			return Exporter.BEST_COMPATIBILITY;
		else
			return Exporter.NOT_COMPATIBLE;
	}

	public Exporter withCompositionSettings(final CompositionSettings settings)
	{
		final BalsaToStgExporter me = this;
		return new Exporter()
		{
			@Override
			public String getDescription() {
				return me.getDescription();
			}

			@Override
			public String getExtenstion() {
				return me.getExtenstion();
			}

			@Override
			public UUID getTargetFormat() {
				return me.getTargetFormat();
			}

			@Override
			public int getCompatibility(Model model) {
				return me.getCompatibility(model);
			}

			@Override
			public void export(Model model, OutputStream out)
					throws IOException, ModelValidationException,
					SerialisationException {
				me.export(model, settings, out);
			}
		};
	}
}