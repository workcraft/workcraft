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

package org.workcraft.testing.plugins.balsa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.DocumentFormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.LoadFromXMLException;
import org.workcraft.exceptions.ModelSaveFailedException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.VisualBalsaCircuit;
import org.workcraft.plugins.balsa.VisualBreezeComponent;
import org.workcraft.plugins.balsa.VisualHandshake;
import org.workcraft.plugins.balsa.components.Loop;
import org.workcraft.plugins.balsa.components.While;

public class SaveLoadTests {

	//@Test
	public void TestMathModelLoad() throws Exception
	{
		testMathModelLoadWhileWhile(new FileInputStream("./org/workcraft/testing/plugins/balsa/tests/LoopWhile.work"));
	}

	void testMathModelLoadWhileWhile(InputStream input) throws LoadFromXMLException, DeserialisationException, IOException, DocumentFormatException, PluginInstantiationException
	{
		Framework framework = new Framework();
		framework.getPluginManager().loadManifest();

		BalsaCircuit circuit = (BalsaCircuit)framework.load(input);

		Assert.assertNotNull(circuit);

		Node[] components = circuit.getRoot().getChildren().toArray(new Node[0]);

		//TODO: fix the test, considering hierarchical math model structure
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
			circuit.getConnectedHandshake(loop.getHandshakeComponentByName("activateOut")) == wh.getHandshakeComponentByName("activate")
		);
	}

	//@Test
	public void TestVisualModelLoad() throws Exception
	{
		//Model model = Framework.load("./org/workcraft/testing/plugins/balsa/tests/LoopWhile_Visual.work");
		//testVisualModelLoopWhile(model);
	}

	private void testVisualModelLoopWhile(Model model) {
		VisualBalsaCircuit visual = (VisualBalsaCircuit)model;
		BalsaCircuit circuit = (BalsaCircuit)visual.getMathModel();

		Assert.assertNotNull(circuit);
		Assert.assertNotNull(visual);

		Assert.assertEquals(3, visual.getRoot().getChildren().size());

		VisualConnection con = null;
		VisualBreezeComponent wh = null;
		VisualBreezeComponent loop = null;

		for(Node node : visual.getRoot().getChildren())
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
		for(Node component : wh.getChildren())
		{
			VisualHandshake handshake = (VisualHandshake) component;
			if(((HandshakeComponent)handshake.getReferencedComponent()).getHandshakeName().equals(name))
				return handshake;
		}
		return null;
	}

	//@Test
	public void TestMathModelSaveLoad() throws InvalidConnectionException, ModelSaveFailedException, LoadFromXMLException, IOException, ModelValidationException, SerialisationException
	{
		BalsaCircuit circuit = createWhileWhileMathCircuit();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		new Framework().save(circuit, stream);

		//testMathModelLoadWhileWhile(new ByteArrayInputStream(stream.toByteArray()));
	}

	//@Test
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
		math.add(wh);
		math.add(loop);
		MathConnection con = (MathConnection)math.connect(loop.getHandshakeComponentByName("activateOut"), wh.getHandshakeComponentByName("activate"));

		VisualBreezeComponent whVis = new VisualBreezeComponent(wh);
		whVis.setX(10);
		visual.getRoot().add(whVis);
		VisualBreezeComponent loopVis = new VisualBreezeComponent(loop);
		visual.getRoot().add(loopVis);

		VisualConnection conVis = new VisualConnection(con, getVisualHandshakeByName(loopVis, "activateOut"),
				getVisualHandshakeByName(whVis, "activate"));
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
		circuit.add(wh);
		circuit.add(loop);
		circuit.connect(loop.getHandshakeComponentByName("activateOut"), wh.getHandshakeComponentByName("activate"));
		return circuit;
	}


//	@Test
	public void TestMathModelSaveLoadSaveLoad() throws InvalidConnectionException, ModelSaveFailedException, LoadFromXMLException, IOException, ModelValidationException, SerialisationException, DocumentFormatException, DeserialisationException, PluginInstantiationException
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
