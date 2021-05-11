package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.commands.MergePlaceTransformationCommand;
import org.workcraft.plugins.petri.commands.ProxyDirectedArcPlaceTransformationCommand;
import org.workcraft.plugins.petri.utils.ConnectionUtils;
import org.workcraft.plugins.stg.commands.*;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

public class TransformationCommandTests {

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCelementMirrorSignalTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testMirrorSignalTransformationCommand(workName);
    }

    @Test
    public void testBuckMirrorSignalTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.stg.work");
        testMirrorSignalTransformationCommand(workName);
    }

    @Test
    public void testVmeMirrorSignalTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testMirrorSignalTransformationCommand(workName);
    }

    private void testMirrorSignalTransformationCommand(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Set<String> srcInputs = stg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcOutputs = stg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcInternals = stg.getSignalReferences(Signal.Type.INTERNAL);

        MirrorSignalTransformationCommand command = new MirrorSignalTransformationCommand();
        command.execute(we);
        Set<String> dstInputs = stg.getSignalReferences(Signal.Type.INPUT);
        Set<String> dstOutputs = stg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> dstInternals = stg.getSignalReferences(Signal.Type.INTERNAL);

        framework.closeWork(we);
        Assertions.assertEquals(srcInputs, dstOutputs);
        Assertions.assertEquals(srcOutputs, dstInputs);
        Assertions.assertEquals(srcInternals, dstInternals);
    }

    @Test
    public void testCelementMirrorTransitionTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testMirrorTransitionTransformationCommand(workName);
    }

    @Test
    public void testBuckMirrorTransitionTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.stg.work");
        testMirrorTransitionTransformationCommand(workName);
    }

    @Test
    public void testVmeMirrorTransitionTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testMirrorTransitionTransformationCommand(workName);
    }

    private void testMirrorTransitionTransformationCommand(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        int srcMinusCount = 0;
        int srcPlusCount = 0;
        int srcToggleCount = 0;
        for (SignalTransition srcTransition : stg.getSignalTransitions()) {
            switch (srcTransition.getDirection()) {
            case MINUS:
                srcMinusCount++;
                break;
            case PLUS:
                srcPlusCount++;
                break;
            case TOGGLE:
                srcToggleCount++;
                break;
            }
        }

        MirrorTransitionTransformationCommand command = new MirrorTransitionTransformationCommand();
        command.execute(we);
        int dstMinusCount = 0;
        int dstPlusCount = 0;
        int dstToggleCount = 0;
        for (SignalTransition dstTransition : stg.getSignalTransitions()) {
            switch (dstTransition.getDirection()) {
            case MINUS:
                dstMinusCount++;
                break;
            case PLUS:
                dstPlusCount++;
                break;
            case TOGGLE:
                dstToggleCount++;
                break;
            }
        }

        framework.closeWork(we);
        Assertions.assertEquals(srcMinusCount, dstPlusCount);
        Assertions.assertEquals(srcPlusCount, dstMinusCount);
        Assertions.assertEquals(srcToggleCount, dstToggleCount);
    }

    @Test
    public void testCelementPlaceTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testPlaceTransformationCommands(workName);
    }

    @Test
    public void testBuckPlaceTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.stg.work");
        testPlaceTransformationCommands(workName);
    }

    @Test
    public void testVmePlaceTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testPlaceTransformationCommands(workName);
    }

    private void testPlaceTransformationCommands(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);
        int srcPlaces = stg.getVisualPlaces().size();
        int srcImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int srcSignalTransitions = stg.getVisualSignalTransitions().size();
        int srcDummyTransitions = stg.getVisualDummyTransitions().size();

        ExplicitPlaceTransformationCommand command1 = new ExplicitPlaceTransformationCommand();
        command1.execute(we);
        int expPlaces = stg.getVisualPlaces().size();
        int expImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int expSignalTransitions = stg.getVisualSignalTransitions().size();
        int expDummyTransitions = stg.getVisualDummyTransitions().size();

        Assertions.assertEquals(srcPlaces + srcImplicitPlaceArcs, expPlaces + expImplicitPlaceArcs);
        Assertions.assertEquals(srcSignalTransitions, expSignalTransitions);
        Assertions.assertEquals(srcDummyTransitions, expDummyTransitions);

        ImplicitPlaceTransformationCommand command2 = new ImplicitPlaceTransformationCommand();
        command2.execute(we);
        int impPlaces = stg.getVisualPlaces().size();
        int impImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int impSignalTransitions = stg.getVisualSignalTransitions().size();
        int impDummyTransitions = stg.getVisualDummyTransitions().size();

        framework.closeWork(we);
        Assertions.assertEquals(srcPlaces + srcImplicitPlaceArcs, impPlaces + impImplicitPlaceArcs);
        Assertions.assertEquals(srcSignalTransitions, impSignalTransitions);
        Assertions.assertEquals(srcDummyTransitions, impDummyTransitions);
    }

    @Test
    public void testHandshakes2ExpandHandshakeTransformationCommand() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "handshakes-2.stg.work");
        testExpandHandshakeTransformationCommand(workName);
    }

    @Test
    public void testHandshakes3ExpandHandshakeTransformationCommand() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "handshakes-3.stg.work");
        testExpandHandshakeTransformationCommand(workName);
    }

    private void testExpandHandshakeTransformationCommand(String workName) throws IOException, DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);
        int srcPlaces = stg.getVisualPlaces().size();
        int srcImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int srcSignalTransitions = stg.getVisualSignalTransitions().size();
        int srcDummyTransitions = stg.getVisualDummyTransitions().size();
        int srcConnections = stg.getVisualConnections().size();

        stg.selectAll();
        ExpandHandshakeReqAckTransformationCommand command = new ExpandHandshakeReqAckTransformationCommand();
        command.execute(we);
        int dstPlaces = stg.getVisualPlaces().size();
        int dstImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int dstSignalTransitions = stg.getVisualSignalTransitions().size();
        int dstDummyTransitions = stg.getVisualDummyTransitions().size();
        int dstConnections = stg.getVisualConnections().size();

        framework.closeWork(we);
        Assertions.assertEquals(srcPlaces, dstPlaces);
        Assertions.assertEquals(srcSignalTransitions * 2, dstSignalTransitions);
        Assertions.assertEquals(srcDummyTransitions, dstDummyTransitions);
        Assertions.assertEquals(srcImplicitPlaceArcs + srcSignalTransitions, dstImplicitPlaceArcs);
        Assertions.assertEquals(srcConnections + srcSignalTransitions, dstConnections);
    }

    @Test
    public void testVmeSelectAllSignalTransitionsTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testSelectAllSignalTransitionsTransformationCommand(workName, new String[]{"dsr+", "dtack+/1"}, 5);
    }

    private void testSelectAllSignalTransitionsTransformationCommand(String workName, String[] refs, int expCount)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);
        selectVisualComponentsByMathRefs(stg, refs);

        SelectAllSignalTransitionsTransformationCommand command = new SelectAllSignalTransitionsTransformationCommand();
        command.execute(we);
        int count = stg.getSelection().size();

        framework.closeWork(we);
        Assertions.assertEquals(expCount, count);
    }

    @Test
    public void testVmeSignalToDummyTransitionTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testSignalToDummyTransitionTransformationCommand(workName, new String[]{"dsw+", "dtack+/1"});
    }

    private void testSignalToDummyTransitionTransformationCommand(String workName, String[] refs) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);

        int srcSignalTransitionCount = stg.getVisualSignalTransitions().size();
        int srcDummyTransitionCount = stg.getVisualDummyTransitions().size();

        selectVisualComponentsByMathRefs(stg, refs);

        int selectionCount = stg.getSelection().size();

        SignalToDummyTransitionTransformationCommand command = new SignalToDummyTransitionTransformationCommand();
        command.execute(we);

        int dstSignalTransitionCount = stg.getVisualSignalTransitions().size();
        int dstDummyTransitionCount = stg.getVisualDummyTransitions().size();

        framework.closeWork(we);
        Assertions.assertEquals(srcDummyTransitionCount + selectionCount, dstDummyTransitionCount);
        Assertions.assertEquals(srcSignalTransitionCount - selectionCount, dstSignalTransitionCount);
    }

    private void selectVisualComponentsByMathRefs(VisualStg stg, String[] refs) {
        stg.selectNone();
        for (String ref : refs) {
            VisualComponent t = stg.getVisualComponentByMathReference(ref, VisualComponent.class);
            if (t != null) {
                stg.addToSelection(t);
            }
        }
    }

    @Test
    public void testTransitionTransformationCommand() throws DeserialisationException, InvalidConnectionException {
        String workName = PackageUtils.getPackagePath(getClass(), "inv.stg.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);
        checkVisualStgNodeCount(stg, 1, 3, 0, 4, 0, 1, 1, 0);

        VisualSignalTransition outPlus = stg.getVisualComponentByMathReference("out+", VisualSignalTransition.class);
        Assertions.assertNotNull(outPlus);

        VisualStgPlace p1 = stg.createVisualPlace("p1");
        p1.getReferencedComponent().setTokens(1);

        VisualSignalTransition intToggle = stg.createVisualSignalTransition("int", Signal.Type.INTERNAL, SignalTransition.Direction.TOGGLE);

        VisualSignalTransition inPlus = stg.getVisualComponentByMathReference("in+", VisualSignalTransition.class);
        Assertions.assertNotNull(inPlus);

        VisualStgPlace p0 = stg.getVisualComponentByMathReference("p0", VisualStgPlace.class);
        Assertions.assertNotNull(p0);

        VisualConnection connection = stg.getConnection(p0, inPlus);
        Assertions.assertNotNull(connection);

        stg.connect(outPlus, p1);
        stg.connect(p1, intToggle);
        stg.connect(intToggle, inPlus);

        InsertDummyTransformationCommand insertDummyCommand = new InsertDummyTransformationCommand();
        stg.select(connection);
        insertDummyCommand.execute(we);
        checkVisualStgNodeCount(stg, 2, 5, 0, 5, 1, 2, 2, 0);

        DummyToSignalTransitionTransformationCommand dummyToSignalTransitionCommand = new DummyToSignalTransitionTransformationCommand();
        VisualDummyTransition dummy = stg.getVisualComponentByMathReference("dum", VisualDummyTransition.class);
        Assertions.assertNotNull(dummy);
        stg.select(dummy);
        dummyToSignalTransitionCommand.execute(we);
        checkVisualStgNodeCount(stg, 2, 5, 0, 6, 0, 2, 2, 0);

        MergePlaceTransformationCommand mergePlaceCommand = new MergePlaceTransformationCommand();
        stg.select(Arrays.asList(p0, p1));
        mergePlaceCommand.execute(we);
        checkVisualStgNodeCount(stg, 1, 5, 0, 6, 0, 1, 2, 0);

        MergeTransitionTransformationCommand mergeTransitionCommand = new MergeTransitionTransformationCommand();
        VisualSignalTransition sigToggle = stg.getVisualComponentByMathReference("sig~", VisualSignalTransition.class);
        Assertions.assertNotNull(sigToggle);
        stg.select(Arrays.asList(intToggle, sigToggle));
        mergeTransitionCommand.execute(we);
        checkVisualStgNodeCount(stg, 1, 4, 0, 5, 0, 1, 1, 0);

        ContractNamedTransitionTransformationCommand contractTransitionCommand = new ContractNamedTransitionTransformationCommand();
        VisualSignalTransition intsigToggle = stg.getVisualComponentByMathReference("int_sig~", VisualSignalTransition.class);
        if (intsigToggle == null) {
            intsigToggle = stg.getVisualComponentByMathReference("sig_int~", VisualSignalTransition.class);
        }
        Assertions.assertNotNull(intsigToggle);
        stg.select(intsigToggle);
        contractTransitionCommand.execute(we);

        framework.closeWork(we);
        checkVisualStgNodeCount(stg, 1, 3, 0, 4, 0, 1, 1, 0);
    }

    @Test
    public void testTransitionContractionCommand() throws DeserialisationException, InvalidConnectionException {
        String workName = PackageUtils.getPackagePath(getClass(), "inv.stg.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);

        checkVisualStgNodeCount(stg, 1, 3, 0, 4, 0, 1, 1, 0);

        VisualSignalTransition inPlus = stg.getVisualComponentByMathReference("in+", VisualSignalTransition.class);
        Assertions.assertNotNull(inPlus);

        VisualStgPlace p0 = stg.getVisualComponentByMathReference("p0", VisualStgPlace.class);
        Assertions.assertNotNull(p0);

        VisualConnection connection = stg.getConnection(p0, inPlus);
        Assertions.assertNotNull(connection);

        InsertDummyTransformationCommand insertDummyCommand = new InsertDummyTransformationCommand();
        stg.select(connection);
        insertDummyCommand.execute(we);
        checkVisualStgNodeCount(stg, 1, 4, 0, 4, 1, 1, 1, 0);

        stg.select(stg.getConnections(p0));
        new ProxyDirectedArcPlaceTransformationCommand().execute(we);
        checkVisualStgNodeCount(stg, 1, 4, 2, 4, 1, 1, 1, 0);

        ContractNamedTransitionTransformationCommand contractTransitionCommand = new ContractNamedTransitionTransformationCommand();
        VisualDummyTransition dummyTransition = stg.getVisualComponentByMathReference("dum", VisualDummyTransition.class);
        Assertions.assertNotNull(dummyTransition);
        stg.select(dummyTransition);
        contractTransitionCommand.execute(we);

        framework.closeWork(we);
        checkVisualStgNodeCount(stg, 1, 3, 1, 4, 0, 1, 1, 0);
    }

    private void checkVisualStgNodeCount(VisualStg stg, int explicitPlaceCount, int implicitPlaceCount, int replicaPlaceCount,
            int signalTransitionCount, int dummyTransitionCount, int producingArcCount, int consumingArcCount, int readArcCount) {

        Assertions.assertEquals(explicitPlaceCount, stg.getVisualPlaces().size());
        Assertions.assertEquals(implicitPlaceCount, stg.getVisualImplicitPlaceArcs().size());
        Assertions.assertEquals(replicaPlaceCount, ConnectionUtils.getVisualReplicaPlaces(stg).size());
        Assertions.assertEquals(signalTransitionCount, stg.getVisualSignalTransitions().size());
        Assertions.assertEquals(dummyTransitionCount, stg.getVisualDummyTransitions().size());
        Assertions.assertEquals(producingArcCount, ConnectionUtils.getVisualProducingArcs(stg).size());
        Assertions.assertEquals(consumingArcCount, ConnectionUtils.getVisualConsumingArcs(stg).size());
        Assertions.assertEquals(readArcCount, ConnectionUtils.getVisualReadArcs(stg).size());
    }

}
