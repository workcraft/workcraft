package org.workcraft.testing.plugins.balsa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.Framework;
import org.workcraft.framework.ModelSaveFailedException;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.DocumentFormatException;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.LoadFromXMLException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.VisualBalsaCircuit;
import org.workcraft.plugins.balsa.VisualBreezeComponent;
import org.workcraft.plugins.balsa.VisualHandshake;
import org.workcraft.plugins.balsa.components.Loop;
import org.workcraft.plugins.balsa.components.While;

public class SaveLoadTests {

	@Test
	public void TestMathModelLoad() throws Exception
	{
		testMathModelLoadWhileWhile(new FileInputStream("./org/workcraft/testing/plugins/balsa/tests/LoopWhile.work"));
	}

	void testMathModelLoadWhileWhile(InputStream input) throws LoadFromXMLException, DeserialisationException, IOException, DocumentFormatException
	{
		Framework framework = new Framework();
		framework.getPluginManager().loadManifest();

		Model model = framework.load(input);
		BalsaCircuit circuit = (BalsaCircuit)model.getMathModel();

		Assert.assertNull(model.getVisualModel());
		Assert.assertNotNull(circuit);

		Component[] components = circuit.getComponents().toArray(new Component[0]);

		Assert.assertEquals(7, components.length);

		ArrayList<BreezeComponent> brz = new ArrayList<BreezeComponent>();

		for(int i=0;i<components.length;i++)
			if(components[i] instanceof BreezeComponent)
				brz.add((BreezeComponent)components[i]);

		Assert.assertEquals(2, brz.size());

		BreezeComponent brz0 = brz.get(0);
		BreezeComponent brz1 = brz.get(1);

		BreezeComponent loop;
		BreezeComponent wh;

		if(brz0.getUnderlyingComponent() instanceof Loop)
		{
			loop = brz0;
			wh = brz1;
		}
		else
		{
			loop = brz1;
			wh = brz0;
		}

		Assert.assertEquals(Loop.class, loop.getUnderlyingComponent().getClass());
		Assert.assertEquals(While.class, wh.getUnderlyingComponent().getClass());

		Assert.assertTrue(
			loop.getHandshakeComponentByName("activateOut").getConnectedHandshake() == wh.getHandshakeComponentByName("activate")
		);
	}

	@Test
	public void TestVisualModelLoad() throws Exception
	{
		//Model model = Framework.load("./org/workcraft/testing/plugins/balsa/tests/LoopWhile_Visual.work");
		//testVisualModelLoopWhile(model);
	}

	private void testVisualModelLoopWhile(Model model) {
		BalsaCircuit circuit = (BalsaCircuit)model.getMathModel();
		VisualBalsaCircuit visual = (VisualBalsaCircuit)model.getVisualModel();

		Assert.assertNotNull(circuit);
		Assert.assertNotNull(visual);

		Assert.assertEquals(3, visual.getRoot().getChildren().size());

		VisualConnection con = null;
		VisualBreezeComponent wh = null;
		VisualBreezeComponent loop = null;

		for(HierarchyNode node : visual.getRoot().getChildren())
			if(node instanceof VisualConnection)
				con = (VisualConnection)node;
			else if(node instanceof VisualBreezeComponent)
			{
				VisualBreezeComponent brz = (VisualBreezeComponent)node;
				if(brz.getRefComponent().getUnderlyingComponent() instanceof Loop)
					loop = brz;
				if(brz.getRefComponent().getUnderlyingComponent() instanceof While)
					wh = brz;
			}

		Assert.assertNotNull(con);
		Assert.assertNotNull(wh);
		Assert.assertNotNull(loop);

		Assert.assertEquals(3, wh.getChildren().size());
		Assert.assertEquals(2, loop.getChildren().size());

		VisualHandshake whActivate = getVisualHandshakeByName(wh, "activate");
		VisualHandshake loopActivateOut = getVisualHandshakeByName(loop, "activateOut");

		Assert.assertTrue(
				con.getFirst() == whActivate && con.getSecond() == loopActivateOut ||
				con.getSecond() == whActivate && con.getFirst() == loopActivateOut
				);

		Assert.assertEquals(0.0, loop.getX(), 0.5);
		Assert.assertEquals(10.0, wh.getX(), 0.5);
	}

