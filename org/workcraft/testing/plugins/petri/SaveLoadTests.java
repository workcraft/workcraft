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

package org.workcraft.testing.plugins.petri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.util.Hierarchy;

public class SaveLoadTests {

    private static final String testDataMathModel = "504b0304140008000800901e723c000000000000000000000000090000006d6f64656c2e786d6cc595514fc23010c7dff9144ddfedd816131f368831d1278c89f8016ad7cd4a775dda03e5dbdbc21820242c82e1a5d9dd7afffd7fd75c978dbf6b4d16d23a6520a7311b52224198424195d3b7e9e3cd1d250e39145c1b90390543c7a341569b426a2234772ea7c656eccbd899b0bc44d6e879a5c0b146a255ec25accf1229b1b2cc291d0d08c9eedf1d5a2e70125442c6e71a6b1a6971b911fde40bce34878abd7a05a828015e7b03a8504b4a165ccf7d44a3956074a0985963f0b8c1c2d4ace6f8c1267e79b266deb4e6c0170f69eb2704bd003517b21568c273dc2a788dd5bb4d748451016eb0046fb850b8ecc8e235dae94a343309aeabbbddd665d18e812c0a44e7c225d7854bff152ebd2e5c7249b8a9e5e014faa96e0931243a6b67ea243d74f6c6ecc10048b123230c0cb7eddedf424a651d76e3449cf4bb8b03847e6d3969233e65a3fd6c67a31d840bdb48fe6423bdb48db4d7a1a4bf0ee54837b228dcc0fe5711d5eb3bf907504b0708bed5e9856a0100006b060000504b0304140008000800901e723c000000000000000000000000040000006d6574615d8d390ec32014057b9f02fdfe7bc1d802c9d85d4e901c00b1245658248cb3dc3ed4a9e7cd9b65fb044f5e361f7b8a1286b60762a34e668f7709b7eb053990a3a868944fd14a8809b6b559de293f7556ae60b045ad0d214b50e551dd92bf1855a8d3908cf56dfd07e252ae18cf73371266ab68ef66815a68868c738e62a00ce9449d65239bc65e405723dd7fe507504b0708449d09358e000000ae000000504b01021400140008000800901e723cbed5e9856a0100006b0600000900000000000000000000000000000000006d6f64656c2e786d6c504b01021400140008000800901e723c449d09358e000000ae0000000400000000000000000000000000a10100006d657461504b0506000000000200020069000000610200000000";
    private static final String testDataVisualModel = "504b0304140008000800901e723c000000000000000000000000090000006d6f64656c2e786d6cc595514fc23010c7dff9144ddfedd816131f368831d1278c89f8016ad7cd4a775dda03e5dbdbc21820242c82e1a5d9dd7afffd7fd75c978dbf6b4d16d23a6520a7311b52224198424195d3b7e9e3cd1d250e39145c1b90390543c7a341569b426a2234772ea7c656eccbd899b0bc44d6e879a5c0b146a255ec25accf1229b1b2cc291d0d08c9eedf1d5a2e70125442c6e71a6b1a6971b911fde40bce34878abd7a05a828015e7b03a8504b4a165ccf7d44a3956074a0985963f0b8c1c2d4ace6f8c1267e79b266deb4e6c0170f69eb2704bd003517b21568c273dc2a788dd5bb4d748451016eb0046fb850b8ecc8e235dae94a343309aeabbbddd665d18e812c0a44e7c225d7854bff152ebd2e5c7249b8a9e5e014faa96e0931243a6b67ea243d74f6c6ecc10048b123230c0cb7eddedf424a651d76e3449cf4bb8b03847e6d3969233e65a3fd6c67a31d840bdb48fe6423bdb48db4d7a1a4bf0ee54837b228dcc0fe5711d5eb3bf907504b0708bed5e9856a0100006b060000504b0304140008000800901e723c0000000000000000000000000f00000076697375616c4d6f64656c2e786d6ced995d6f9b301486effb2b90b5db108c818094b48af6a5495b55a91fbb36c6a66cc6b60c49d37f3f1320d0246d9aad93b6245c24e4d8797d8efdbc80c5f86291736b4e7591493101d076804505914926d209b8bdf934088155945824984b4127404870717e36ce6542b945382e8a09903ab51fa4fe493466a5adf82ccd44612b5aeaccbecb8a19e657d5f9252d81a5299b00707e6659e3695c941a93f25ba555454c4c69a9a82e1f5be91f788e6d8e456a5f1b0591024be0dca4516625a7c09a633e33bfc0702938dc501c6b29cbed692632b7e7cbe49a1c3f6b39534d820e68f2a99b6e341605933ac731a79746bc6e7d2e5ffc50da2935fa53c6324157ff06568e4d150ba3bf3033ad1ccb59386bdf5be3abaa574ac326bfe18b0936e9bf98712c25a758b443dc674942c56a6619e6055d1fad378030a77b60c031a1cd1443b04aa9d758b7a9ea14b6c3ae7abc97b932108ab28def010cc731e56bc0f46bdad0deb5f47f73f1a19d2082a08f1c880257b9755330421851889c3866ca7d96899d546ce5e2cfc8d8cac650bc0527ee2e4edc63e6c48912c61889a33018b90a2e6338a24e1c456e121862143c1650d02e50d03183827c4c2327089398b958a1658cf9904604854e14aa817fc89c2c13cf4af38cd3c0e2adc3d2f5a83b2c67e1a86f41c80bdcd80d08f1dc306a88710981c427c4474160283a2a64fc572173d4772397418c7c37a48ee7b5c4c449427c2760a390c487434c6ff740a4109454181476bb2a6da441275847a7eb61b14c17657541aabb1a3573c72ea8f94efa0f3f5b2a4de42cae364175a1586bf9f0958ab4bc5f555b2d40d43bb01af4f87c9de4f72c595344fd430dd01e8adcb0b529e88d3085cd47ac06c18b82bfb50eefae09e6b4da1656dbdb593e782bb1baaca20b34654d3f4caf6ebedc7d7c524aaab1bacfc85e955c49fe58cd5a8347b8ed1ad0f6feff8d12ed6514b7330a3a19e56494be51a073d84e811bfbe24dabc0ce2a4e6715ef649593559e58c53b70ab6c3cb96f5a057556419d55fc93554e56796295d13f6895f1b07acb717e361ee6f57b8f5f504b070865992c4d00030000d5190000504b0304140008000800901e723c000000000000000000000000040000006d657461a5cfbd0e82301405e09da768ee5e7e4a2125a1b0b9b9e903dc945689fd494a417d7b894ebabade73ce97dc7e7c384b361d97397809555e02d15e8569f61709e7d3810a204b423fa10d5e4bf001c621ebef21de544493a8d309878c90de61baeedb149fd4a3dbab2e4cdae6bb0fc484b8c7745de74942ab9195a6eda8ea14a75c0841bb8a71ca1a6634af7953971d146f739b9715ed97fa391dffb4fbe2f78317504b07086c2bfeaca00000000a010000504b01021400140008000800901e723cbed5e9856a0100006b0600000900000000000000000000000000000000006d6f64656c2e786d6c504b01021400140008000800901e723c65992c4d00030000d51900000f00000000000000000000000000a101000076697375616c4d6f64656c2e786d6c504b01021400140008000800901e723c6c2bfeaca00000000a0100000400000000000000000000000000de0400006d657461504b05060000000003000300a6000000b00500000000";

