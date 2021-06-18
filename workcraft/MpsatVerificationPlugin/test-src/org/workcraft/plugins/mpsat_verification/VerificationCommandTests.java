package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.*;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
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
                false,  // deadlock freeness
                null,  // input properness
                null,  // output persistency
                null,  // output determinacy
                null, // CSC
                null, // USC
                null,  // DI interface
                null, // normalcy
                null,  // mutex implementability (late protocol)
                null  // mutex implementability (early protocol)
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
                null, // CSC
                null, // USC
                null,  // DI interface
                null, // normalcy
                null,  // mutex implementability (late protocol)
                null  // mutex implementability (early protocol)
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
                true,  // DI interface
                false, // normalcy
                null,  // mutex implementability (late protocol)
                null  // mutex implementability (early protocol)
        );
    }

    @Test
    void testArbitrationVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3.stg.work");
        testVerificationCommands(workName,
                true,  // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                true,  // CSC
                true,  // USC
                false, // DI interface
                false, // normalcy
                true,  // mutex implementability (late protocol)
                false  // mutex implementability (early protocol)
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
                false, // DI interface
                false, // normalcy
                null,  // mutex implementability (late protocol)
                null  // mutex implementability (early protocol)
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
                true,  // DI interface
                true,  // normalcy
                null,  // mutex implementability (late protocol)
                null  // mutex implementability (early protocol)
        );
    }

    @Test
    void testCycleMutexVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-mutex.stg.work");
        testVerificationCommands(workName,
                true, // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                true,  // CSC
                true,  // USC
                true,  // DI interface
                false,  // normalcy
                true,  // mutex implementability (late protocol)
                false  // mutex implementability (early protocol)
        );
    }

    @Test
    void testInconsistentVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "consistency_violation-no_alternation.stg.work");
        testVerificationCommands(workName,
                false, // combined
                false,  // consistency
                null,  // deadlock freeness
                null,  // input properness
                null,  // output persistency
                true,  // output determinacy
                null,  // CSC
                null,  // USC
                null,  // DI interface
                null,  // normalcy
                null,  // mutex implementability (late protocol)
                null  // mutex implementability (early protocol)
        );
    }

    private void testVerificationCommands(String workName, Boolean combined,
            Boolean consistency, Boolean deadlockFreeness,
            Boolean inputProperness, Boolean outputPersistency, Boolean outputDeterminacy,
            Boolean csc, Boolean usc,
            Boolean diInterface, Boolean normalcy,
            Boolean mutexImplementabilityLateProtocol,
            Boolean mutexImplementabilityEarlyProtocol)
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

        NormalcyVerificationCommand normalcyCommand = new NormalcyVerificationCommand();
        Assertions.assertEquals(normalcy, normalcyCommand.execute(we));

        MutexImplementabilityVerificationCommand mutexImplementabilityCommand = new MutexImplementabilityVerificationCommand();
        setMutexProtocolIfApplicable(we, Mutex.Protocol.LATE);
        Assertions.assertEquals(mutexImplementabilityLateProtocol, mutexImplementabilityCommand.execute(we));

        setMutexProtocolIfApplicable(we, Mutex.Protocol.EARLY);
        Assertions.assertEquals(mutexImplementabilityEarlyProtocol, mutexImplementabilityCommand.execute(we));
    }

    private void setMutexProtocolIfApplicable(WorkspaceEntry we, Mutex.Protocol mutexProtocol) {
        if (WorkspaceUtils.isApplicable(we, Stg.class)) {
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            stg.getMutexPlaces().forEach(mutexPlace -> mutexPlace.setMutexProtocol(mutexProtocol));
        }
    }

}
