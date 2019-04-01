package org.workcraft.plugins.stg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.ImportUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.ModelEntry;

public class ImportTests {

    @Test
    public void test1() throws IOException, DeserialisationException {
        File tempFile = File.createTempFile("test-", ".g");
        tempFile.deleteOnExit();
        FileOutputStream fileStream = new FileOutputStream(tempFile);
        OutputStreamWriter writer = new OutputStreamWriter(fileStream);
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
    public void test2() throws Throwable {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String resourceName = PackageUtils.getPackagePath(getClass(), "test2.g");
        final InputStream test = classLoader.getResourceAsStream(resourceName);
        StgModel imported = new StgImporter().importStg(test);
        Assert.assertEquals(17, imported.getTransitions().size());
        Assert.assertEquals(0, imported.getDummyTransitions().size());

        int explicitPlaces = 0;
        for (Place p : imported.getPlaces()) {
            if (!((StgPlace) p).isImplicit()) explicitPlaces++;
        }

        Assert.assertEquals(2, explicitPlaces);

        Assert.assertEquals(18, imported.getPlaces().size());

        for (Transition t : imported.getTransitions()) {
            Assert.assertTrue(imported.getPreset(t).size() > 0);
            Assert.assertTrue(imported.getPostset(t).size() > 0);
        }
    }

}
