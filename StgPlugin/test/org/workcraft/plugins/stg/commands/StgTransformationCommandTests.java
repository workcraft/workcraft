package org.workcraft.plugins.stg.commands;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class StgTransformationCommandTests {

    private static final String[] TEST_STG_WORKS = {
        "org/workcraft/plugins/stg/commands/celement.stg.work",
        "org/workcraft/plugins/stg/commands/buck.stg.work",
        "org/workcraft/plugins/stg/commands/vme.stg.work",
    };

    private static final String[] COMPRESSED_HANDSHAKE_STG_WORKS = {
            "org/workcraft/plugins/stg/commands/handshakes-2.stg.work",
            "org/workcraft/plugins/stg/commands/handshakes-3.stg.work",
    };

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins();
    }

    @Test
    public void testMirrorSignalTransformationCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TEST_STG_WORKS) {
            URL url = classLoader.getResource(testStgWork);

            WorkspaceEntry we = framework.loadWork(url.getFile());
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            Set<String> srcInputs = stg.getSignalNames(Type.INPUT, null);
            Set<String> srcOutputs = stg.getSignalNames(Type.OUTPUT, null);
            Set<String> srcInternals = stg.getSignalNames(Type.INTERNAL, null);

            MirrorSignalTransformationCommand command = new MirrorSignalTransformationCommand();
            command.execute(we);
            Set<String> dstInputs = stg.getSignalNames(Type.INPUT, null);
            Set<String> dstOutputs = stg.getSignalNames(Type.OUTPUT, null);
            Set<String> dstInternals = stg.getSignalNames(Type.INTERNAL, null);

            Assert.assertEquals(srcInputs, dstOutputs);
            Assert.assertEquals(srcOutputs, dstInputs);
            Assert.assertEquals(srcInternals, dstInternals);

            framework.closeWork(we);
        }
    }

    @Test
    public void testMirrorTransitionTransformationCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TEST_STG_WORKS) {
            URL url = classLoader.getResource(testStgWork);

            WorkspaceEntry we = framework.loadWork(url.getFile());
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            int srcMinusCount = 0;
            int srcPlusCount = 0;
            int srcToggleCount = 0;
            for (SignalTransition srcTransition: stg.getSignalTransitions()) {
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
            for (SignalTransition dstTransition: stg.getSignalTransitions()) {
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

            Assert.assertEquals(srcMinusCount, dstPlusCount);
            Assert.assertEquals(srcPlusCount, dstMinusCount);
            Assert.assertEquals(srcToggleCount, dstToggleCount);

            framework.closeWork(we);
        }
    }

    @Test
    public void testPlaceTransformationCommands() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TEST_STG_WORKS) {
            URL url = classLoader.getResource(testStgWork);

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

            Assert.assertEquals(srcPlaces + srcImplicitPlaceArcs, expPlaces + expImplicitPlaceArcs);
            Assert.assertEquals(srcSignalTransitions, expSignalTransitions);
            Assert.assertEquals(srcDummyTransitions, expDummyTransitions);

            ImplicitPlaceTransformationCommand command2 = new ImplicitPlaceTransformationCommand();
            command2.execute(we);
            int impPlaces = stg.getVisualPlaces().size();
            int impImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
            int impSignalTransitions = stg.getVisualSignalTransitions().size();
            int impDummyTransitions = stg.getVisualDummyTransitions().size();

            Assert.assertEquals(srcPlaces + srcImplicitPlaceArcs, impPlaces + impImplicitPlaceArcs);
            Assert.assertEquals(srcSignalTransitions, impSignalTransitions);
            Assert.assertEquals(srcDummyTransitions, impDummyTransitions);

            framework.closeWork(we);
        }
    }

    @Test
    public void testExpandHandshakeTransformationCommand() throws IOException, DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: COMPRESSED_HANDSHAKE_STG_WORKS) {
            URL url = classLoader.getResource(testStgWork);

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

            Assert.assertEquals(srcPlaces, dstPlaces);
            Assert.assertEquals(srcSignalTransitions * 2, dstSignalTransitions);
            Assert.assertEquals(srcDummyTransitions, dstDummyTransitions);
            Assert.assertEquals(srcImplicitPlaceArcs + srcSignalTransitions, dstImplicitPlaceArcs);
            Assert.assertEquals(srcConnections + srcSignalTransitions, dstConnections);

            framework.closeWork(we);
        }
    }

}
