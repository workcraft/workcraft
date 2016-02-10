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
import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;

public class SaveLoadTests {

    private static final String testDataMathModel = "504b0304140008080800b68a0743000000000000000000000000090000006d6f64656c2e786d6cb595d14ec3201486eff714847be9dac6c48b768b31d1ab1913e703206515470f0d9c4df7f6c2d6cecd3559b3d51bd273caf9f93fc8816cfa5d69b296d62903398dd9981209c2140aca9cbecd1f6fee2871c8a1e0da80cc29183a9d8cb2ca145213a1b9733935b6645fc62e85e50b64b55e950a1cab255ac55ec2f82c91122b1739a5931121d9fdbb43cb05ce824ac8f85c6d4d2d2d6e5ad14fbee64c7328d9ab57809212e09537800ab5a464cdf5ca4734da0a46278a993506bb0d16a66215c70f36f3c39335abba3107be784c1b3f21e805a8b9908d401dbee346c16b6cffb55107a3026cb104afb950b8d993ddeed0ce57a2594a705d75597460208b02d1b570c93070e98570e9bfc2a5c3c02517c22543c2cd2d07a7d0777543882111b74b5ca993f4d0396ab3070320c5818c3030feddeee32964a1acc37d3b1127fdece204a1dfb69cb5119fb3d12cbbb7d134c2c036928b6ca443db487b1d4afae7503a76238bc20dec9f8aa8daddc93f504b07082edc556e6d0100006b060000504b0304140008080800b68a0743000000000000000000000000040000006d6574615d8ebb6ec3201486f73c053a3bc47148644b2659a26cad3ab40f80e0d845058e75c0bdbc7ded0e19bafccb7ffb86eb778ae213b904ca060eaa0181d9910f7932f0f67a971d88526df63652460399e07ad90d5fc41f8eed5865c26a2f3b21068fc571982bb170d196628078528fa09ae332855cd48c95837ad9f419eb13798cb74715f67f5bc9d6f795a3f28fcc36adb7698ba99515c448bcda725982377046db36e3b997ae775aeaaeeb647f68b56c4fed88faa84fc7a6df3687fd7fe25f504b070818897d51be000000fa000000504b01021400140008080800b68a07432edc556e6d0100006b0600000900000000000000000000000000000000006d6f64656c2e786d6c504b01021400140008080800b68a074318897d51be000000fa0000000400000000000000000000000000a40100006d657461504b0506000000000200020069000000940200000000";
    private static final String testDataVisualModel = "504b030414000808080053890743000000000000000000000000090000006d6f64656c2e786d6cb595d14ec3201486eff714847be9dac6c48b768b31d1ab1913e703206515470f0d9c4df7f6c2d6cecd3559b3d51bd273caf9f93fc8816cfa5d69b296d62903398dd9981209c2140aca9cbecd1f6fee2871c8a1e0da80cc29183a9d8cb2ca145213a1b9733935b6645fc62e85e50b64b55e950a1cab255ac55ec2f82c91122b1739a5931121d9fdbb43cb05ce824ac8f85c6d4d2d2d6e5ad14fbee64c7328d9ab57809212e09537800ab5a464cdf5ca4734da0a46278a993506bb0d16a66215c70f36f3c39335abba3107be784c1b3f21e805a8b9908d401dbee346c16b6cffb55107a3026cb104afb950b8d993ddeed0ce57a2594a705d75597460208b02d1b570c93070e98570e9bfc2a5c3c02517c22543c2cd2d07a7d0777543882111b74b5ca993f4d0396ab3070320c5818c3030feddeee32964a1acc37d3b1127fdece204a1dfb69cb5119fb3d12cbbb7d134c2c036928b6ca443db487b1d4afae7503a76238bc20dec9f8aa8daddc93f504b07082edc556e6d0100006b060000504b0304140008080800538907430000000000000000000000000f00000076697375616c4d6f64656c2e786d6ced9a5d6fd3301486eff72b22c36dd3d86ed2446a374d1320c418138c21ae90e33859c0b12d27ddba7f8ff3d1a66bbb7e0c86a024176d7aec9cf31e9ff3a4aa9bd1c934e3d62dd3792ac51840db0116135446a948c6e0f3d5eb9e0facbc2022225c0a3606428293e3a3512623c62dca499e8f81d4897d27f50faa495cd88a4f9254e4b662854eedeb349f107e599e5fb002589ac563008e8f2c6b741ae68526b4785ffa2a2dc6a6b4544c17f733d7dfc92db1391189fdc9781009b004c98c8c222d3803d62de113f309f42b87fd158f232d65b15e662433fbb612d7687ca3e54435021dd0e8a987ae3411792c754642ce2e8cf37af431bde4aeb01366fc9fc6712ad8fc6a6065c4643135fea766a59563395367e97dad7d9ef5dc53bfd1d7df28b091bf517128256744cc42dca451c4c47c6563c273b61c6d218030a77bb4012794354b0cc15cd2c2603da6cad376c2a6753e935ceaf9fac81f4c34169d8463f0c299696fd5577196629fc94c99f616c5de21e394f30711e3ea68c3eeee496a96981e14d16319ecc1072721e34b7ceca5a672b0879047e1ba94795a989b4b258e8949d6dbeb8a05350fec4d66571f2e572bbc52cd6d183f27c8d08e30c5d0c50ec41e52a81ef286986006b11386b1428ff2bd95f0b58cff1ae56b39ef8bdfc13cdac63cea98ef983f04e69d20321d41c3c0f78648c1ca4602e684418022cfd0afe0ff023dde063deea0efa03f04e8b14b58e0787e14c688285cd96217b28062df097cd5730f99f94a7855b706fcc132f8ed8c7a42b50a70414ec768c7e873333af050883c4a07c80f1a4611a590ba94bad8f30cb7ff15a4ee4e90a20ed20ed23f07298a21c12ef2993318cc180da388ba8e170f7d1a1e0ea30b45a4520846cb72e5f6ac2a334b03abb70c6b3bc38a539d17e5b65a3dd57873809533f31e2d7e15afc9349293b0dc4cad13255acbbb732692e2669e6d598060e120aa8736f6f61a975fd268c9235e3c540f3f8139ba3b6e0f2571d39cab8a0643c260f312aa9ef7347e3715f2e5274a382bf7a777447b5767755a796b68d23aff70f6eedbc757e7a7576faf5f9d7f7d9051a289ba49e95e095d4a7e5f2e5ed366feba7bc96cf6bf0f5cb01d38dc02875be0dc0eb80eb8e7000ec2c3260eaeec11af22376891432d721bb7913ae43ae49e8cdce0c0915bf90db81139d822b7f1ef9a0eb90eb9272337fc0b911bf5cba7498e8f46fdac7ebee427504b070879a71ace720300003d230000504b030414000808080053890743000000000000000000000000040000006d657461a590c16ec2301044ef7c85b5779b100c4aa4182e556f543dd00fb0ec4d6ad5f646b603f4ef1b688554ae5cf6303bfb66b4ddfe123c3b61ca8ea28295a8806134645d1c147c1c5f79032c171dadf614514124d8ef16dd99d29749ba2f3c60d1bb05639dc56c921b0b2566bcce5901a541dc8d62f4d3e062162396e4c4fb75be61399045ff723f85e58d1574f99c7b94f4cda30e736cb8dac4dc15584f695ef3697256c116755df5db969bd6482e9ba6e1edaa96bcded43dcab5dcacabf68f797279d2fe1ff5573a3cc9ee968fdff801504b07085f5986b2cf00000056010000504b01021400140008080800538907432edc556e6d0100006b0600000900000000000000000000000000000000006d6f64656c2e786d6c504b010214001400080808005389074379a71ace720300003d2300000f00000000000000000000000000a401000076697375616c4d6f64656c2e786d6c504b01021400140008080800538907435f5986b2cf000000560100000400000000000000000000000000530500006d657461504b05060000000003000300a6000000540600000000";


