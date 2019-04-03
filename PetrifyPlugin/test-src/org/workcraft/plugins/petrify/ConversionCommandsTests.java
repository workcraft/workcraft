package org.workcraft.plugins.petrify;

import java.net.URL;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.DesktopApi;
import org.workcraft.plugins.petrify.commands.CscConflictResolutionCommand;
import org.workcraft.plugins.petrify.commands.UntoggleConversionCommand;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class ConversionCommandsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PetrifySettings.setCommand("dist-template/linux/tools/PetrifyTools/petrify");
            break;
        case MACOS:
            PetrifySettings.setCommand("dist-template/osx/Contents/Resources/tools/PetrifyTools/petrify");
            break;
        case WINDOWS:
            PetrifySettings.setCommand("dist-template\\windows\\tools\\PetrifyTools\\petrify.exe");
            break;
        default:
        }
    }

    @Test
    public void bufferUntoggleConversion() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        testUntoggleConversion(workName);
    }

    @Test
    public void celementUntoggleConversion() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-compact.stg.work");
        testUntoggleConversion(workName);
    }

    private void testUntoggleConversion(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcInternals = srcStg.getSignalReferences(Signal.Type.INTERNAL);

        UntoggleConversionCommand command = new UntoggleConversionCommand();
        WorkspaceEntry dstWe = command.execute(srcWe);
        Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
        Set<String> dstInputs = dstStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> dstOutputs = dstStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> dstInternals = dstStg.getSignalReferences(Signal.Type.INTERNAL);

        int dstToggleCount = 0;
        for (SignalTransition dstTransition: dstStg.getSignalTransitions()) {
            if (dstTransition.getDirection() == SignalTransition.Direction.TOGGLE) {
                dstToggleCount++;
            }
        }

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(srcOutputs, dstOutputs);
        Assert.assertEquals(srcInternals, dstInternals);
        Assert.assertEquals(dstToggleCount, 0);
    }

    @Test
    public void toggleCscConflictResolution() throws DeserialisationException {
        testCscConflictResolution("org/workcraft/plugins/petrify/toggle.stg.work");
    }

    @Test
    public void vmeCscConflictResolution() throws DeserialisationException {
        testCscConflictResolution("org/workcraft/plugins/petrify/vme.stg.work");
    }

    private void testCscConflictResolution(String testStgWork) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(testStgWork);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcInternals = srcStg.getSignalReferences(Signal.Type.INTERNAL);

        srcInternals.add("csc0");

        CscConflictResolutionCommand command = new CscConflictResolutionCommand();
        WorkspaceEntry dstWe = command.execute(srcWe);
        Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
        Set<String> dstInputs = dstStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> dstOutputs = dstStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> dstInternals = dstStg.getSignalReferences(Signal.Type.INTERNAL);

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(srcOutputs, dstOutputs);
        Assert.assertEquals(srcInternals, dstInternals);
    }

}
