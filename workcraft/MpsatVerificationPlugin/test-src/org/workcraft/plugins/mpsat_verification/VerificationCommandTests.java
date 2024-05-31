package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.*;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class VerificationCommandTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    void testPhilosophersDeadlockVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "philosophers-deadlock.pn.work");
        testVerificationCommands(workName,
                null,  // combined
                null,  // consistency
                false, // deadlock freeness
                null,  // input properness
                null,  // output persistency
                null,  // output determinacy
                null,  // CSC
                null,  // USC
                null,  // absence of self-triggering local signals
                null,  // DI interface
                null,  // normalcy
                null   // mutex implementability
        );
    }

    @Test
    void testPhilosophersNoDeadlockVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "philosophers-no_deadlock.pn.work");
        testVerificationCommands(workName,
                null,  // combined
                null,  // consistency
                true,  // deadlock freeness
                null,  // input properness
                null,  // output persistency
                null,  // output determinacy
                null,  // CSC
                null,  // USC
                null,  // absence of self-triggering local signals
                null,  // DI interface
                null,  // normalcy
                null   // mutex implementability
        );
    }

    @Test
    void testVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testVerificationCommands(workName,
                true,  // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                false, // CSC
                false, // USC
                true,  // absence of self-triggering local signals
                true,  // DI interface
                false, // normalcy
                null   // mutex implementability
        );
    }

    @Test
    void testArbitrationVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3.stg.work");
        testVerificationCommands(workName,
                false,  // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                true,  // CSC
                true,  // USC
                true,  // absence of self-triggering local signals
                false, // DI interface
                false, // normalcy
                true  // mutex implementability
        );
    }

    @Test
    void testBadVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bad.stg.work");
        testVerificationCommands(workName,
                false, // combined
                true,  // consistency
                false, // deadlock freeness
                true,  // input properness
                false, // output persistency
                true,  // output determinacy
                true,  // CSC
                false, // USC
                true,  // absence of self-triggering local signals
                false, // DI interface
                false, // normalcy
                null   // mutex implementability
        );
    }

    @Test
    void testCycleVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle.stg.work");
        testVerificationCommands(workName,
                null, // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                null,  // output persistency
                true,  // output determinacy
                true,  // CSC
                true,  // USC
                true,  // absence of self-triggering local signals
                true,  // DI interface
                true,  // normalcy
                null   // mutex implementability
        );
    }

    @Test
    void testCycleMutexEarlyVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-mutex-early.stg.work");
        testVerificationCommands(workName,
                false, // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                true,  // CSC
                true,  // USC
                true,  // absence of self-triggering local signals
                true,  // DI interface
                false, // normalcy
                false  // mutex implementability
        );
    }

    @Test
    void testCycleMutexLateVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-mutex-late.stg.work");
        testVerificationCommands(workName,
                true, // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                true,  // CSC
                true,  // USC
                true,  // absence of self-triggering local signals
                true,  // DI interface
                false, // normalcy
                true  // mutex implementability
        );
    }

    @Test
    void testInconsistentVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "consistency_violation-no_alternation.stg.work");
        testVerificationCommands(workName,
                false, // combined
                false, // consistency
                null,  // deadlock freeness
                null,  // input properness
                null,  // output persistency
                true,  // output determinacy
                null,  // CSC
                null,  // USC
                null,  // absence of self-triggering local signals
                null,  // DI interface
                null,  // normalcy
                null   // mutex implementability
        );
    }

    @Test
    void testToggleSignalVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(),
                "toggle_signals-no_input_properness-no_output_determinacy.stg.work");

        testVerificationCommands(workName,
                false, // combined
                true,  // consistency
                true,  // deadlock freeness
                false, // input properness
                true,  // output persistency
                false, // output determinacy
                false, // CSC
                false, // USC
                true,  // absence of self-triggering local signals
                true,  // DI interface
                false, // normalcy
                null   // mutex implementability
        );
    }

    @Test
    void testDlatchVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.stg.work");
        testVerificationCommands(workName,
                false, // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                true, // CSC
                true, // USC
                true,  // absence of self-triggering local signals
                false, // DI interface
                false, // normalcy
                null   // mutex implementability
        );
    }

    @Test
    void testInoutPulseVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "inout_pulse.stg.work");
        testVerificationCommands(workName,
                false, // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                false, // CSC
                false, // USC
                false, // absence of self-triggering local signals
                false, // DI interface
                false, // normalcy
                null   // mutex implementability
        );
    }

    @Test
    void testPulserSelfTriggerExceptionsVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "pulser-self_trigger_exceptions.stg.work");
        testVerificationCommands(workName,
                true, // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                false, // CSC
                false, // USC
                true,  // absence of self-triggering local signals
                true,  // DI interface
                false, // normalcy
                null   // mutex implementability
        );
    }

    private void testVerificationCommands(String workName, Boolean combined,
            Boolean consistency, Boolean deadlockFreeness,
            Boolean inputProperness, Boolean outputPersistency, Boolean outputDeterminacy,
            Boolean csc, Boolean usc,
            Boolean localSelfTriggering, Boolean diInterface, Boolean normalcy,
            Boolean mutexImplementability)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        CombinedVerificationCommand combinedCommand = new CombinedVerificationCommand();
        Assertions.assertEquals(combined, combinedCommand.execute(we));

        ConsistencyVerificationCommand consistencyCommand = new ConsistencyVerificationCommand();
        Assertions.assertEquals(consistency, consistencyCommand.execute(we));

        DeadlockFreenessVerificationCommand deadlockCommand = new DeadlockFreenessVerificationCommand();
        Assertions.assertEquals(deadlockFreeness, deadlockCommand.execute(we));

        InputPropernessVerificationCommand inputPropernessCommand = new InputPropernessVerificationCommand();
        Assertions.assertEquals(inputProperness, inputPropernessCommand.execute(we));

        OutputPersistencyVerificationCommand persistencyCommand = new OutputPersistencyVerificationCommand();
        Assertions.assertEquals(outputPersistency, persistencyCommand.execute(we));

        OutputDeterminacyVerificationCommand determinacyCommand = new OutputDeterminacyVerificationCommand();
        Assertions.assertEquals(outputDeterminacy, determinacyCommand.execute(we));

        CscVerificationCommand cscCommand = new CscVerificationCommand();
        Assertions.assertEquals(csc, cscCommand.execute(we));

        UscVerificationCommand uscCommand = new UscVerificationCommand();
        Assertions.assertEquals(usc, uscCommand.execute(we));

        DiInterfaceVerificationCommand diInterfaceCommand = new DiInterfaceVerificationCommand();
        Assertions.assertEquals(diInterface, diInterfaceCommand.execute(we));

        LocalSelfTriggeringVerificationCommand localSelfTriggeringCommand = new LocalSelfTriggeringVerificationCommand();
        Assertions.assertEquals(localSelfTriggering, localSelfTriggeringCommand.execute(we));

        NormalcyVerificationCommand normalcyCommand = new NormalcyVerificationCommand();
        Assertions.assertEquals(normalcy, normalcyCommand.execute(we));

        MutexImplementabilityVerificationCommand mutexImplementabilityCommand = new MutexImplementabilityVerificationCommand();
        Assertions.assertEquals(mutexImplementability, mutexImplementabilityCommand.execute(we));
    }

}