	private VisualHandshake getVisualHandshakeByName(VisualBreezeComponent wh, String name) {
		for(HierarchyNode component : wh.getChildren())
		{
			VisualHandshake handshake = (VisualHandshake) component;
			if(((HandshakeComponent)handshake.getReferencedComponent()).getHandshakeName().equals(name))
				return handshake;
		}
		return null;
	}

	@Test
	public void TestMathModelSaveLoad() throws InvalidConnectionException, ModelSaveFailedException, LoadFromXMLException, IOException, ModelValidationException, SerialisationException
	{
		BalsaCircuit circuit = createWhileWhileMathCircuit();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		new Framework().save(circuit, stream);

		//testMathModelLoadWhileWhile(new ByteArrayInputStream(stream.toByteArray()));
	}

	@Test
	public void TestVisualModelSaveLoad() throws InvalidConnectionException, ModelSaveFailedException, LoadFromXMLException, VisualModelInstantiationException, IOException, ModelValidationException, SerialisationException
	{
		VisualBalsaCircuit circuit = createLoopWhileVisualCircuit();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		//FileOutputStream temp = new FileOutputStream("temp.work");
		//new Framework().save(circuit, temp);
		//temp.close();
		new Framework().save(circuit, stream);
		testVisualModelLoopWhile(circuit);
		/*testVisualModelLoopWhile(
				Framework.load(
				new ByteArrayInputStream(stream.toByteArray())));*/
	}

	private VisualBalsaCircuit createLoopWhileVisualCircuit() throws VisualModelInstantiationException, InvalidConnectionException {
		BalsaCircuit math = new BalsaCircuit();

		VisualBalsaCircuit visual = new VisualBalsaCircuit(math);

		BreezeComponent wh = new BreezeComponent();
		wh.setUnderlyingComponent(new While());
		BreezeComponent loop = new BreezeComponent();
		loop.setUnderlyingComponent(new Loop());
		math.addComponent(wh);
		math.addComponent(loop);
		Connection con = math.connect(loop.getHandshakeComponentByName("activateOut"), wh.getHandshakeComponentByName("activate"));

		VisualBreezeComponent whVis = new VisualBreezeComponent(wh);
		whVis.setX(10);
		visual.addNode(whVis);
		visual.getRoot().add(whVis);
		VisualBreezeComponent loopVis = new VisualBreezeComponent(loop);
		visual.addNode(loopVis);
		visual.getRoot().add(loopVis);

		VisualConnection conVis = new VisualConnection(con, getVisualHandshakeByName(loopVis, "activateOut"),
				getVisualHandshakeByName(whVis, "activate"));
		visual.addNode(conVis);
		visual.getRoot().add(conVis);

		return visual;
	}

	private BalsaCircuit createWhileWhileMathCircuit()
			throws InvalidConnectionException {
		BalsaCircuit circuit = new BalsaCircuit();

		BreezeComponent wh = new BreezeComponent();
		wh.setUnderlyingComponent(new While());
		BreezeComponent loop = new BreezeComponent();
		loop.setUnderlyingComponent(new Loop());
		circuit.addComponent(wh);
		circuit.addComponent(loop);
		circuit.connect(loop.getHandshakeComponentByName("activateOut"), wh.getHandshakeComponentByName("activate"));
		return circuit;
	}


	@Test
	public void TestMathModelSaveLoadSaveLoad() throws InvalidConnectionException, ModelSaveFailedException, LoadFromXMLException, IOException, ModelValidationException, SerialisationException, DocumentFormatException, DeserialisationException
	{
		Framework f = new Framework();
		f.getPluginManager().loadManifest();

		BalsaCircuit circuit = createWhileWhileMathCircuit();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		f.save(circuit, stream);

		Model loaded = f.load(new ByteArrayInputStream(stream.toByteArray()));

		stream = new ByteArrayOutputStream();

		f.save(loaded, stream);

		testMathModelLoadWhileWhile(new ByteArrayInputStream(stream.toByteArray()));
	}


}
