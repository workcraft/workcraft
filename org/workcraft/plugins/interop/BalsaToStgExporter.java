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

package org.workcraft.plugins.interop;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.SynchronousExternalProcess;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeProtocol;
import org.workcraft.plugins.balsa.stg.MainStgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.HandshakeNameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.layout.PetriNetToolsSettings;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.Export;
import org.workcraft.util.Hierarchy;

public abstract class BalsaToStgExporter {

	private final HandshakeProtocol protocol;
	private final String protocolName;

	public BalsaToStgExporter(HandshakeProtocol protocol, String protocolName)
	{
		this.protocol = protocol;
		this.protocolName = protocolName;
	}

	public void export(Model model, OutputStream out) throws IOException, ModelValidationException, SerialisationException {

		BalsaCircuit balsa = (BalsaCircuit)model;

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

	protected Iterable<BreezeComponent> getComponentsToSave(BalsaCircuit balsa) {
		return Hierarchy.getDescendantsOfType(balsa.getRoot(), BreezeComponent.class);
	}

	public static void saveData(byte [] outputData, OutputStream out) throws IOException
	{
		out.write(outputData);
	}


	private STG buildStg(final BalsaCircuit circuit, final BreezeComponent breezeComponent) {
		STG stg = new STG();

		HandshakeNameProvider nameProvider = getNamesProvider(circuit, breezeComponent);

		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, nameProvider);

		MainStgBuilder.buildStg(breezeComponent.getUnderlyingComponent(), breezeComponent.getHandshakes(), stgBuilder, protocol);
		return stg;
	}

	private HandshakeNameProvider getNamesProvider(final BalsaCircuit circuit,
			final BreezeComponent breezeComponent) {
		final HashMap<Object, String> names;

			names = new HashMap<Object, String>();
			for(Entry<String, Handshake> entry : breezeComponent.getHandshakes().entrySet())
			{
				names.put(entry.getValue(), "c" + circuit.getNodeID(breezeComponent) + "_" + entry.getKey());
			}
			for(Entry<Handshake, HandshakeComponent> entry : breezeComponent.getHandshakeComponents().entrySet())
			{
				Connection connection = circuit.getConnection(entry.getValue());
				if(connection != null)
					names.put(entry.getKey(), "cn_" + circuit.getNodeID(connection));
			}
			names.put(breezeComponent.getUnderlyingComponent(), "c" + circuit.getNodeID(breezeComponent));

		HandshakeNameProvider nameProvider = new HandshakeNameProvider()
		{
			public String getName(Object handshake) {
				return names.get(handshake);
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

	public boolean isApplicableTo(Model model) {
		return model instanceof BalsaCircuit;
	}

}