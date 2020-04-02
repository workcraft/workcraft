package org.workcraft.plugins.mpsat_verification;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.*;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.net.URL;

public class VerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testPhilosophersDeadlockVerification() throws DeserialisationException {
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
                null,  // mutex implementability (strict)
                null,  // mutex implementability (relaxed)
                null, null // conformation
        );
    }

    @Test
    public void testPhilosophersNoDeadlockVerification() throws DeserialisationException {
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
                null,  // mutex implementability (strict)
                null,  // mutex implementability (relaxed)
                null, null // conformation
        );
    }

    @Test
    public void testVmeVerification() throws DeserialisationException {
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
                null,  // mutex implementability (strict)
                null,  // mutex implementability (relaxed)
                null, null // conformation
        );
    }

    @Test
    public void testArbitrationVerification() throws DeserialisationException {
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
                true,  // mutex implementability (strict)
                false,  // mutex implementability (relaxed)
                null, null // conformation
        );
    }

    @Test
    public void testBadVerification() throws DeserialisationException {
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
                null,  // mutex implementability (strict)
                null,  // mutex implementability (relaxed)
                null, null // conformation
        );
    }

    @Test
    public void testCycleVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle.stg.work");
        testVerificationCommands(workName,
                false, // combined
                true,  // consistency
                true,  // deadlock freeness
                true,  // input properness
                true,  // output persistency
                true,  // output determinacy
                true,  // CSC
                true,  // USC
                true,  // DI interface
                true,  // normalcy
                false,  // mutex implementability (strict)
                false,  // mutex implementability (relaxed)
                "org/workcraft/plugins/mpsat_verification/charge.stg.work", true // conformation
        );
    }

    private void testVerificationCommands(String workName, Boolean combined,
            Boolean consistency, Boolean deadlockFreeness,
            Boolean inputProperness, Boolean outputPersistency, Boolean outputDeterminacy,
            Boolean csc, Boolean usc,
            Boolean diInterface, Boolean normalcy,
            Boolean mutexImplementabilityStrict,
            Boolean mutexImplementabilityRelaxed,
            String envToConform, Boolean conformation)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        CombinedVerificationCommand combinedCommand = new CombinedVerificationCommand();
        Assert.assertEquals(combined, combinedCommand.execute(we));

        ConsistencyVerificationCommand consistencyCommand = new ConsistencyVerificationCommand();
        Assert.assertEquals(consistency, consistencyCommand.execute(we));

        DeadlockFreenessVerificationCommand deadlockCommand = new DeadlockFreenessVerificationCommand();
        Assert.assertEquals(deadlockFreeness, deadlockCommand.execute(we));

        InputPropernessVerificationCommand inputPropernessCommand = new InputPropernessVerificationCommand();
        Assert.assertEquals(inputProperness, inputPropernessCommand.execute(we));

        OutputPersistencyVerificationCommand persistencyCommand = new OutputPersistencyVerificationCommand();
        Assert.assertEquals(outputPersistency, persistencyCommand.execute(we));

        OutputDeterminacyVerificationCommand determinacyCommand = new OutputDeterminacyVerificationCommand();
        Assert.assertEquals(outputDeterminacy, determinacyCommand.execute(we));

        CscVerificationCommand cscCommand = new CscVerificationCommand();
        Assert.assertEquals(csc, cscCommand.execute(we));

        UscVerificationCommand uscCommand = new UscVerificationCommand();
        Assert.assertEquals(usc, uscCommand.execute(we));

        DiInterfaceVerificationCommand diInterfaceCommand = new DiInterfaceVerificationCommand();
        Assert.assertEquals(diInterface, diInterfaceCommand.execute(we));

        NormalcyVerificationCommand normalcyCommand = new NormalcyVerificationCommand();
        Assert.assertEquals(normalcy, normalcyCommand.execute(we));

        MutexImplementabilityVerificationCommand mutexImplementabilityCommand = new MutexImplementabilityVerificationCommand();
        StgSettings.setMutexProtocol(Mutex.Protocol.RELAXED);
        Assert.assertEquals(mutexImplementabilityRelaxed, mutexImplementabilityCommand.execute(we));

        StgSettings.setMutexProtocol(Mutex.Protocol.STRICT);
        Assert.assertEquals(mutexImplementabilityStrict, mutexImplementabilityCommand.execute(we));

        ConformationVerificationCommand conformationCommand = new ConformationVerificationCommand();
        if (envToConform != null) {
            URL envUrl = classLoader.getResource(envToConform);
            File envFile = new File(envUrl.getFile());
            conformationCommand.setEnvironment(envFile);
            Assert.assertEquals(conformation, conformationCommand.execute(we));
        }
    }

}
