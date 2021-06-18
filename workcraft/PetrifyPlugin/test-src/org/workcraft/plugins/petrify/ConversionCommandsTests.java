package org.workcraft.plugins.petrify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.petrify.commands.CscConflictResolutionCommand;
import org.workcraft.plugins.petrify.commands.UntoggleConversionCommand;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Set;

class ConversionCommandsTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PetrifySettings.setCommand(BackendUtils.getTemplateToolPath("PetrifyTools", "petrify"));
    }

    @Test
    void bufferUntoggleConversion() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        testUntoggleConversion(workName);
    }

    @Test
    void celementUntoggleConversion() throws DeserialisationException {
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

        Assertions.assertEquals(srcInputs, dstInputs);
        Assertions.assertEquals(srcOutputs, dstOutputs);
        Assertions.assertEquals(srcInternals, dstInternals);
        Assertions.assertEquals(dstToggleCount, 0);
    }

    @Test
    void toggleCscConflictResolution() throws DeserialisationException {
        testCscConflictResolution("org/workcraft/plugins/petrify/toggle.stg.work");
    }

    @Test
    void vmeCscConflictResolution() throws DeserialisationException {
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

        Assertions.assertEquals(srcInputs, dstInputs);
        Assertions.assertEquals(srcOutputs, dstOutputs);
        Assertions.assertEquals(srcInternals, dstInternals);
    }

}
