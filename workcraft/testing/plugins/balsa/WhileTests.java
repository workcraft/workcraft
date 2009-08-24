package org.workcraft.testing.plugins.balsa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.framework.ModelSaveFailedException;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.LayoutFailedException;
import org.workcraft.framework.exceptions.ModelCheckingFailedException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.framework.util.Import;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakes.MainHandshakeMaker;
import org.workcraft.plugins.balsa.protocols.FourPhaseProtocol;
import org.workcraft.plugins.balsa.stg.MainStgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.HandshakeNameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.interop.BalsaToStgExporter_TwoPhase;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.modelchecking.DeadlockChecker;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;


public class WhileTests {

	@Test
	public void Test1() throws ModelSaveFailedException, VisualModelInstantiationException, ModelCheckingFailedException, ModelValidationException, ExportException, IOException
	{
		final While wh = new While();

		final STG stg = new STG();

		final Map<String, Handshake> handshakes = MainHandshakeMaker.getHandshakes(wh);

		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, new HandshakeNameProvider()
		{
			HashMap<Object, String> names;

			{
				names = new HashMap<Object, String>();
				for(Entry<String, Handshake> entry : handshakes.entrySet())
				{
					names.put(entry.getValue(), entry.getKey());
				}
			}

			public String getName(Object handshake) {
				return names.get(handshake);
			}
		});
		FourPhaseProtocol handshakeBuilder = new FourPhaseProtocol();
		handshakeBuilder.setStgBuilder(stgBuilder);
		MainStgBuilder.buildStg(wh, handshakes, handshakeBuilder);

		new DeadlockChecker().run(stg);

		new org.workcraft.framework.Framework().save(new VisualSTG(stg), "while.stg.work");
	}

	@Test
	public void TestCombine() throws ModelSaveFailedException, VisualModelInstantiationException, InvalidConnectionException, ModelCheckingFailedException, IOException, LayoutFailedException, ModelValidationException, ExportException, ImportException
	{
		BalsaCircuit balsa = new BalsaCircuit();

		BreezeComponent while1 = new BreezeComponent();
		while1.setUnderlyingComponent(new While());
		BreezeComponent while2 = new BreezeComponent();
		while2.setUnderlyingComponent(new While());

		balsa.addComponent(while1);
		balsa.addComponent(while2);

		HandshakeComponent wh1Out = while1.getHandshakeComponents().get(while1.getHandshakes().get("activateOut"));
		HandshakeComponent wh2In = while2.getHandshakeComponents().get(while2.getHandshakes().get("activate"));

		balsa.addConnection(new Connection(wh1Out, wh2In));

		File stgFile = new File("while_while.g");
		WritableByteChannel ch = new FileOutputStream(stgFile).getChannel();
		new BalsaToStgExporter_TwoPhase().export(balsa, ch);
		ch.close();

		final STG stg = (STG) Import.importFromFile(new DotGImporter(), stgFile);

		VisualSTG visualStg = new VisualSTG(stg);

		new org.workcraft.framework.Framework().save(visualStg, "while_while.stg.work");
	}
}
