package org.workcraft.testing.plugins.stg.serialisation;

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
import org.workcraft.plugins.stg.Stg;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;

public class DotGSerialiserTests {
    //@Test
    //TODO: Make real test.
    public void test() throws IOException, ModelValidationException, SerialisationException, DeserialisationException {
        Stg good = getModel(true);

        Set<Place> goodTokenized = getTokenizedPlaces(good);
        ArrayList<String> goodIds = getSortedIds(good, goodTokenized);
        print(goodIds);

        for (int i = 0; i < 50; i++) {
            Stg bad = getModel(false);

            Set<Place> badTokenized = getTokenizedPlaces(bad);

            ArrayList<String> badIds = getSortedIds(bad, badTokenized);

            HashSet<String> missing = new HashSet<>(goodIds);
            missing.removeAll(badIds);

            print(missing);
        }
    }

    private void print(Collection<String> badIds) {
        for (String i : badIds) {
            System.out.print(i + " ");
        }
        System.out.println();
    }

    private ArrayList<String> getSortedIds(Stg stg, Set<Place> goodTokenized) {
        ArrayList<String> ids = new ArrayList<>();
        for (Place place : goodTokenized) {
            ids.add(stg.getNodeReference(place));
        }
        Collections.sort(ids);
        return ids;
    }

    private Set<Place> getTokenizedPlaces(Stg stg) {
        HashSet<Place> places = new HashSet<>();
        for (Place place : stg.getPlaces()) {
            if (place.getTokens() == 1) {
                places.add(place);
            }
        }
        return places;
    }

    private Stg getModel(boolean good) throws IOException, DeserialisationException {
        int reimportedTokens;
        do {
            ModelEntry importedEntry = Import.importFromFile(new DotGImporter(), new File("D:\\Work\\Out\\fetchA-fetchAmB-varA-muxA.g.ren.g"));
            Stg model = (Stg) importedEntry.getModel();

            reimportedTokens = getTokenCount(model);

            if ((reimportedTokens == 17) == good) {
                return model;
            }
        } while (true);
    }

    private int getTokenCount(Stg model) {
        int result = 0;
        for (Place place : model.getPlaces()) {
            result += place.getTokens();
        }
        return result;
    }
}
