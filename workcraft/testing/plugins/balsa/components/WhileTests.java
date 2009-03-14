package org.workcraft.testing.plugins.balsa.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.framework.ModelSaveFailedException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelCheckingFailedException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.components.FourPhaseProtocol;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakes.MainHandshakeMaker;
import org.workcraft.plugins.balsa.stg.MainStgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.HandshakeNameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.modelchecking.DeadlockChecker;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;


public class WhileTests {

	@Test
	public void Test1() throws ModelSaveFailedException, VisualModelInstantiationException, ModelCheckingFailedException
	{
		final While wh = new While();

		final STG stg = new STG();

		final Map<String, Handshake> handshakes = MainHandshakeMaker.getHandshakes(wh);

		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, new HandshakeNameProvider()
		{
			HashMap<Handshake, String> names;

			{
				names = new HashMap<Handshake, String>();
				for(Entry<String, Handshake> entry : handshakes.entrySet())
				{
					names.put(entry.getValue(), entry.getKey());
				}
			}

			public String getName(Handshake handshake) {
				return names.get(handshake);
			}
		});
		FourPhaseProtocol handshakeBuilder = new FourPhaseProtocol(stgBuilder);
		MainStgBuilder.buildStg(wh, handshakes, handshakeBuilder);

		new DeadlockChecker().run(stg);

		new org.workcraft.framework.Framework().save(new VisualSTG(stg), "while.stg.work");
	}

	private void buildStg(BalsaCircuit balsa, STG stg) throws ModelCheckingFailedException
	{
		for(Component component : balsa.getComponents())
		{
			if(component instanceof BreezeComponent)
			{
				final BreezeComponent breezeComponent = (BreezeComponent) component;

				final Map<Handshake, HandshakeComponent> handshakeComponents = breezeComponent.getHandshakeComponents();
				final Map<String, Handshake> handshakes = breezeComponent.getHandshakes();

				StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, new HandshakeNameProvider()
				{
					HashMap<Handshake, String> names;

					{
						names = new HashMap<Handshake, String>();
						for(Entry<String, Handshake> entry : handshakes.entrySet())
						{
							names.put(entry.getValue(), "free_" + breezeComponent.getID() + "_" + entry.getKey());
						}
						for(Entry<Handshake, HandshakeComponent> entry : handshakeComponents.entrySet())
						{
							Connection connection = entry.getValue().getConnection();
							if(connection != null)
								names.put(entry.getKey(), "con_" + connection.getID());
						}
					}

					@Override
					public String getName(Handshake handshake) {
						return names.get(handshake);
					}
				});

				FourPhaseProtocol handshakeBuilder = new FourPhaseProtocol(stgBuilder);
				MainStgBuilder.buildStg(breezeComponent.getUnderlyingComponent(), handshakes, handshakeBuilder);
			}
		}
	}

	@Test
	public void TestCombine() throws ModelSaveFailedException, VisualModelInstantiationException, InvalidConnectionException, ModelCheckingFailedException
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

		final STG stg = new STG();

		buildStg(balsa, stg);

		new org.workcraft.framework.Framework().save(new VisualSTG(stg), "while_while.stg.work");
	}

}