	InputStream stringToStream(String string) throws IOException
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(bytes);
		writer.write(string);
		writer.close();

		return new ByteArrayInputStream(bytes.toByteArray());
	}

	@Test
	public void TestMathModelLoad() throws Exception
	{
		Framework framework = new Framework();
		framework.getPluginManager().loadManifest();

		Model model = framework.load(new Base16Reader(testDataMathModel));
		PetriNet petri = (PetriNet)model;

		Assert.assertNotNull(petri);

		assertPetriEquals(petri, buildSamplePetri());
	}

	@Test
	public void TestVisualModelLoad() throws Exception
	{
		Framework framework = new Framework();
		framework.getPluginManager().loadManifest();

		Model model = framework.load(new Base16Reader(testDataVisualModel));
		VisualPetriNet petriVisual = (VisualPetriNet)model;
		PetriNet petri = (PetriNet)petriVisual.getMathModel();

		Assert.assertNotNull(petriVisual);
		Assert.assertNotNull(petri);

		VisualPetriNet sample = buildSampleVisualPetri();
		assertPetriEquals(petri, (PetriNet)sample.getMathModel());
		assertVisualPetriEquals(petriVisual, sample);
	}

	private void assertVisualPetriEquals(VisualPetriNet petriVisual,
			VisualPetriNet sample) {
		// TODO Auto-generated method stub

	}

	@Test
	public void EnsureSamplesUpToDate() throws Exception
	{
		System.out.println("If the serialisation format has changed, you can use these new serialisation samples:");
		ensureSampleUpToDate("testDataMathModel", buildSamplePetri(), testDataMathModel);
		ensureSampleUpToDate("testDataVisualModel", buildSampleVisualPetri(), testDataVisualModel);
	}

	private void ensureSampleUpToDate(String sampleVarName, Model model, String currentValue) throws SerialisationException, Exception
	{
		Framework f = new Framework();
		f.getPluginManager().loadManifest();
		StringWriter writer = new StringWriter();
		f.save(model, new Base16Writer(writer));
		String generatedValue = writer.toString();
		if(currentValue.equals(generatedValue))
			return;
		System.out.print("    private static final String ");
		System.out.print(sampleVarName);
		System.out.print(" = \"");
		System.out.print(generatedValue);
		System.out.println("\";");
	}

	private Collection<MathNode> getComponents(PetriNet net)
	{
		ArrayList<MathNode> result = new ArrayList<MathNode>(net.getTransitions());
		result.addAll(net.getPlaces());
		return result;
	}

	private Collection<MathConnection> getConnections(PetriNet net)
	{
		return Hierarchy.getChildrenOfType(net.getRoot(), MathConnection.class);
	}

	private void assertPetriEquals(PetriNet expected, PetriNet actual) {
		Assert.assertEquals(getComponents(expected).size(), getComponents(actual).size());
		for(MathNode component : getComponents(expected))
			assertComponentEquals(component,(MathNode) actual.getNodeByReference(expected.getNodeReference(component)));

		Assert.assertEquals(getConnections(expected).size(), getConnections(actual).size());
		for(MathConnection connection : getConnections(expected))
			assertConnectionEquals(connection, (MathConnection) actual.getNodeByReference(expected.getNodeReference(connection)));
	}

	private void assertConnectionEquals(MathConnection expected, MathConnection actual) {
		assertComponentEquals(expected.getFirst(), actual.getFirst());
		assertComponentEquals(expected.getSecond(), actual.getSecond());
	}

	int toHexchar(int ch)
	{
		if(ch<10)
			return '0'+ch;
		else
			return 'a'+ch-10;
	}

	int fromHexchar(int ch)
	{
		if(ch <= 'f' && ch >= 'a')
			return ch-'a'+10;
		if(ch <= 'F' && ch >= 'A')
			return ch-'A'+10;
		if(ch <= '9' && ch >= '0')
			return ch-'0';
		throw new RuntimeException("Hex parse error");
	}

	class Base16Writer extends OutputStream
	{
		private final Writer output;

		public Base16Writer(Writer output)
		{
			this.output = output;
		}

		@Override
		public void write(int b) throws IOException {
			b &= 0xff;
			output.write(toHexchar(b/16));
			output.write(toHexchar(b%16));
		}
	}

	class Base16Reader extends InputStream
	{
		private final Reader stringReader;

		Base16Reader(String string)
		{
			this(new StringReader(string));
		}
		Base16Reader(Reader stringReader)
		{
			this.stringReader = stringReader;
		}

		@Override
		public int read() throws IOException {
			int ch1 = stringReader.read();
			if(ch1 == -1)
				return -1;
			int ch2 = stringReader.read();
			if(ch2 == -1)
				throw new RuntimeException("Length must be even");

			return fromHexchar(ch1)*16+fromHexchar(ch2);
		}
	}

	public void assertComponentEquals(MathNode node, MathNode node2)
	{
		if(node == null)
		{
			Assert.assertNull(node2);
			return;
		}
		Assert.assertNotNull(node2);

		Class<? extends Node> type = node.getClass();
		Assert.assertEquals(type, node2.getClass());

		if(type == Transition.class)
			assertTransitionEquals((Transition)node, (Transition)node2);
		if(type == Place.class)
			assertPlaceEquals((Place)node, (Place)node2);
	}

	private void assertTransitionEquals(Transition expected, Transition actual) {
	}

	private void assertPlaceEquals(Place expected, Place actual) {
		Assert.assertEquals(expected.getTokens(), actual.getTokens());
	}

	private PetriNet buildSamplePetri() throws Exception
	{
		return (PetriNet) buildSampleVisualPetri().getMathModel();
	}

	private VisualPetriNet buildSampleVisualPetri() throws Exception {
		PetriNet petri = new PetriNet();

		Place place1 = new Place();
		place1.setTokens(5);
		Place place2 = new Place();
		place2.setTokens(3);
		Place place3 = new Place();
		place3.setTokens(2);
		petri.add(place1);
		petri.setName(place1, "place1");
		petri.add(place2);
		petri.setName(place2, "place2");
		petri.add(place3);
		petri.setName(place3, "place3");

		Transition trans1 = new Transition();
		petri.setName(trans1, "trans1");
		Transition trans2 = new Transition();
		petri.setName(trans2, "trans2");

		petri.add(trans1);
		petri.add(trans2);

		petri.connect(place1, trans1);
		petri.connect(trans1, place2);
		petri.connect(trans1, place3);
		petri.connect(place3, trans2);


		VisualPetriNet visual = new VisualPetriNet(petri);
/*		VisualPlace vp1 = new VisualPlace(place1);
		VisualPlace vp2 = new VisualPlace(place2);
		VisualPlace vp3 = new VisualPlace(place3);

		VisualTransition vt1 = new VisualTransition(trans1);
		VisualTransition vt2 = new VisualTransition(trans2);

		VisualGroup gr1 = new VisualGroup();
		VisualGroup gr2 = new VisualGroup();
		VisualGroup gr3 = new VisualGroup();

		//todo: add components

		gr1.add(vp1);
		gr1.add(vt2);

		gr2.add(gr1);
		gr2.add(vp2);

		gr3.add(vp3);
		gr3.add(vt2);

		visual.getRoot().add(gr2);
		visual.getRoot().add(gr3);

		VisualConnection vc1 = new VisualConnection(con1, vp1, vt1);
		VisualConnection vc2 = new VisualConnection(con1, vt1, vp2);
		VisualConnection vc3 = new VisualConnection(con1, vt1, vp3);
		VisualConnection vc4 = new VisualConnection(con1, vp3, vt2);

		visual.addConnection(vc1);
		visual.addConnection(vc2);
		visual.addConnection(vc3);
		visual.addConnection(vc4);*/

		r = new Random(1);

		for(Node component : visual.getRoot().getChildren())
			randomPosition(component);

		/*randomPosition(vp1);
		randomPosition(vp2);
		randomPosition(vp3);

		randomPosition(vt1);
		randomPosition(vt2);

		randomPosition(gr1);
		randomPosition(gr2);
		randomPosition(gr3);*/

		return visual;
	}
	Random r;

	private void randomPosition(Node node) {
		if(node instanceof Movable)
			MovableHelper.translate((Movable)node, r.nextDouble()*10, r.nextDouble()*10);
	}
}
