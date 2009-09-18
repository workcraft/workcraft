package org.workcraft.plugins.interop;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.stg.MainStgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.HandshakeNameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.layout.PetriNetToolsSettings;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.Export;
import org.workcraft.util.Hierarchy;

public abstract class BalsaToStgExporter {

	private final HandshakeStgBuilder protocol;
	private final String protocolName;

	public BalsaToStgExporter(HandshakeStgBuilder protocol, String protocolName)
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

		String [] args = new String [tempFiles.size() + 2];
		args[0] = PetriNetToolsSettings.getPcompCommand();
		args[1] = "-d";
		for(int i=0;i<tempFiles.size();i++)
			args[i+2] = tempFiles.get(i).getPath();

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

		final Map<Handshake, HandshakeComponent> handshakeComponents = breezeComponent.getHandshakeComponents();
		final Map<String, Handshake> handshakes = breezeComponent.getHandshakes();

		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, new HandshakeNameProvider()
		{
			HashMap<Object, String> names;

			{
				names = new HashMap<Object, String>();
				for(Entry<String, Handshake> entry : handshakes.entrySet())
				{
					names.put(entry.getValue(), "c" + circuit.getNodeID(breezeComponent) + "_" + entry.getKey());
				}
				for(Entry<Handshake, HandshakeComponent> entry : handshakeComponents.entrySet())
				{
					Connection connection = circuit.getConnection(entry.getValue());
					if(connection != null)
						names.put(entry.getKey(), "cn_" + circuit.getNodeID(connection));
				}
				names.put(breezeComponent.getUnderlyingComponent(), "c" + circuit.getNodeID(breezeComponent));
			}

			public String getName(Object handshake) {
				return names.get(handshake);
			}
		});

		protocol.setStgBuilder(stgBuilder);
		MainStgBuilder.buildStg(breezeComponent.getUnderlyingComponent(), handshakes, protocol);
		return stg;
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