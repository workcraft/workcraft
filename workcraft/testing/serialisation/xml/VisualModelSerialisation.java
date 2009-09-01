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
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.testing.serialisation.SerialisationTestingUtils;
import org.workcraft.util.DataAccumulator;

public class VisualModelSerialisation {

	@Test
	public void SimpleSaveLoad() throws InvalidConnectionException,
			SerialisationException, PluginInstantiationException, IOException,
			DocumentFormatException, DeserialisationException, VisualModelInstantiationException {

		STG stg = XMLSerialisationTestingUtils.createTestSTG1();
		VisualSTG visualstg = new VisualSTG(stg);

		RandomLayout layout = new RandomLayout();
		layout.doLayout(visualstg);

		// serialise
		PluginProvider mockPluginManager = XMLSerialisationTestingUtils.createMockPluginManager();

		XMLSerialiser serialiser = new XMLSerialiser();
		serialiser.processPlugins(mockPluginManager);

		DataAccumulator mathData = new DataAccumulator();
		ReferenceProducer mathModelReferences = serialiser.export(stg, mathData, null);

		DataAccumulator visualData = new DataAccumulator();
		serialiser.export(visualstg, visualData, mathModelReferences);

		/* System.out.println (new String (mathData.getData()));
		System.out.println ("---------------");
		System.out.println (new String (visualData.getData())); */

		// deserialise
		XMLDeserialiser deserialiser = new XMLDeserialiser();
		deserialiser.processPlugins(mockPluginManager);

		DeserialisationResult mathResult = deserialiser.deserialise(mathData.getInputStream(), null);
		DeserialisationResult visualResult = deserialiser.deserialise(visualData.getInputStream(), mathResult.referenceResolver);

		SerialisationTestingUtils.compareNodes(visualstg.getRoot(), visualResult.model.getRoot());
	}
}
