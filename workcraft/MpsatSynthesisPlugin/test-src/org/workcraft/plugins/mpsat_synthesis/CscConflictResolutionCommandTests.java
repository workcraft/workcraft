package org.workcraft.plugins.mpsat_synthesis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_synthesis.commands.CscConflictResolutionCommand;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class CscConflictResolutionCommandTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        MpsatSynthesisSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    void testVmeCscConflictResolution() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testCscConflictResolutionCommand(workName, new String[]{"csc1", "csc"});
    }

    @Test
    void testCycleCscConflictResolution() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-mutex.stg.work");
        testCscConflictResolutionCommand(workName, new String[] {});
    }

    @Test
    void testIrreducibleConflictResolution() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "irreducible_conflict.stg.work");
        testCscConflictResolutionCommand(workName, null);
    }

    private void testCscConflictResolutionCommand(String workName, String[] cscSignals)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry srcWe = framework.loadWork(url.getFile());

        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcInternals = srcStg.getSignalReferences(Signal.Type.INTERNAL);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcMutexes = MutexUtils.getMutexPlaceReferences(srcStg);

        CscConflictResolutionCommand command = new CscConflictResolutionCommand();
        WorkspaceEntry dstWe = command.execute(srcWe);

        if (cscSignals == null) {
            Assertions.assertNull(dstWe);
        } else {
            Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
            Set<String> dstInputs = dstStg.getSignalReferences(Signal.Type.INPUT);
            Set<String> dstInternals = dstStg.getSignalReferences(Signal.Type.INTERNAL);
            Set<String> dstOutputs = dstStg.getSignalReferences(Signal.Type.OUTPUT);
            Set<String> dstMutexes = MutexUtils.getMutexPlaceReferences(dstStg);

            Set<String> expInternals = new HashSet<>(srcInternals);
            expInternals.addAll(Arrays.asList(cscSignals));

            Assertions.assertEquals(srcInputs, dstInputs);
            Assertions.assertEquals(expInternals, dstInternals);
            Assertions.assertEquals(srcOutputs, dstOutputs);
            Assertions.assertEquals(srcMutexes, dstMutexes);
        }
    }

}
