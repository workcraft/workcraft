package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.circuit.utils.CircuitVerificationGoals;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.ModelHasher;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.verification.VerificationGoal;
import org.workcraft.verification.VerificationRecord;
import org.workcraft.verification.VerificationResultStore;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

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
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @AfterEach
    void restoreSettings() {
        enableTools();
    }

    private void disableTools() {
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat-missing"));
    }

    private File getResourceFile(String name) throws URISyntaxException {
        URL url = ClassLoader.getSystemClassLoader().getResource(PackageUtils.getPackagePath(getClass(), name));
        return new File(url.toURI());
    }

    @Test
    void testHashesDoNotDependOnLocation() throws DeserialisationException, URISyntaxException, IOException {
        Framework framework = Framework.getInstance();
        String[] circuitHashes = new String[2];
        String[] environmentHashes = new String[2];
        for (int i = 0; i < 2; i++) {
            File directory = FileUtils.createTempDirectory("verification-cache-test-");
            FileUtils.deleteOnExitRecursively(directory);
            File circuitFile = new File(directory, "celement.circuit.work");
            copyResource("celement.circuit.work", circuitFile);
            copyResource("celement.stg.work", new File(directory, "celement.stg.work"));

            WorkspaceEntry we = framework.loadWork(circuitFile);
            Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
            Assertions.assertNotNull(circuit.getEnvironmentFile());
            circuitHashes[i] = ModelHasher.getMathHash(we);
            environmentHashes[i] = ModelHasher.getFileHash(circuit.getEnvironmentFile());
            framework.closeWork(we);
        }
        Assertions.assertEquals(circuitHashes[0], circuitHashes[1]);
        Assertions.assertEquals(environmentHashes[0], environmentHashes[1]);
    }

    private void copyResource(String name, File target) throws URISyntaxException, IOException {
        Files.copy(getResourceFile(name).toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void checkRecord(WorkspaceEntry we, VerificationGoal goal, VerificationRecord.Outcome outcome) {
        VerificationRecord record = new VerificationResultStore(we).getRecord(goal);
        Assertions.assertNotNull(record, goal.getId());
        Assertions.assertEquals(outcome, record.outcome(), goal.getId());
        VerificationRecord.Dependency environment = record.dependencies().get("");
        Assertions.assertNotNull(environment, goal.getId());
        Assertions.assertTrue(environment.file().endsWith("celement.stg.work"), goal.getId());
    }

    @Test
    void testRefinementAndStrictImplementationReuseAndEnvironmentChange()
            throws DeserialisationException, URISyntaxException, IOException {

        File directory = FileUtils.createTempDirectory("verification-cache-test-");
        FileUtils.deleteOnExitRecursively(directory);
        File circuitFile = new File(directory, "celement.circuit.work");
        File environmentFile = new File(directory, "celement.stg.work");
        copyResource("celement.circuit.work", circuitFile);
        copyResource("celement.stg.work", environmentFile);

        Framework framework = Framework.getInstance();
        WorkspaceEntry we = framework.loadWork(circuitFile);
        Assertions.assertTrue(new RefinementVerificationCommand().execute(we));
        Assertions.assertTrue(new StrictImplementationVerificationCommand().execute(we));
        checkRecord(we, CircuitVerificationGoals.REFINEMENT, VerificationRecord.Outcome.PASS);
        checkRecord(we, CircuitVerificationGoals.STRICT_IMPLEMENTATION, VerificationRecord.Outcome.PASS);

        disableTools();
        Assertions.assertTrue(new RefinementVerificationCommand().execute(we));
        Assertions.assertTrue(new StrictImplementationVerificationCommand().execute(we));

        // Different environment makes the results stale, even though the circuit itself is not changed
        copyResource("celement-deadlock.stg.work", environmentFile);
        Assertions.assertNull(new RefinementVerificationCommand().execute(we));
        Assertions.assertNull(new StrictImplementationVerificationCommand().execute(we));

        // Restoring the environment makes the results valid again
        copyResource("celement.stg.work", environmentFile);
        Assertions.assertTrue(new RefinementVerificationCommand().execute(we));
        Assertions.assertTrue(new StrictImplementationVerificationCommand().execute(we));

        framework.closeWork(we);
    }

    @Test
    void testAuxiliaryPortSettingInvalidatesOnlyOutputPersistency()
            throws DeserialisationException, URISyntaxException, IOException {

        File directory = FileUtils.createTempDirectory("verification-cache-test-");
        FileUtils.deleteOnExitRecursively(directory);
        File circuitFile = new File(directory, "celement.circuit.work");
        copyResource("celement.circuit.work", circuitFile);
        copyResource("celement.stg.work", new File(directory, "celement.stg.work"));

        String oldRegex = CircuitSettings.getAuxiliaryPortRegex();
        Framework framework = Framework.getInstance();
        WorkspaceEntry we = framework.loadWork(circuitFile);
        try {
            Assertions.assertTrue(new CombinedVerificationCommand().execute(we));
            Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
            String outputPortName = circuit.getOutputPorts().iterator().next().getName();
            disableTools();

            // Setting that changes nothing for this circuit keeps all results valid
            CircuitSettings.setAuxiliaryPortRegex("no_such_port");
            Assertions.assertTrue(new OutputPersistencyVerificationCommand().execute(we));

            // Setting that makes an output port ignored changes only the exceptions of output persistency
            CircuitSettings.setAuxiliaryPortRegex(Pattern.quote(outputPortName));
            Assertions.assertTrue(new ConformationVerificationCommand().execute(we));
            Assertions.assertTrue(new DeadlockFreenessVerificationCommand().execute(we));
            Assertions.assertNull(new OutputPersistencyVerificationCommand().execute(we));
            VerificationResultStore store = new VerificationResultStore(we);
            VerificationResultStore.Lookup lookup = store.lookup(CircuitVerificationGoals.OUTPUT_PERSISTENCY,
                    name -> ModelHasher.getFileHash(circuit.getEnvironmentFile()));

            Assertions.assertEquals(VerificationResultStore.Status.STALE, lookup.getStatus());
            Assertions.assertTrue(lookup.getReason().contains("exceptions"), lookup.getReason());
            // The result still describes which ports were ignored when it was obtained
            Assertions.assertEquals("", lookup.getRecord().exceptions());

            // Restoring the setting makes the result valid again
            CircuitSettings.setAuxiliaryPortRegex(oldRegex);
            Assertions.assertTrue(new OutputPersistencyVerificationCommand().execute(we));
        } finally {
            CircuitSettings.setAuxiliaryPortRegex(oldRegex);
            framework.closeWork(we);
        }
    }

    @Test
    void testCombinedCommandResultsDependOnEnvironment()
            throws DeserialisationException, URISyntaxException, IOException {

        File directory = FileUtils.createTempDirectory("verification-cache-test-");
        FileUtils.deleteOnExitRecursively(directory);
        File circuitFile = new File(directory, "celement.circuit.work");
        File environmentFile = new File(directory, "celement.stg.work");
        copyResource("celement.circuit.work", circuitFile);
        copyResource("celement.stg.work", environmentFile);

        Framework framework = Framework.getInstance();
        WorkspaceEntry we = framework.loadWork(circuitFile);
        Assertions.assertTrue(new CombinedVerificationCommand().execute(we));
        checkRecord(we, CircuitVerificationGoals.CONFORMATION, VerificationRecord.Outcome.PASS);
        checkRecord(we, CircuitVerificationGoals.OUTPUT_PERSISTENCY, VerificationRecord.Outcome.PASS);
        checkRecord(we, CircuitVerificationGoals.DEADLOCK_FREENESS, VerificationRecord.Outcome.PASS);

        disableTools();
        Assertions.assertTrue(new ConformationVerificationCommand().execute(we));
        Assertions.assertTrue(new OutputPersistencyVerificationCommand().execute(we));
        Assertions.assertTrue(new DeadlockFreenessVerificationCommand().execute(we));

        copyResource("celement-deadlock.stg.work", environmentFile);
        Assertions.assertNull(new ConformationVerificationCommand().execute(we));
        Assertions.assertNull(new CombinedVerificationCommand().execute(we));

        framework.closeWork(we);
    }

}
