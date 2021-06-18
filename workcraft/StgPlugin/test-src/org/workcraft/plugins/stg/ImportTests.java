package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.utils.PackageUtils;

import java.io.InputStream;

class ImportTests {

    @Test
    void emptyTest() throws DeserialisationException {
        testStg("empty.g", 0, 0, 0, 0);
    }

    @Test
    void seqMixTest() throws DeserialisationException {
        testStg("seq_mix.g", 17, 0, 18, 2);
    }

    @Test
    void dlatchSplitPlaceHierarchyTest() throws DeserialisationException {
        testStg("dlatch-split_place-hierarchy.g", 8, 0, 8, 4);
    }

    @Test
    void dlatchSplitPlaceReverseTest() throws DeserialisationException {
        testStg("dlatch-split_place-reverse.g", 8, 0, 8, 4);
    }

    @Test
    void bufferNameClashTest() throws DeserialisationException {
        testStg("buffer-name_clash.g", 2, 0, 2, 1);
    }

    private void testStg(String workName, int transitionCount, int dummyCount, int placeCount, int explicitPlaceCount) throws DeserialisationException {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String resourceName = PackageUtils.getPackagePath(getClass(), workName);
        final InputStream stream = classLoader.getResourceAsStream(resourceName);
        StgModel stg = new StgImporter().importStg(stream);

        Assertions.assertEquals(transitionCount, stg.getTransitions().size());
        Assertions.assertEquals(dummyCount, stg.getDummyTransitions().size());
        Assertions.assertEquals(placeCount, stg.getPlaces().size());

        int count = 0;
        for (Place place : stg.getPlaces()) {
            if (!((StgPlace) place).isImplicit()) count++;
        }
        Assertions.assertEquals(explicitPlaceCount, count);

        for (Transition t : stg.getTransitions()) {
            Assertions.assertTrue(stg.getPreset(t).size() > 0);
            Assertions.assertTrue(stg.getPostset(t).size() > 0);
        }
    }

}
