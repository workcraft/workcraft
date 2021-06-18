package org.workcraft.plugins.petri;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkUtils;
import org.workcraft.workspace.ModelEntry;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

class SaveLoadTests {

    private static final String testDataMathModel = "504b0304140008080800ed9a3151000000000000000000000000090000006d6f64656c2e786d6cb595514fc23010c7dff9144ddfedd816131f36d098e8138989f8016a5766a5bb36ed81f2eded604390252c305f9af5b6fbdfff77cd75d9f4bbd2642d9d5706721ab331251284291494397d9b3fdddc51e29143c1b501995330743a19659529a4264273ef736a5cc9be8c5b0ac717c8ac5e950a3cb3129d622ff54a89938b9cd2c98890ece1dda3e30267b5441d0931eb8c950e37ade2275f73a63994ec35a4434909f02a5447855a52b2e67a157634da0a46278a993306bbdd15a66215c70f360bcbb3332bdb9863b4f10241a51799e64236c9b67e8e1b85a0b17dd7ee3af814608b24b8e542e1664f75bbc33a9f896629c177e565d181812caa89ae854b86814b2f844bff152e1d062eb9102e19126eee387885619c1b42ac03715be24a9da487ced1883d1a00290e64eec5f8b7dbc75f9085721ef7d344bc14068a13827e5d39e7223ee7a2a9ba77d18cc1b02e928b5ca403bb487b9d48fae7443a7a9145f5cd1bfe0f51b5bb8b7f00504b0708c94da3bb6a01000060060000504b0304140008080800ed9a3151000000000000000000000000040000006d6574615d90c16ec3201044eff90ac47d09acb163477672a97aeea1fd000a38a53560619cb67f5f125939f4b25aed6ade8ca63ffff8895c6d5a5c0c03158c5362838ec685cb40df5e9fa1a564c92a1835c560071a223d9f76fd774c5f3aa93183b7599d7684f41b8478f519d3402b4abc0bdb96ecd56d0e775c5e9781be1725dddfb5e5e467929d2f0ec89103ef401c88e88e581db161952890757566a04d5d73c48304ad14826c5040db480dc68c423528eb11f5463576d1c9cd3926a227b514cb982eec119dcdd37a716161b3cdc9b197db7c7a48368657f9a33492d32f04758be7a3b1132bad5132c654deb0e5b20af9d874a03b2d41b66d0b9d400958e3686525eb8a773766bfffdfdd1f504b0708c2a1a3490901000084010000504b01021400140008080800ed9a3151c94da3bb6a010000600600000900000000000000000000000000000000006d6f64656c2e786d6c504b01021400140008080800ed9a3151c2a1a34909010000840100000400000000000000000000000000a10100006d657461504b0506000000000200020069000000dc0200000000";
    private static final String testDataVisualModel = "504b0304140008080800ed9a3151000000000000000000000000090000006d6f64656c2e786d6cb595514fc23010c7dff9144ddfedd816131f36d098e8138989f8016a5766a5bb36ed81f2eded604390252c305f9af5b6fbdfff77cd75d9f4bbd2642d9d5706721ab331251284291494397d9b3fdddc51e29143c1b501995330743a19659529a4264273ef736a5cc9be8c5b0ac717c8ac5e950a3cb3129d622ff54a89938b9cd2c98890ece1dda3e30267b5441d0931eb8c950e37ade2275f73a63994ec35a4434909f02a5447855a52b2e67a157634da0a46278a993306bbdd15a66215c70f360bcbb3332bdb9863b4f10241a51799e64236c9b67e8e1b85a0b17dd7ee3af814608b24b8e542e1664f75bbc33a9f896629c177e565d181812caa89ae854b86814b2f844bff152e1d062eb9102e19126eee387885619c1b42ac03715be24a9da487ced1883d1a00290e64eec5f8b7dbc75f9085721ef7d344bc14068a13827e5d39e7223ee7a2a9ba77d18cc1b02e928b5ca403bb487b9d48fae7443a7a9145f5cd1bfe0f51b5bb8b7f00504b0708c94da3bb6a01000060060000504b0304140008080800ed9a31510000000000000000000000000f00000076697375616c4d6f64656c2e786d6ced9a5d739b381486eff32b18edde1a03321866ec74dbceeede6c13cfc4ddbd9684a0b442d2089ca4fdf5151f067f05874eb3de7645663016e2e83dc7e77d26062f5e3de6ccbaa7aac8045f02d7768045391171c6d32578bffe631202ab28118f11139c2e0117e0d5f5d5221731651661a8289640a8d47e10ea135128296dc93669c60b5bd25265f6df59b1416c551d034bd16409c0f595652d5ee3a2548894efaa40d5881e934a48aacacfdbb81fd13db219e2a97da72fe729b038cab586322b1905d63d621bfd0e4ceb80d3a3880b2544795a632c72fbbe56d60afc53898d6c053aa0d5b373aa1939a1110bc128e25b6959f15630866441e34e608258411b9595cea3a8ed3a6b857891089523cce88d4ee2c935ebbaa087d24ea9cee37592649c7657032b47ba5a8f3a8f47fd714ac7721e9d83d793e35d75bb4807929f10b8e0fa70442b3044685b69177439ee9c6ccec9eab09f3054065d72a13af9e213e5ed884af112fce2d4db369b3e9f7ab103016f452e759ff372f4ba49c6d8deb249bdf5cb3e3f925034d5edc1e3c134461886214cd981614649aa038c55f3a4e556a2c84acd9b5a21e59b7c32ea8a1d497be36d7aebdbd5376458edf712f4a3eaef820956fb53f9bdb95daf6fdf1db7f351eb9ee3ca4b92c5b56348a0eb43c7858127bde65430870852173a1827d27b1238e79133e5df033fde39fc78063f063f063f3f227e9c28d6ed4f701406734fbaf5188aa883a3c88b030d22e95e9c3ff01c7fa0e18fe18fe1cf8fc81fe8231a394118e3c44312d66389efd288c0d0894239f12f829f3a625dd39641b34306f5339a09b53c77479b2185218521c57724c52cf0b0171032f3c2a8258547884b7c427c18049a1eff0d54f8cf4285675061506150f122a8f0121741df0ba9339b6d4981e398f84e90cc4382ff0552ecd49708ce29a94a59d8db726d475a640487c8e8675849a68ab2ba07dc4cfd8d38c02aa80e1aeffe5772a2a0b1d8e0eafe7f9329524a3cfc45795a7ee83eceaa30d1ce86e4c41becba1321ffc9e2838870779313382222de60fdee2efb4287340e473ced31321220fbba98eec5e344677344dd7687e524f836c30eb5c7af7704315a3da979a6979f1bac49abe807dab46e6e6f7e1f4ce3e0394eff357aa527a214955595db60a5dad0bd60a942f2434646956425d8e7aafc6dfb87a7e0b39dfd820e8dce3b74d639d4ed1d3a786bd438d438f4a773a8eb5ec8a2eed1738a218f7abd47076f1f1a8f1a8ffe7c1e9d5dcaa3475f8e8f3d0a3b8fc2dea3bef1a8f1e8ffcba3f3311e5d4cab5f715d5f2da679f3bbaeaf504b0708b48ac059b0030000b2260000504b0304140008080800ed9a31510000000000000000000000000900000073746174652e786d6c658c310e833010047b5e71ba9e005d0a1bbabc20798065960871dc49b645783e166dca9dd1ac9bce5de840caaba9e7e1d13341a3cdab7e3d7fdeaff6c9944bd03988293cabf13436ee67698b292ca5adb2606c889ce08050c2e2b9e7ee46198258ea759daefb8b2e504b0708c696633c680000007e000000504b0304140008080800ed9a3151000000000000000000000000040000006d657461a591cb6ec3201444f7f90ac41e82c12f223bd954dd55eaa2fd00ccc3a53560619cb67f5f925891da6d37575717cd99d1d09dbedc04ce3a2e36f81e169840a0bd0ccafab187af2f8fa8856049c22b3105af7be8033c1d77dd67881f320a9390d3491c7700741b0438f11e620f1904cefa6d8bfa6c37872b2ead4b0f87ac84fbab369fdc0c9275d981124a10e1a86840c10f941d688d59d540b0ae56f5b0ac8d5183644891a64425a5060d5c49649891b42646914a6e54a51719ed9c420472124bb60c71c4f7e8789ed6d1fa05cf3a458b9f2ff3e12ed9184ea4b7dc488adfc88b4b3c17949e706e0d0213627e46b75cb51694989a23c965ced5b62de2052d11ada8d1252b2b46f8c6cc5dac62fa45bd9d9efec9eef67fffe507504b0708ff24186219010000e0010000504b01021400140008080800ed9a3151c94da3bb6a010000600600000900000000000000000000000000000000006d6f64656c2e786d6c504b01021400140008080800ed9a3151b48ac059b0030000b22600000f00000000000000000000000000a101000076697375616c4d6f64656c2e786d6c504b01021400140008080800ed9a3151c696633c680000007e00000009000000000000000000000000008e05000073746174652e786d6c504b01021400140008080800ed9a3151ff24186219010000e001000004000000000000000000000000002d0600006d657461504b05060000000004000400dd000000780700000000";

