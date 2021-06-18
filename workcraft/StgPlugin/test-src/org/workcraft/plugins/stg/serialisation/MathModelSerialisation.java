package org.workcraft.plugins.stg.serialisation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.PluginProvider;
import org.workcraft.plugins.builtin.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.builtin.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.shared.DataAccumulator;

class MathModelSerialisation {

    @Test
    void simpleSaveLoad() {
        runTest(XMLSerialisationTestingUtils.createTestSTG1());
    }

    @Test
    void saveLoadWithGroups() {
        runTest(XMLSerialisationTestingUtils.createTestSTG2());
    }

    private void runTest(Stg stg) {
        try {
            PluginProvider mock = XMLSerialisationTestingUtils.createMockPluginManager();

            // serialise
            XMLModelSerialiser serialiser = new XMLModelSerialiser(mock);

            DataAccumulator accum = new DataAccumulator();
            serialiser.serialise(stg, accum, null);

            System.out.println(new String(accum.getData()));

            // deserialise
            XMLModelDeserialiser deserisaliser = new XMLModelDeserialiser(mock);

            Stg stg2 = (Stg) deserisaliser.deserialise(accum.getInputStream(), null, null).model;

            compareMathModels(stg, stg2);
        } catch (SerialisationException | DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compareMathModels(Model model1, Model model2) {
        Assertions.assertTrue(model1.getTitle().equals(model2.getTitle()));
        SerialisationTestingUtils.compareNodes(model1.getRoot(), model2.getRoot());
    }

}
