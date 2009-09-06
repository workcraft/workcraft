package org.workcraft.testing.serialisation.xml;

import java.io.IOException;

import org.junit.Test;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.DocumentFormatException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.framework.plugins.PluginProvider;
import org.workcraft.framework.serialisation.DeserialisationResult;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.plugins.layout.RandomLayout;
import org.workcraft.plugins.serialisation.XMLDeserialiser;
import org.workcraft.plugins.serialisation.XMLSerialiser;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.testing.serialisation.SerialisationTestingUtils;
import org.workcraft.util.DataAccumulator;

public class STGSerialisationTests {

		@Test
		public void SimpleVisualSaveLoadWithImplicitArcs() throws InvalidConnectionException,
				SerialisationException, PluginInstantiationException, IOException,
				DocumentFormatException, DeserialisationException, VisualModelInstantiationException {

			VisualSTG stg = XMLSerialisationTestingUtils.createTestSTG3();

			RandomLayout layout = new RandomLayout();
			layout.doLayout(stg);

			// serialise
			PluginProvider mockPluginManager = XMLSerialisationTestingUtils.createMockPluginManager();

			XMLSerialiser serialiser = new XMLSerialiser();
			serialiser.processPlugins(mockPluginManager);

			DataAccumulator mathData = new DataAccumulator();
			ReferenceProducer mathModelReferences = serialiser.export(stg.getMathModel(), mathData, null);

			DataAccumulator visualData = new DataAccumulator();
			serialiser.export(stg, visualData, mathModelReferences);



			 System.out.println (new String (mathData.getData()));
			System.out.println ("---------------");
			System.out.println (new String (visualData.getData()));

			// deserialise
			XMLDeserialiser deserialiser = new XMLDeserialiser();
			deserialiser.processPlugins(mockPluginManager);

			DeserialisationResult mathResult = deserialiser.deserialise(mathData.getInputStream(), null);
			DeserialisationResult visualResult = deserialiser.deserialise(visualData.getInputStream(), mathResult.referenceResolver);

			SerialisationTestingUtils.compareNodes(stg.getRoot(), visualResult.model.getRoot());
		}

}
