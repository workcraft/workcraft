package org.workcraft.plugins.petrify;

import java.net.URL;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.petrify.commands.PetrifyCscConflictResolutionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyUntoggleConversionCommand;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifyConversionCommandsTests {

    private static final String[] TOGGLE_STG_WORKS = {
        "org/workcraft/plugins/petrify/buffer-compact.stg.work",
        "org/workcraft/plugins/petrify/celement-compact.stg.work",
    };

    private static final String[] CSC_CONFLICT_STG_WORKS = {
        "org/workcraft/plugins/petrify/toggle.stg.work",
        "org/workcraft/plugins/petrify/vme.stg.work",
    };

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins(false);
        switch (DesktopApi.getOs()) {
        case LINUX:
            PetrifyUtilitySettings.setCommand("../dist-template/linux/tools/PetrifyTools/petrify");
            break;
        case MACOS:
            PetrifyUtilitySettings.setCommand("../dist-template/osx/Contents/Resources/tools/PetrifyTools/petrify");
            break;
        case WINDOWS:
            PetrifyUtilitySettings.setCommand("..\\dist-template\\windows\\tools\\PetrifyTools\\petrify.exe");
            break;
        default:
        }
    }

    @Test
    public void testPetrifyUntoggleConversionCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TOGGLE_STG_WORKS) {
            URL srcUrl = classLoader.getResource(testStgWork);

            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
            Set<String> srcInputs = srcStg.getSignalNames(Type.INPUT, null);
            Set<String> srcOutputs = srcStg.getSignalNames(Type.OUTPUT, null);
            Set<String> srcInternals = srcStg.getSignalNames(Type.INTERNAL, null);

            PetrifyUntoggleConversionCommand command = new PetrifyUntoggleConversionCommand();
            WorkspaceEntry dstWe = command.execute(srcWe);
            Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
            Set<String> dstInputs = dstStg.getSignalNames(Type.INPUT, null);
            Set<String> dstOutputs = dstStg.getSignalNames(Type.OUTPUT, null);
            Set<String> dstInternals = dstStg.getSignalNames(Type.INTERNAL, null);

            int dstToggleCount = 0;
            for (SignalTransition dstTransition: dstStg.getSignalTransitions()) {
                if (dstTransition.getDirection() == Direction.TOGGLE) {
                    dstToggleCount++;
                }
            }

            Assert.assertEquals(srcInputs, dstInputs);
            Assert.assertEquals(srcOutputs, dstOutputs);
            Assert.assertEquals(srcInternals, dstInternals);
            Assert.assertEquals(dstToggleCount, 0);
        }
    }

    @Test
    public void testPetrifyCscConflictResolutionCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: CSC_CONFLICT_STG_WORKS) {
            URL srcUrl = classLoader.getResource(testStgWork);

            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
            Set<String> srcInputs = srcStg.getSignalNames(Type.INPUT, null);
            Set<String> srcOutputs = srcStg.getSignalNames(Type.OUTPUT, null);
            Set<String> srcInternals = srcStg.getSignalNames(Type.INTERNAL, null);

            srcInternals.add("csc0");

            PetrifyCscConflictResolutionCommand command = new PetrifyCscConflictResolutionCommand();
            WorkspaceEntry dstWe = command.execute(srcWe);
            Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
            Set<String> dstInputs = dstStg.getSignalNames(Type.INPUT, null);
            Set<String> dstOutputs = dstStg.getSignalNames(Type.OUTPUT, null);
            Set<String> dstInternals = dstStg.getSignalNames(Type.INTERNAL, null);

            Assert.assertEquals(srcInputs, dstInputs);
            Assert.assertEquals(srcOutputs, dstOutputs);
            Assert.assertEquals(srcInternals, dstInternals);
        }
    }

}