    InputStream stringToStream(String string) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(bytes);
        writer.write(string);
        writer.close();

        return new ByteArrayInputStream(bytes.toByteArray());
    }

    //@Test
    public void TestMathModelLoad() throws Exception {
        Framework framework = Framework.getInstance();
        framework.getPluginManager().loadManifest();

        final CompatibilityManager compatibilityManager = framework.getCompatibilityManager();
        ByteArrayInputStream bis = compatibilityManager.process(new Base16Reader(testDataMathModel));
        ModelEntry modelEntry = framework.load(bis);
        PetriNet petri = (PetriNet)modelEntry.getModel();

        Assert.assertNotNull(petri);

        assertPetriEquals(petri, buildSamplePetri());
    }

    //@Test
    public void TestVisualModelLoad() throws Exception {
        Framework framework = Framework.getInstance();
        framework.getPluginManager().loadManifest();

        final CompatibilityManager compatibilityManager = framework.getCompatibilityManager();
        ByteArrayInputStream bis = compatibilityManager.process(new Base16Reader(testDataVisualModel));
        ModelEntry modelEntry = framework.load(bis);
        VisualPetriNet petriVisual = (VisualPetriNet)modelEntry.getModel();
        PetriNet petri = (PetriNet)petriVisual.getMathModel();

        Assert.assertNotNull(petriVisual);
        Assert.assertNotNull(petri);

        VisualPetriNet sample = buildSampleVisualPetri();
        assertPetriEquals(petri, (PetriNet)sample.getMathModel());
        assertVisualPetriEquals(petriVisual, sample);
    }

    private void assertVisualPetriEquals(VisualPetriNet petriVisual, VisualPetriNet sample) {
    }

    @Test
    public void EnsureMathSamplesUpToDate() throws Exception {
        System.out.println("If the serialisation format has changed, you can use these new serialisation samples:");
        ensureSampleUpToDate("testDataMathModel", buildSamplePetri(), testDataMathModel);
        ensureSampleUpToDate("testDataVisualModel", buildSampleVisualPetri(), testDataVisualModel);
    }

    private void ensureSampleUpToDate(String sampleVarName, Model model, String currentValue) throws SerialisationException, Exception {
        Framework framework = Framework.getInstance();
        framework.getPluginManager().loadManifest();
        StringWriter writer = new StringWriter();
        framework.save(new ModelEntry(new PetriNetDescriptor(), model), new Base16Writer(writer));
        String generatedValue = writer.toString();
        if(currentValue.equals(generatedValue))
            return;
        System.out.print("    private static final String ");
        System.out.print(sampleVarName);
        System.out.print(" = \"");
        System.out.print(generatedValue);
        System.out.println("\";");
        System.out.println(currentValue);
        System.out.println(generatedValue);
    }

    private Collection<MathNode> getComponents(PetriNet net) {
        ArrayList<MathNode> result = new ArrayList<MathNode>(net.getTransitions());
        result.addAll(net.getPlaces());
        return result;
    }

    private Collection<MathConnection> getConnections(PetriNet net) {
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

    int toHexchar(int ch) {
        if(ch<10)
            return '0'+ch;
        else
            return 'a'+ch-10;
    }

    int fromHexchar(int ch) {
        if(ch <= 'f' && ch >= 'a')
            return ch-'a'+10;
        if(ch <= 'F' && ch >= 'A')
            return ch-'A'+10;
        if(ch <= '9' && ch >= '0')
            return ch-'0';
        throw new RuntimeException("Hex parse error");
    }

    class Base16Writer extends OutputStream {
        private final Writer output;

        Base16Writer(Writer output) {
            this.output = output;
        }

        @Override
        public void write(int b) throws IOException {
            b &= 0xff;
            output.write(toHexchar(b/16));
            output.write(toHexchar(b%16));
        }
    }

    class Base16Reader extends InputStream {
        private final Reader stringReader;

        Base16Reader(String string) {
            this(new StringReader(string));
        }
        Base16Reader(Reader stringReader) {
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

    public void assertComponentEquals(MathNode node, MathNode node2) {
        if(node == null) {
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

    private PetriNet buildSamplePetri() throws Exception {
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
/*        VisualPlace vp1 = new VisualPlace(place1);
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