    @BeforeAll
    static void init() {
        Framework.getInstance().init();
    }

    @Test
    void testMathModelLoad()
            throws IOException, DeserialisationException, InvalidConnectionException {

        CompatibilityManager compatibilityManager = Framework.getInstance().getCompatibilityManager();
        ByteArrayInputStream bis = compatibilityManager.process(new Base16Reader(testDataMathModel), null);
        ModelEntry modelEntry = WorkUtils.loadModel(bis);
        Petri petri = (Petri) modelEntry.getModel();

        Assertions.assertNotNull(petri);

        assertPetriEquals(petri, buildSamplePetri());
    }

    @Test
    void testVisualModelLoad()
            throws IOException, DeserialisationException, InvalidConnectionException {

        CompatibilityManager compatibilityManager = Framework.getInstance().getCompatibilityManager();
        ByteArrayInputStream bis = compatibilityManager.process(new Base16Reader(testDataVisualModel), null);
        ModelEntry modelEntry = WorkUtils.loadModel(bis);

        VisualPetri petriVisual = (VisualPetri) modelEntry.getModel();
        Assertions.assertNotNull(petriVisual);

        Petri petri = petriVisual.getMathModel();
        Assertions.assertNotNull(petri);

        VisualPetri sample = buildSampleVisualPetri();
        assertPetriEquals(petri, sample.getMathModel());
    }

