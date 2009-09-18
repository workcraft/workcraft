package org.workcraft.testing.serialisation.xml;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.DocumentFormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.serialisation.XMLDeserialiser;
import org.workcraft.plugins.serialisation.XMLSerialiser;
import org.workcraft.plugins.stg.STG;
import org.workcraft.testing.serialisation.SerialisationTestingUtils;
import org.workcraft.util.DataAccumulator;

public class MathModelSerialisation {

	public static void compareMathModels(Model model1, Model model2) {
		Assert.assertTrue(model1.getTitle().equals(model2.getTitle()));
		SerialisationTestingUtils.compareNodes (model1.getRoot(), model2.getRoot());
	}

	public void runTest (STG stg) {
		try {
			PluginProvider mock = XMLSerialisationTestingUtils.createMockPluginManager();

			// serialise
			XMLSerialiser serialiser = new XMLSerialiser();
			serialiser.processPlugins(mock);

			DataAccumulator accum = new DataAccumulator();
			serialiser.export(stg, accum, null);

			System.out.println (new String(accum.getData()));

			// deserialise
			XMLDeserialiser deserisaliser = new XMLDeserialiser();
			deserisaliser.processPlugins(mock);

			STG stg2 = (STG)deserisaliser.deserialise(accum.getInputStream(), null).model;

			compareMathModels(stg, stg2);
		} catch (SerialisationException e) {
			throw new RuntimeException(e);
		} catch (DeserialisationException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void SimpleSaveLoad() throws InvalidConnectionException, SerialisationException, PluginInstantiationException, IOException, DocumentFormatException, DeserialisationException {
		runTest (XMLSerialisationTestingUtils.createTestSTG1());
	}

	@Test
	public void SaveLoadWithGroups() throws InvalidConnectionException, SerialisationException, PluginInstantiationException, IOException, DocumentFormatException, DeserialisationException {
		runTest (XMLSerialisationTestingUtils.createTestSTG2());
	}

}
