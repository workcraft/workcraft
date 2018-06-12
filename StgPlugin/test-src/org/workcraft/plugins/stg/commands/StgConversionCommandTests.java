package org.workcraft.plugins.stg.commands;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class StgConversionCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCelementPetriConversionCommands() throws IOException, DeserialisationException {
        testPetriConversionCommands("org/workcraft/plugins/stg/celement.stg.work");
    }

    @Test
    public void testBuckPetriConversionCommands() throws IOException, DeserialisationException {
        testPetriConversionCommands("org/workcraft/plugins/stg/buck.stg.work");
    }

    @Test
    public void testVmePetriConversionCommands() throws DeserialisationException {
        testPetriConversionCommands("org/workcraft/plugins/stg/vme.stg.work");
    }

    private void testPetriConversionCommands(String testStgWork) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(testStgWork);

        WorkspaceEntry srcWe = framework.loadWork(url.getFile());
        VisualStg srcStg = WorkspaceUtils.getAs(srcWe, VisualStg.class);
        int srcPlaces = srcStg.getVisualPlaces().size();
        int srcImplicitPlaceArcs = srcStg.getVisualImplicitPlaceArcs().size();
        int srcSignalTransitions = srcStg.getVisualSignalTransitions().size();
        int srcDummyTransitions = srcStg.getVisualDummyTransitions().size();

        StgToPetriConversionCommand command1 = new StgToPetriConversionCommand();
        WorkspaceEntry midWe = command1.execute(srcWe);
        VisualPetriNet midPetri = WorkspaceUtils.getAs(midWe, VisualPetriNet.class);
        int midPlaces = midPetri.getVisualPlaces().size();
        int midTransitions = midPetri.getVisualTransitions().size();

        Assert.assertEquals(srcPlaces + srcImplicitPlaceArcs, midPlaces);
        Assert.assertEquals(srcSignalTransitions + srcDummyTransitions, midTransitions);

        PetriToStgConversionCommand command2 = new PetriToStgConversionCommand();
        WorkspaceEntry dstWe = command2.execute(midWe);
        VisualStg dstStg = WorkspaceUtils.getAs(dstWe, VisualStg.class);
        int dstPlaces = dstStg.getVisualPlaces().size();
        int dstImplicitPlaceArcs = dstStg.getVisualImplicitPlaceArcs().size();
        int dstSignalTransitions = dstStg.getVisualSignalTransitions().size();
        int dstDummyTransitions = dstStg.getVisualDummyTransitions().size();

        Assert.assertEquals(midPlaces, dstPlaces + dstImplicitPlaceArcs);
        Assert.assertEquals(midTransitions, dstDummyTransitions);
        Assert.assertEquals(0, dstSignalTransitions);

        framework.closeWork(srcWe);
        framework.closeWork(midWe);
        framework.closeWork(dstWe);
    }

}
