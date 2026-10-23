package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.mpsat_verification.commands.*;
import org.workcraft.plugins.mpsat_verification.presets.InputPropernessDataPreserver;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.verification.VerificationGoal;
import org.workcraft.verification.VerificationRecord;
import org.workcraft.verification.VerificationResultStore;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Cached results are reused if the tools are made unavailable after the first verification run. In this case
 * a command can return a definite outcome only from the cache, otherwise it fails and returns null.
 */
class CachedVerificationTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        Framework.getInstance().init();
    }

    @BeforeEach
    void enableTools() {
        // Other tests may have left works open, and the cached results of an open work would be reused
        Framework.getInstance().closeAllWorks();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @AfterEach
    void restoreSettings() {
        enableTools();
    }

    private void disableTools() {
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp-missing"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat-missing"));
    }

    private WorkspaceEntry loadWork(String name) throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), name);
        URL url = ClassLoader.getSystemClassLoader().getResource(workName);
        return Framework.getInstance().loadWork(url.getFile());
    }

    private void checkRecord(WorkspaceEntry we, VerificationGoal goal, VerificationRecord.Outcome outcome) {
        VerificationResultStore.Lookup lookup = new VerificationResultStore(we).lookup(goal);
        Assertions.assertTrue(lookup.isValid(), goal.getId() + " " + lookup.getStatus() + " " + lookup.getReason());
        Assertions.assertEquals(outcome, lookup.getRecord().outcome(), goal.getId());
    }

    @Test
    void testIndividualCommandsReuseCombinedResult() throws DeserialisationException {
        WorkspaceEntry we = loadWork("vme.stg.work");
        Assertions.assertTrue(new CombinedVerificationCommand().execute(we));
        for (VerificationGoal goal : StgVerificationGoals.getCombinedGoals(true)) {
            checkRecord(we, goal, VerificationRecord.Outcome.PASS);
        }

        disableTools();
        Assertions.assertTrue(new CombinedVerificationCommand().execute(we));
        Assertions.assertTrue(new ConsistencyVerificationCommand().execute(we));
        Assertions.assertTrue(new OutputDeterminacyVerificationCommand().execute(we));
        Assertions.assertTrue(new InputPropernessVerificationCommand().execute(we));
        Assertions.assertTrue(new OutputPersistencyVerificationCommand().execute(we));
        Assertions.assertTrue(new LocalSelfTriggeringVerificationCommand().execute(we));
        Assertions.assertTrue(new DeadlockFreenessVerificationCommand().execute(we));
        Assertions.assertTrue(new DiInterfaceVerificationCommand().execute(we));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testFailedResultIsCached() throws DeserialisationException {
        WorkspaceEntry we = loadWork("bad.stg.work");
        Assertions.assertFalse(new DeadlockFreenessVerificationCommand().execute(we));
        checkRecord(we, StgVerificationGoals.DEADLOCK_FREENESS, VerificationRecord.Outcome.FAIL);

        disableTools();
        Assertions.assertFalse(new DeadlockFreenessVerificationCommand().execute(we));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testResultForPetriNetIsNotCached() throws DeserialisationException {
        // Deadlock freeness can be checked for a Petri net, but its goal is defined for STG models
        WorkspaceEntry we = loadWork("philosophers-deadlock.pn.work");
        Assertions.assertFalse(new DeadlockFreenessVerificationCommand().execute(we));
        Assertions.assertNull(new VerificationResultStore(we).getRecord(StgVerificationGoals.DEADLOCK_FREENESS));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testUndeterminedResultIsCachedButNotReused() throws DeserialisationException {
        // Inconsistent STG makes the other properties impossible to verify, which is a property of the model
        WorkspaceEntry we = loadWork("consistency_violation-no_alternation.stg.work");
        Assertions.assertNull(new DeadlockFreenessVerificationCommand().execute(we));
        checkRecord(we, StgVerificationGoals.DEADLOCK_FREENESS, VerificationRecord.Outcome.UNDETERMINED);
        VerificationRecord record = new VerificationResultStore(we).getRecord(StgVerificationGoals.DEADLOCK_FREENESS);
        Assertions.assertEquals("Cannot build unfolding prefix for the inconsistent STG: "
                + "rising and falling edges of signal 'out' do not alternate", record.message());

        // The verification is repeated rather than reused, which fails without the tools
        disableTools();
        Assertions.assertNull(new DeadlockFreenessVerificationCommand().execute(we));
        checkRecord(we, StgVerificationGoals.DEADLOCK_FREENESS, VerificationRecord.Outcome.UNDETERMINED);

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testSilentVerificationProceedsDespiteSoundnessIssues() throws DeserialisationException {
        WorkspaceEntry we = loadWork("vme.stg.work");
        WorkspaceUtils.getAs(we, Stg.class).createPlace("pDisconnected", null);

        // Verification cancelled by the model validation would store no result
        Assertions.assertNull(new DeadlockFreenessVerificationCommand().execute(we));
        Assertions.assertTrue(DialogUtils.callSilently(() -> new DeadlockFreenessVerificationCommand().execute(we)));
        checkRecord(we, StgVerificationGoals.DEADLOCK_FREENESS, VerificationRecord.Outcome.PASS);

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testVacuousMutexProtocolIsCachedWithoutTool() throws DeserialisationException {
        // There are no mutex places in the STG, hence no verification is needed even if the tools are unavailable
        WorkspaceEntry we = loadWork("vme.stg.work");
        disableTools();
        Assertions.assertTrue(new MutexProtocolVerificationCommand().execute(we));
        checkRecord(we, StgVerificationGoals.MUTEX_PROTOCOL, VerificationRecord.Outcome.PASS);
        VerificationRecord record = new VerificationResultStore(we).getRecord(StgVerificationGoals.MUTEX_PROTOCOL);
        Assertions.assertEquals(VerificationResultStore.VACUOUS_TOOL, record.tool());
        Assertions.assertEquals(StgVerificationGoals.MUTEX_PROTOCOL.getVacuityMessage(), record.message());
        Assertions.assertTrue(new MutexProtocolVerificationCommand().execute(we));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testMutexProtocolAsksAboutSoundnessUnlessVacuous() throws DeserialisationException {
        // The prompt about the soundness issues is declined by default in the tests
        WorkspaceEntry vacuousWork = loadWork("vme.stg.work");
        WorkspaceUtils.getAs(vacuousWork, Stg.class).createPlace("pDisconnected", null);
        Assertions.assertTrue(new MutexProtocolVerificationCommand().execute(vacuousWork));
        Framework.getInstance().closeWork(vacuousWork);

        WorkspaceEntry mutexWork = loadWork("arbitration-3.stg.work");
        WorkspaceUtils.getAs(mutexWork, Stg.class).createPlace("pDisconnected", null);
        Assertions.assertNull(new MutexProtocolVerificationCommand().execute(mutexWork));
        Assertions.assertNull(new VerificationResultStore(mutexWork).getRecord(StgVerificationGoals.MUTEX_PROTOCOL));
        Framework.getInstance().closeWork(mutexWork);
    }

    @Test
    void testCombinedFailureStoresNothing() throws DeserialisationException {
        WorkspaceEntry we = loadWork("arbitration-3.stg.work");
        Assertions.assertFalse(new CombinedVerificationCommand().execute(we));
        for (VerificationGoal goal : StgVerificationGoals.getCombinedGoals(true)) {
            Assertions.assertEquals(VerificationResultStore.Status.MISSING,
                    new VerificationResultStore(we).lookup(goal).getStatus(), goal.getId());
        }

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testModelChangeInvalidatesResult() throws DeserialisationException {
        WorkspaceEntry we = loadWork("vme.stg.work");
        Assertions.assertTrue(new ConsistencyVerificationCommand().execute(we));

        WorkspaceUtils.getAs(we, Stg.class).createPlace("pTest", null);
        Assertions.assertEquals(VerificationResultStore.Status.STALE,
                new VerificationResultStore(we).lookup(StgVerificationGoals.CONSISTENCY).getStatus());

        disableTools();
        Assertions.assertNull(new ConsistencyVerificationCommand().execute(we));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testExceptionsInvalidateOnlyTheirGoal() throws DeserialisationException {
        WorkspaceEntry we = loadWork("vme.stg.work");
        Assertions.assertTrue(new CombinedVerificationCommand().execute(we));

        new InputPropernessDataPreserver(we).saveData(List.of("dsr"));
        VerificationResultStore store = new VerificationResultStore(we);
        Assertions.assertEquals(VerificationResultStore.Status.STALE,
                store.lookup(StgVerificationGoals.INPUT_PROPERNESS).getStatus());
        Assertions.assertTrue(store.lookup(StgVerificationGoals.CONSISTENCY).isValid());

        disableTools();
        Assertions.assertNull(new InputPropernessVerificationCommand().execute(we));
        Assertions.assertTrue(new ConsistencyVerificationCommand().execute(we));
        // Combined command is re-run as a whole if any of its goals is stale
        Assertions.assertNull(new CombinedVerificationCommand().execute(we));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testResultsSurviveSaveAndLoad() throws DeserialisationException, SerialisationException {
        Framework framework = Framework.getInstance();
        File directory = FileUtils.createTempDirectory("verification-cache-test-");
        FileUtils.deleteOnExitRecursively(directory);
        WorkspaceEntry we = loadWork("vme.stg.work");
        Assertions.assertTrue(new CombinedVerificationCommand().execute(we));

        File file = new File(directory, "vme-cached.stg.work");
        framework.saveWork(we, file);
        framework.closeWork(we);

        WorkspaceEntry loadedWe = framework.loadWork(file);
        disableTools();
        Assertions.assertTrue(new DeadlockFreenessVerificationCommand().execute(loadedWe));
        Assertions.assertTrue(new CombinedVerificationCommand().execute(loadedWe));

        framework.closeWork(loadedWe);
    }

}
