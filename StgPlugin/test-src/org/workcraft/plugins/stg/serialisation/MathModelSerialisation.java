package org.workcraft.plugins.stg.serialisation;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.plugins.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.builtin.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.builtin.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.shared.DataAccumulator;

public class MathModelSerialisation {

    public static void compareMathModels(Model model1, Model model2) {
        Assert.assertTrue(model1.getTitle().equals(model2.getTitle()));
        SerialisationTestingUtils.compareNodes(model1.getRoot(), model2.getRoot());
    }

    public void runTest(Stg stg) {
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
        } catch (SerialisationException e) {
            throw new RuntimeException(e);
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void simpleSaveLoad() throws InvalidConnectionException, SerialisationException, PluginInstantiationException, IOException, FormatException, DeserialisationException {
        runTest(XMLSerialisationTestingUtils.createTestSTG1());
    }

    @Test
    public void saveLoadWithGroups() throws InvalidConnectionException, SerialisationException, PluginInstantiationException, IOException, FormatException, DeserialisationException {
        runTest(XMLSerialisationTestingUtils.createTestSTG2());
    }

}
