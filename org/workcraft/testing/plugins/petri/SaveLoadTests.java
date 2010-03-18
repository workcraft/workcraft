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

    private static final String testDataMathModel = "504b0304140008000800b10e723c000000000000000000000000090000006d6f64656c2e786d6cd5965b4fc3201886effd15847ba95d370f49ab31267ab5c544fd018cb28ad28f06be4df7ef056db7755b320f3bc41b021fe1e579df52427af55e6a3291d62903198dd909251284c91514197d7abc3d3ea7c421879c6b0332a360e8d5e5515a9a5c6a2234772ea3c616eccdd85761f90859a5c78502c72a8956b1fbd00e245262e528a3f4f28890f47ae8d07281fda0122abe565953498bd346f4854f38d31c0af6e015a0a00478e90150a196944cb81efb118d3e05a315c5d41a83eb017353b292e333ebfbe6ce9a7155c39dd09a254c0cbcd2d7f007709a0fa55e820b786dc1147cf75be1692e640d17d319cd67b919ada153800d90e015170aa733a6b881dab412cdab04375bd79baf4ba316c04a5abfceab0ab2f1e2464bb945f0b70c3b87cd30d957869d1d66981c36c3cebe324cb69ce1a3e5e014fa6bb60eb24bb74f8e61936dff412be4bd5d91fffddcb6eef61b0320c502f8691b7c3e4f46ca3a0ca79238290ce4e1f344db77b9637f679bfc75e7fe3affd0dff90ffc25ffd0dfc5267fc9dc5fef50fed2283cadfc1b302abf1e5b1f504b0708f46fe9fc93010000440a0000504b0304140008000800b10e723c000000000000000000000000040000006d6574615d8d390ec32014057b9f02fdfe7bc1d802c9d85d4e901c00b1245658248cb3dc3ed4a9e7cd9b65fb044f5e361f7b8a1286b60762a34e668f7709b7eb053990a3a868944fd14a8809b6b559de293f7556ae60b045ad0d214b50e551dd92bf1855a8d3908cf56dfd07e252ae18cf73371266ab68ef66815a68868c738e62a00ce9449d65239bc65e405723dd7fe507504b0708449d09358e000000ae000000504b01021400140008000800b10e723cf46fe9fc93010000440a00000900000000000000000000000000000000006d6f64656c2e786d6c504b01021400140008000800b10e723c449d09358e000000ae0000000400000000000000000000000000ca0100006d657461504b05060000000002000200690000008a0200000000";
    private static final String testDataVisualModel = "504b0304140008000800b10e723c000000000000000000000000090000006d6f64656c2e786d6cd5965b4fc3201886effd15847ba95d370f49ab31267ab5c544fd018cb28ad28f06be4df7ef056db7755b320f3bc41b021fe1e579df52427af55e6a3291d62903198dd909251284c91514197d7abc3d3ea7c421879c6b0332a360e8d5e5515a9a5c6a2234772ea3c616eccdd85761f90859a5c78502c72a8956b1fbd00e245262e528a3f4f28890f47ae8d07281fda0122abe565953498bd346f4854f38d31c0af6e015a0a00478e90150a196944cb81efb118d3e05a315c5d41a83eb017353b292e333ebfbe6ce9a7155c39dd09a254c0cbcd2d7f007709a0fa55e820b786dc1147cf75be1692e640d17d319cd67b919ada153800d90e015170aa733a6b881dab412cdab04375bd79baf4ba316c04a5abfceab0ab2f1e2464bb945f0b70c3b87cd30d957869d1d66981c36c3cebe324cb69ce1a3e5e014fa6bb60eb24bb74f8e61936dff412be4bd5d91fffddcb6eef61b0320c502f8691b7c3e4f46ca3a0ca79238290ce4e1f344db77b9637f679bfc75e7fe3affd0dff90ffc25ffd0dfc5267fc9dc5fef50fed2283cadfc1b302abf1e5b1f504b0708f46fe9fc93010000440a0000504b0304140008000800b10e723c0000000000000000000000000f00000076697375616c4d6f64656c2e786d6ced995d6fda301486effb2b226bb7e4cb242412b442fbd2a4adaad4aebb76fc9166736ccb3194fefb3990a429503ab64eda805c4038366fceb19f37b195f1c5a2e4ce9ceaaa90620202d7070e15589242e413f0f5e6c320014e659020884b4127404870717e362e25a1dcc11c55d504489dbbf752ffc01a31e32a3ecb0b51b98a1a5db8b7453543fcaa3ebfa406389ab20900e7678e339e6695d1089b2fb5561db131a5a5a2da3cb4d2dfd11cb91c89dcbdb60a22078e40a54dc3148653e0cc119fd95fc05b0a7a1b8a632da5d99e2691a53b5f26d7e4f851cb996a12f44193cfaae946235131a94b94717a69c557adcfe58bee8d9b53ab3f65ac10b4fb37704a64ab5858fd851d69e53bfec25ffbde1aefaaee94bc263f6f67824dfa3b33cea4e41489f612770521547423cb10afe8fad57a1710f6740f0c38c2b419e2007429f51adb366fadf1ad2c95e54f9836be072b1c6594afb1d22f6743fba559ff9bf31eb804621844d00f601caa70d5148f208234807e9631153e8bc38b406c45e2cfa0d88a85275e0391700722e13123e2a7843186b3348947a10a963194523f4bd390c41616151c0b23700723f098198111a2a91f27246321527019635140530c133f4dd4203a6444968917c62e6a1a4e86eb9c1c130ac338ccc218e36198a40d0a21c6018e308e601c5b3c8e8a85e88859085980601426d41f0e5b14324270e4c76c94e0ec7050e82df0b11482e29a80ca6d67a58d344cc49b4cb43d1c56e8cad4abd2b6ab53512b49fa77952d651239cbea4dcaaa4aa4b5bcff4c456eeeba52ebd14f7b075283debae6d724bf15644d11f60f35807b28720bd6a6e0708468d07c646a10ef14fcad4978738d11a7f5b6adde7ececac16b89adcaaa1e034d59d377d3ab9b4fb7ef9f94926ba4ee0abc572557923fd4a3d6b0916cbb01b4bdff7f97a42fbb64d8741d3dba243cb9e4e492be4b02ffb06d126cec5c377d023b4b753e894e3e39f9e4894fe081fb64cb4afcb9e749f2e81378f2c9c9274f7c32fa077d32f6ea970fe76763af5cbd8ef809504b07082fdefd87ee0200006c190000504b0304140008000800b20e723c000000000000000000000000040000006d657461a5cfbd0e82301405e09da768ee5e7e4a2125a1b0b9b9e903dc945689fd494a417d7b894ebabade73ce97dc7e7c384b361d97397809555e02d15e8569f61709e7d3810a204b423fa10d5e4bf001c621ebef21de544493a8d309878c90de61baeedb149fd4a3dbab2e4cdae6bb0fc484b8c7745de74942ab9195a6eda8ea14a75c0841bb8a71ca1a6634af7953971d146f739b9715ed97fa391dffb4fbe2f78317504b07086c2bfeaca00000000a010000504b01021400140008000800b10e723cf46fe9fc93010000440a00000900000000000000000000000000000000006d6f64656c2e786d6c504b01021400140008000800b10e723c2fdefd87ee0200006c1900000f00000000000000000000000000ca01000076697375616c4d6f64656c2e786d6c504b01021400140008000800b20e723c6c2bfeaca00000000a0100000400000000000000000000000000f50400006d657461504b05060000000003000300a6000000c70500000000";

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
		Finder<MathNode> componentFinder = new Finder<MathNode>(getComponents(actual), new ComponentLabelExtractor());

		Assert.assertEquals(getComponents(expected).size(), getComponents(actual).size());
		for(MathNode component : getComponents(expected))
			assertComponentEquals(component, componentFinder.getMatching(component));

		Finder<MathConnection> connectionFinder = new Finder<MathConnection>(getConnections(actual), new ConnectionByComponentsIdentifier(new ComponentLabelExtractor()));
		Assert.assertEquals(getConnections(expected).size(), getConnections(actual).size());
		for(MathConnection connection : getConnections(expected))
			assertConnectionEquals(connection, connectionFinder.getMatching(connection));
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

		Assert.assertEquals(node.getLabel(), node2.getLabel());

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
		place1.setLabel("place1");
		petri.add(place2);
		place2.setLabel("place2");
		petri.add(place3);
		place3.setLabel("place3");

		Transition trans1 = new Transition();
		trans1.setLabel("trans1");
		Transition trans2 = new Transition();
		trans2.setLabel("trans2");

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
