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

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.stg.STG;
import org.workcraft.testing.serialisation.SerialisationTestingUtils;
import org.workcraft.util.DataAccumulator;

public class MathModelSerialisation {

    public static void compareMathModels(Model model1, Model model2) {
        Assert.assertTrue(model1.getTitle().equals(model2.getTitle()));
        SerialisationTestingUtils.compareNodes(model1.getRoot(), model2.getRoot());
    }

    public void runTest(STG stg) {
        try {
            PluginProvider mock = XMLSerialisationTestingUtils.createMockPluginManager();

            // serialise
            XMLModelSerialiser serialiser = new XMLModelSerialiser(mock);

            DataAccumulator accum = new DataAccumulator();
            serialiser.serialise(stg, accum, null);

            System.out.println(new String(accum.getData()));

            // deserialise
            XMLModelDeserialiser deserisaliser = new XMLModelDeserialiser(mock);

            STG stg2 = (STG) deserisaliser.deserialise(accum.getInputStream(), null, null).model;

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
