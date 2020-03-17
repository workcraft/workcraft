package org.workcraft.plugins.stg;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.ImportUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.ModelEntry;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ImportTests {

    @Test
    public void test1() throws IOException, DeserialisationException, OperationCancelledException {
        File tempFile = File.createTempFile("test-", ".g");
        tempFile.deleteOnExit();
        FileOutputStream fileStream = new FileOutputStream(tempFile);
        OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
        writer.write(" \n");
        writer.write(" \n");
        writer.write(".outputs  x\t y   z\n");
        writer.write("\n");
        writer.write(".inputs  a\tb \tc\n");
        writer.write("\n");
        writer.write(" \t.graph\n");
        writer.write("a+ p1 p2\n");
        writer.write("b+ p1 p2\n");
        writer.write(" c+  p1 \t p2\n");
        writer.write("\n");
        writer.write("p1 z+ y+ x+\n");
        writer.write("p2 z+ y+ x+\n");
        writer.write("\n");
        writer.write(".marking { }\n");
        writer.write(".end\n");
        writer.close();
        fileStream.close();

        ModelEntry importedEntry = ImportUtils.importFromFile(new StgImporter(), tempFile);
        Stg imported = (Stg) importedEntry.getModel();

        Assert.assertEquals(6, Hierarchy.getChildrenOfType(imported.getRoot(), Transition.class).size());
        Assert.assertEquals(2, Hierarchy.getChildrenOfType(imported.getRoot(), Place.class).size());
        Assert.assertEquals(12, Hierarchy.getChildrenOfType(imported.getRoot(), Connection.class).size());
    }

    @Test
    public void test2Test() throws DeserialisationException {
        testStg("test2.g", 17, 0, 18, 2);
    }

    @Test
    public void dlatchSplitPlaceHierarchyTest() throws DeserialisationException {
        testStg("dlatch-split_place-hierarchy.g", 8, 0, 8, 4);
    }

    @Test
    public void dlatchSplitPlaceReverseTest() throws DeserialisationException {
        testStg("dlatch-split_place-reverse.g", 8, 0, 8, 4);
    }

    private void testStg(String workName, int transitionCount, int dummyCount, int placeCount, int explicitPlaceCount) throws DeserialisationException {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String resourceName = PackageUtils.getPackagePath(getClass(), workName);
        final InputStream stream = classLoader.getResourceAsStream(resourceName);
        StgModel stg = new StgImporter().importStg(stream);

        Assert.assertEquals(transitionCount, stg.getTransitions().size());
        Assert.assertEquals(dummyCount, stg.getDummyTransitions().size());
        Assert.assertEquals(placeCount, stg.getPlaces().size());

        int count = 0;
        for (Place place : stg.getPlaces()) {
            if (!((StgPlace) place).isImplicit()) count++;
        }
        Assert.assertEquals(explicitPlaceCount, count);

        for (Transition t : stg.getTransitions()) {
            Assert.assertTrue(stg.getPreset(t).size() > 0);
            Assert.assertTrue(stg.getPostset(t).size() > 0);
        }
    }

}