    @Test
    void ensureMathSamplesUpToDate() throws InvalidConnectionException, SerialisationException {

        System.out.println("If the serialisation format has changed, you can use these new serialisation samples:");
        ensureSampleUpToDate("testDataMathModel", buildSamplePetri(), testDataMathModel);
        ensureSampleUpToDate("testDataVisualModel", buildSampleVisualPetri(), testDataVisualModel);
    }

    private void ensureSampleUpToDate(String sampleVarName, Model model, String currentValue)
            throws SerialisationException {

        StringWriter writer = new StringWriter();
        WorkUtils.saveModel(new ModelEntry(new PetriDescriptor(), model), null, new Base16Writer(writer));
        String generatedValue = writer.toString();
        if (currentValue.equals(generatedValue)) {
            return;
        }
        System.out.print("    private static final String ");
        System.out.print(sampleVarName);
        System.out.print(" = \"");
        System.out.print(generatedValue);
        System.out.println("\";");
        System.out.println(currentValue);
        System.out.println(generatedValue);
    }

    private Collection<MathNode> getComponents(Petri net) {
        ArrayList<MathNode> result = new ArrayList<>(net.getTransitions());
        result.addAll(net.getPlaces());
        return result;
    }

    private Collection<MathConnection> getConnections(Petri net) {
        return Hierarchy.getChildrenOfType(net.getRoot(), MathConnection.class);
    }

    private void assertPetriEquals(Petri expected, Petri actual) {
        Assertions.assertEquals(getComponents(expected).size(), getComponents(actual).size());
        for (MathNode component : getComponents(expected)) {
            assertComponentEquals(component, actual.getNodeByReference(expected.getNodeReference(component)));
        }

        Assertions.assertEquals(getConnections(expected).size(), getConnections(actual).size());
        for (MathConnection connection : getConnections(expected)) {
            String connectionRef = expected.getNodeReference(connection);
            MathConnection mathConnection = (MathConnection) actual.getNodeByReference(connectionRef);
            assertConnectionEquals(connection, mathConnection);
        }
    }

    private void assertConnectionEquals(MathConnection expected, MathConnection actual) {
        assertComponentEquals(expected.getFirst(), actual.getFirst());
        assertComponentEquals(expected.getSecond(), actual.getSecond());
    }

    private int toHexchar(int ch) {
        if (ch < 10) {
            return '0' + ch;
        } else {
            return 'a' + ch - 10;
        }
    }

    private int fromHexchar(int ch) {
        if (ch <= 'f' && ch >= 'a') {
            return ch - 'a' + 10;
        }
        if (ch <= 'F' && ch >= 'A') {
            return ch - 'A' + 10;
        }
        if (ch <= '9' && ch >= '0') {
            return ch - '0';
        }
        throw new RuntimeException("Hex parse error");
    }

    private class Base16Writer extends OutputStream {
        private final Writer output;

        Base16Writer(Writer output) {
            this.output = output;
        }

        @Override
        public void write(int b) throws IOException {
            b &= 0xff;
            output.write(toHexchar(b / 16));
            output.write(toHexchar(b % 16));
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
            if (ch1 == -1) {
                return -1;
            }
            int ch2 = stringReader.read();
            if (ch2 == -1) {
                throw new RuntimeException("Length must be even");
            }

            return fromHexchar(ch1) * 16 + fromHexchar(ch2);
        }
    }

    public void assertComponentEquals(MathNode node, MathNode node2) {
        if (node == null) {
            Assertions.assertNull(node2);
            return;
        }
        Assertions.assertNotNull(node2);

        Class<? extends Node> type = node.getClass();
        Assertions.assertEquals(type, node2.getClass());

        if (type == Place.class) {
            assertPlaceEquals((Place) node, (Place) node2);
        }
    }

    private void assertPlaceEquals(Place expected, Place actual) {
        Assertions.assertEquals(expected.getTokens(), actual.getTokens());
    }

    private Petri buildSamplePetri() throws InvalidConnectionException {
        return buildSampleVisualPetri().getMathModel();
    }

    private VisualPetri buildSampleVisualPetri() throws InvalidConnectionException {
        Petri petri = new Petri();

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

        VisualPetri visual = new VisualPetri(petri);
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

        Random r = new Random(1);
        for (Node component : visual.getRoot().getChildren()) {
            randomPosition(component, r);
        }

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

    private void randomPosition(Node node, Random r) {
        if (node instanceof Movable) {
            MovableHelper.translate((Movable) node, r.nextDouble() * 10, r.nextDouble() * 10);
        }
    }

}
