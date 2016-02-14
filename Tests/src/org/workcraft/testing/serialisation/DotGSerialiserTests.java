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

package org.workcraft.testing.serialisation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;

public class DotGSerialiserTests {
    //@Test
    //TODO: Make real test.
    public void test() throws IOException, ModelValidationException, SerialisationException, DeserialisationException {
        STG good = getModel(true);

        Set<Place> goodTokenized = getTokenizedPlaces(good);
        ArrayList<String> goodIds = getSortedIds(good, goodTokenized);
        print(goodIds);

        for (int i = 0; i < 50; i++) {
            STG bad = getModel(false);

            Set<Place> badTokenized = getTokenizedPlaces(bad);

            ArrayList<String> badIds = getSortedIds(bad, badTokenized);

            HashSet<String> missing = new HashSet<String>(goodIds);
            missing.removeAll(badIds);

            print(missing);
        }
    }

    private void print(Collection<String> badIds) {
        for (String i : badIds)
            System.out.print(i + " ");
        System.out.println();
    }

    private ArrayList<String> getSortedIds(STG stg, Set<Place> goodTokenized) {
        ArrayList<String> ids = new ArrayList<String>();
        for (Place place : goodTokenized)
            ids.add(stg.getNodeReference(place));
        Collections.sort(ids);
        return ids;
    }

    private Set<Place> getTokenizedPlaces(STG stg) {
        HashSet<Place> places = new HashSet<Place>();
        for (Place place : stg.getPlaces())
            if (place.getTokens() == 1)
                places.add(place);
        return places;
    }

    private STG getModel(boolean good) throws IOException, DeserialisationException {
        int reimportedTokens;
        do {
            ModelEntry importedEntry = Import.importFromFile(new DotGImporter(), new File("D:\\Work\\Out\\fetchA-fetchAmB-varA-muxA.g.ren.g"));
            STG model = (STG) importedEntry.getModel();

            reimportedTokens = getTokenCount(model);

            if ((reimportedTokens == 17) == good)
                return model;
        } while (true);
    }

    private int getTokenCount(STG model) {
        int result = 0;
        for (Place place : model.getPlaces())
            result += place.getTokens();
        return result;
    }
}
