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

package org.workcraft.testing.serialisation.xml;

import java.io.IOException;

import org.junit.Test;
import org.workcraft.PluginProvider;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.DocumentFormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.layout.RandomLayout;
import org.workcraft.plugins.serialisation.XMLDeserialiser;
import org.workcraft.plugins.serialisation.XMLSerialiser;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.serialisation.DeserialisationResult;
import org.workcraft.serialisation.ReferenceProducer;
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
		ReferenceProducer mathModelReferences = serialiser.serialise(stg, mathData, null);

		DataAccumulator visualData = new DataAccumulator();
		serialiser.serialise(visualstg, visualData, mathModelReferences);

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
