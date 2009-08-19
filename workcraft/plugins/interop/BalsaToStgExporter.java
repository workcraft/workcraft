package org.workcraft.plugins.interop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.framework.interop.SynchronousExternalProcess;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.stg.MainStgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.HandshakeNameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.stg.STG;

public abstract class BalsaToStgExporter {

	private final HandshakeStgBuilder protocol;
	private final String protocolName;

	public BalsaToStgExporter(HandshakeStgBuilder protocol, String protocolName)
	{
		this.protocol = protocol;
		this.protocolName = protocolName;
	}

	private String pcompPath = "../Util/pcomp";

	public void exportToFile(Model model, File file) throws IOException {

		BalsaCircuit balsa = (BalsaCircuit)model.getMathModel();

		ArrayList<File> tempFiles = new ArrayList<File>();

		for(Component component : balsa.getComponents())
		{
			if(component instanceof BreezeComponent)
			{
				final BreezeComponent breezeComponent = (BreezeComponent) component;

				STG stg = buildStg(breezeComponent);

				File tempFile = File.createTempFile("brz_", ".g");
				tempFiles.add(tempFile);

				DotGExporter exporter = new DotGExporter();

				exporter.exportToFile(stg, tempFile);
			}
		}

		String [] args = new String [tempFiles.size() + 2];
		args[0] = pcompPath;
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

		saveData(outputData, file);

		for(File f : tempFiles)
			f.delete();
	}

	public static void saveData(byte [] outputData, File out) throws IOException
	{
	    FileChannel outChannel = new
	        FileOutputStream(out).getChannel();
	    try {
	    	outChannel.write(ByteBuffer.wrap(outputData));
	    }
	    finally {
	        outChannel.close();
	    }
	}


	private STG buildStg(final BreezeComponent breezeComponent) {
		STG stg = new STG();

		final Map<Handshake, HandshakeComponent> handshakeComponents = breezeComponent.getHandshakeComponents();
		final Map<String, Handshake> handshakes = breezeComponent.getHandshakes();

		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, new HandshakeNameProvider()
		{
			HashMap<Handshake, String> names;

			{
				names = new HashMap<Handshake, String>();
				for(Entry<String, Handshake> entry : handshakes.entrySet())
				{
					names.put(entry.getValue(), "c" + breezeComponent.getID() + "_" + entry.getKey());
				}
				for(Entry<Handshake, HandshakeComponent> entry : handshakeComponents.entrySet())
				{
					Connection connection = entry.getValue().getConnection();
					if(connection != null)
						names.put(entry.getKey(), "cn_" + connection.getID());
				}
			}

			public String getName(Handshake handshake) {
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
		return model.getMathModel() instanceof BalsaCircuit;
	}
}