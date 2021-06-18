package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.Signal.Type;
import org.workcraft.plugins.stg.commands.DtdToStgConversionCommand;
import org.workcraft.plugins.stg.commands.PetriToStgConversionCommand;
import org.workcraft.plugins.stg.commands.StgToPetriConversionCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Set;

class ConversionCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testCelementPetriConversionCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testPetriConversionCommands(workName);
    }

    @Test
    void testBuckPetriConversionCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.stg.work");
        testPetriConversionCommands(workName);
    }

    @Test
    void testVmePetriConversionCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testPetriConversionCommands(workName);
    }

    private void testPetriConversionCommands(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(url.getFile());
        VisualStg srcStg = WorkspaceUtils.getAs(srcWe, VisualStg.class);
        int srcPlaceCount = srcStg.getVisualPlaces().size();
        int srcImplicitPlaceCount = srcStg.getVisualImplicitPlaceArcs().size();
        int srcSignalTransitionCount = srcStg.getVisualSignalTransitions().size();
        int srcDummyTransitionCount = srcStg.getVisualDummyTransitions().size();

        StgToPetriConversionCommand command1 = new StgToPetriConversionCommand();
        WorkspaceEntry midWe = command1.execute(srcWe);
        VisualPetri midPetri = WorkspaceUtils.getAs(midWe, VisualPetri.class);
        int midPlaceCount = midPetri.getVisualPlaces().size();
        int midTransitionCount = midPetri.getVisualTransitions().size();

        Assertions.assertEquals(srcPlaceCount + srcImplicitPlaceCount, midPlaceCount);
        Assertions.assertEquals(srcSignalTransitionCount + srcDummyTransitionCount, midTransitionCount);

        PetriToStgConversionCommand command2 = new PetriToStgConversionCommand();
        WorkspaceEntry dstWe = command2.execute(midWe);
        VisualStg dstStg = WorkspaceUtils.getAs(dstWe, VisualStg.class);
        int dstPlaceCount = dstStg.getVisualPlaces().size();
        int dstImplicitPlaceCount = dstStg.getVisualImplicitPlaceArcs().size();
        int dstSignalTransitionCount = dstStg.getVisualSignalTransitions().size();
        int dstDummyTransitionCount = dstStg.getVisualDummyTransitions().size();

        Assertions.assertEquals(midPlaceCount, dstPlaceCount + dstImplicitPlaceCount);
        Assertions.assertEquals(midTransitionCount, dstDummyTransitionCount);
        Assertions.assertEquals(0, dstSignalTransitionCount);

        framework.closeWork(srcWe);
        framework.closeWork(midWe);
        framework.closeWork(dstWe);
    }

    @Test
    void testTraceToStgConversionCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "trace.dtd.work");
        testDtdToStgConversionCommands(workName);
    }

    private void testDtdToStgConversionCommands(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(url.getFile());
        VisualDtd srcDtd = WorkspaceUtils.getAs(srcWe, VisualDtd.class);

        DtdToStgConversionCommand command = new DtdToStgConversionCommand();
        WorkspaceEntry dstWe = command.execute(srcWe);
        VisualStg dstStg = WorkspaceUtils.getAs(dstWe, VisualStg.class);

        Set<String> srcInputSignalRefs = ReferenceHelper.getReferenceSet(srcDtd,
                srcDtd.getMathModel().getSignals(org.workcraft.plugins.dtd.Signal.Type.INPUT));
        Set<String> dstInputSignalRefs = dstStg.getMathModel().getSignalReferences(Type.INPUT);
        Assertions.assertNotEquals(srcInputSignalRefs, dstInputSignalRefs);

        Set<String> srcOutputSignalRefs = ReferenceHelper.getReferenceSet(srcDtd,
                srcDtd.getMathModel().getSignals(org.workcraft.plugins.dtd.Signal.Type.OUTPUT));

        Set<String> dstOutputSignalRefs = dstStg.getMathModel().getSignalReferences(Type.OUTPUT);
        Assertions.assertNotEquals(srcOutputSignalRefs, dstOutputSignalRefs);

        Set<String> srcInternalSignalRefs = ReferenceHelper.getReferenceSet(srcDtd,
                srcDtd.getMathModel().getSignals(org.workcraft.plugins.dtd.Signal.Type.INPUT));
        Set<String> dstInternalSignalRefs = dstStg.getMathModel().getSignalReferences(Type.INTERNAL);
        Assertions.assertNotEquals(srcInternalSignalRefs, dstInternalSignalRefs);

        int dstPlaceCount = dstStg.getVisualPlaces().size();
        Assertions.assertEquals(8, dstPlaceCount);

        int dstDummyTransitionCount = dstStg.getVisualDummyTransitions().size();
        Assertions.assertEquals(0, dstDummyTransitionCount);

        int srcEdgeCount = srcDtd.getVisualConnections().size()
                - srcDtd.getVisualSignalEntries(null).size()
                - srcDtd.getVisualSignalExits(null).size();

        int dstImplicitPlaceCount = dstStg.getVisualImplicitPlaceArcs().size();
        Assertions.assertEquals(srcEdgeCount, dstImplicitPlaceCount);

        int srcEventCount = srcDtd.getVisualSignalTransitions(null).size();
        int dstSignalTransitionCount = dstStg.getVisualSignalTransitions().size();
        Assertions.assertEquals(srcEventCount, dstSignalTransitionCount);

        framework.closeWork(srcWe);
        framework.closeWork(dstWe);
    }

}
